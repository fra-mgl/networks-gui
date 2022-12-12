from ryu.lib.packet import arp
from ryu.lib.packet import packet, packet_base
from ryu.ofproto import ether
from ryu.lib.packet import ethernet
from ryu.lib.packet import ether_types
from ryu.lib import mac
from ofctl import add_flow, send_arp, send_packet
from utils import ARP, IPV4
from iptables import IpTable


class L3Controller:

    def __init__(self):
        self.datapaths = {}
    def ip_table(self, dpid):
        return self.datapaths[dpid]['ip_table']
    def ports(self, dpid):
        return self.datapaths[dpid]['ports']
    def packet_buffers(self, dpid):
        return self.datapaths[dpid]['packet_buffers']

    def register_datapath(self, datapath, ip_addresses, ip_table):
        # For each known datapath, the controller keeps a data structure
        # with relevant information
        self.datapaths[datapath.id] = {
            'ports': {},
            'ip_table': {},
            'packet_buffers': {}
        }

        # Register datapath ports
        for port in datapath.ports.values():
            if str(port.port_no) in ip_addresses:
                ip = ip_addresses[port.port_no]
                self.datapaths[datapath.id]['ports'][port.port_no] = Port(
                    port.hw_addr, ip['ip']
                )

        # Initialize datapath ip table
        _ip_table = IpTable()
        for key in ip_table:
            _ip_table[key] = ip_table[key]
        self.datapaths[datapath.id]['ip_table'] = _ip_table

    def buffer_packet(self, dpid, ip, packet):
        # The packet is added in its queue. If there is no
        # queue for the packet's destination IP, create one
        if ip in self.packet_buffers(dpid):
            self.packet_buffers(dpid)[ip].append(packet)
        else:
            self.packet_buffers(dpid)[ip] = [packet]

    def packet_in_handler(self, msg):
        # The way the packet is handled depends on its layer 3 protocol
        pkt = packet.Packet(msg.data)
        header_list = dict((p.protocol_name, p)
                           for p in pkt.protocols
                           if isinstance(p, packet_base.PacketBase))
        in_port = msg.match['in_port']
        datapath = msg.datapath
        if ARP in header_list:
            self.packet_in_arp(in_port, datapath, header_list[ARP])
        elif IPV4 in header_list:
            self.packet_in_ip(msg, header_list[IPV4])

    def packet_in_arp(self, in_port, datapath, arp_packet):
        port = self.ports(datapath.id)[in_port]

        # check wether the destination IP is equal to the IP
        # assigned to the input port
        if arp_packet.dst_ip == port.ip:
            if arp_packet.opcode == arp.ARP_REQUEST:
                # send an ARP reply with the port mac address
                src_mac = port.mac
                dst_mac = arp_packet.src_mac
                src_ip = arp_packet.dst_ip
                dst_ip = arp_packet.src_ip
                send_arp(datapath, arp.ARP_REPLY, src_mac, src_ip,
                         dst_mac, dst_ip, in_port)
            elif arp_packet.opcode == arp.ARP_REPLY:
                # learn mapping from IP address to MAC address
                src_ip = arp_packet.src_ip
                src_mac = arp_packet.src_mac

                # teach the router to send packets to the learned host
                parser = datapath.ofproto_parser
                match = parser.OFPMatch(eth_type=ether_types.ETH_TYPE_IP,
                                        ipv4_dst=src_ip)
                actions = []
                actions.append(parser.OFPActionDecNwTtl())
                actions.append(parser.OFPActionSetField(eth_src=port.mac))
                actions.append(parser.OFPActionSetField(eth_src=src_mac))
                actions.append(parser.OFPActionOutput(in_port, 0))
                add_flow(datapath, 1, match, actions)

                # flush out buffered packets for that IP
                for buffered_packet in self.packet_buffers(datapath.id)[src_ip]:
                    send_packet(datapath, in_port, buffered_packet)

    # The function is called when the OF switch receives for the first
    # time a packet for an host in one of its ground networks. The switch
    # sends an ARP request to learn the MAC address of the host and in the
    # meantime it buffers packets for that host

    def learn_host_mac(self, msg, src_mac, src_ip, port_no, dst_ip):
        datapath = msg.datapath

        # If an ARP request has already been issued fo the desired
        # IP address, just wait for the response
        if dst_ip not in self.packet_buffers(datapath.id):
            send_arp(datapath, arp.ARP_REQUEST, src_mac, src_ip,
                     mac.BROADCAST_STR, dst_ip, port_no)

        self.buffer_packet(datapath.id, dst_ip, msg)

    def packet_in_ip(self, msg, ip_packet):
        datapath = msg.datapath
        dst_ip = ip_packet.dst
        route = self.iptables(datapath.id)[dst_ip]

        if route is None:
            # TODO teach the switch to drop packets for which it does not
            # know a route
            pass
        elif route['dst_ip'] == '':
            # The switch can directly forward the packet to the
            # host, but first it needs to learn the host's MAC
            out_port = route['src_port_no']
            src_mac = self.ports(datapath.id)[out_port].mac
            self.learn_host_mac(msg, src_mac, route['src_ip'],
                                out_port, dst_ip)
        else:
            # The controller teaches the switch the rule for
            # packets destined to this subnet
            parser = datapath.ofproto_parser
            match = parser.OFPMatch(eth_type=ether_types.ETH_TYPE_IP,
                                    ipv4_dst=dst_ip)
            actions = []
            actions.append(parser.OFPActionDecNwTtl())
            actions.append(parser.OFPActionSetField(eth_src=mac))
            actions.append(parser.OFPActionSetField(eth_src=route['src_mac']))
            actions.append(parser.OFPActionOutput(route['port_no'], 0))
            add_flow(datapath, 1, match, actions)

            # The controller also forwards the packet to the switch
            out_port = route['src_port_no']
            src_mac = self.ports(datapath.id)[out_port].mac
            dst_mac = self.ports(datapath.id)[route['dst_port_no']].mac
            eth_header = ethernet.ethernet(dst_mac, src_mac, ether.ETH_TYPE_IP)
            ip_packet.add_protocol(eth_header)
            ip_packet.serialize()
            send_packet(datapath, out_port, ip_packet)

# Simple wrapper to record information about the ports of an OF switch

class Port:
    def __init__(self, port_mac, ip):
        self.mac = port_mac
        self.ip = ip

