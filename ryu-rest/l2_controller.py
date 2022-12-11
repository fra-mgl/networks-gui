from ofctl import add_flow
from ryu.lib.packet import packet
from ryu.lib.packet import ethernet
from ryu.lib.packet import ether_types


class L2Controller:

    def __init__(self):
        self.switches_list = {}

    def register_datapath(self, datapath):
        self.switches_list[datapath.id] = {}

    def unregister_datapath(self, datapath):
        self.switches_list[datapath.id]

    # The first time a switch receives a packet to forward from
    # MAC address A to MAC address B, it doesn't know which output port
    # to choose and sends the packet to the controller.
    #
    # The controller then instructs the switch to perform a
    # FLOOD operation, but does not write a rule in the switch's flow
    # table, so that next time it will again ask the controller what
    # to do with a packet from A to B.
    #
    # The destination host receives the FLOOD packet and replies to the
    # source host.
    #
    # When the switch receives the FLOOD response packet, the controller
    # learns the port associated to MAC B.
    #
    # The next time the switch needs to forward a packet from MAC A to
    # MAC B, it contacts again the controller, because no rule was written
    # in its flow table. Because now the controller knows the mapping from
    # MAC B to an output port, it writes a rule in the flow table of the
    # switch to avoid FLOODing.
    #
    # From now on the switch won't need to contact the controller to forward
    # packets from MAC A to MAC B.

    def packet_in_handler(self, msg):
        datapath = msg.datapath
        ofproto = datapath.ofproto
        parser = datapath.ofproto_parser
        in_port = msg.match['in_port']

        pkt = packet.Packet(msg.data)
        eth = pkt.get_protocols(ethernet.ethernet)[0]

        if eth.ethertype == ether_types.ETH_TYPE_LLDP:
            # ignore lldp packet
            return

        dst = eth.dst
        src = eth.src

        dpid = datapath.id
        self.switches_list.setdefault(dpid, {})

        # Learn that 'in_port' is associated to 'src' MAC address
        self.switches_list[dpid][src] = in_port
        if dst in self.switches_list[dpid]:
            out_port = self.switches_list[dpid][dst]
        else:
            out_port = ofproto.OFPP_FLOOD

        actions = [parser.OFPActionOutput(out_port)]

        # install a flow to avoid packet_in next time
        if out_port != ofproto.OFPP_FLOOD:
            match = parser.OFPMatch(in_port=in_port, eth_dst=dst, eth_src=src)
            # verify if we have a valid buffer_id, if yes avoid to send both
            # flow_mod & packet_out
            if msg.buffer_id != ofproto.OFP_NO_BUFFER:
                add_flow(datapath, 1, match, actions, msg.buffer_id)
                return
            else:
                add_flow(datapath, 1, match, actions)
        data = None
        if msg.buffer_id == ofproto.OFP_NO_BUFFER:
            data = msg.data

        out = parser.OFPPacketOut(datapath=datapath, buffer_id=msg.buffer_id,
                                  in_port=in_port, actions=actions, data=data)
        datapath.send_msg(out)

