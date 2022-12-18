import json

from ryu.app.wsgi import ControllerBase
from ryu.app.wsgi import Response
from ryu.app.wsgi import route
from ryu.lib import dpid as dpid_lib
from ryu.topology.api import get_switch, get_link, get_host
from utils import hexstr_to_int


class TopologyController(ControllerBase):
    def __init__(self, req, link, data, **config):
        super(TopologyController, self).__init__(req, link, data, **config)
        self.app = data['app']

    @route('topology', '/topology/l2switches', methods=['GET'])
    def list_l2switches(self, req, **kwargs):
        # If the network has yet to be configured for L3 forwarding,
        # all switches are considered as l2 switches
        if self.app.configured:
            return self._l2_switches(req, **kwargs)
        else:
            switches = get_switch(self.app, None)
            body = json.dumps([switch.to_dict() for switch in switches])
            return Response(content_type='application/json', body=body)

    @route('topology', '/topology/l3switches',
           methods=['GET'])
    def list_l3switches(self, req, **kwargs):
        # If the network has yet to be configured for L3 forwarding,
        # all switches are considered as L2. So the response is empty
        if self.app.configured:
            return self._l3_switches(req, **kwargs)
        else:
            return Response(content_type='application/json', 
            body=json.dumps([]))

    @route('topology', '/topology/links',
           methods=['GET'])
    def list_links(self, req, **kwargs):
        return self._links(req, **kwargs)

    @route('topology', '/topology/links/{dpid}',
           methods=['GET'], requirements={'dpid': dpid_lib.DPID_PATTERN})
    def get_links(self, req, **kwargs):
        return self._links(req, **kwargs)

    @route('topology', '/topology/hosts',
           methods=['GET'])
    def list_hosts(self, req, **kwargs):
        return self._hosts(req, **kwargs)

    @route('topology', '/topology/hosts/{dpid}',
           methods=['GET'], requirements={'dpid': dpid_lib.DPID_PATTERN})
    def get_hosts(self, req, **kwargs):
        return self._hosts(req, **kwargs)

    @route('mactable', '/mactable/{dpid}', methods=['GET'],
                requirements={'dpid': dpid_lib.DPID_PATTERN})
    def mac_table(self, req, **kwargs):
        if not self.app.configured:
            return Response(content_type='application/json', body=json.dumps({}))
        try:
            dpid = dpid_lib.str_to_dpid(kwargs['dpid'])
        except KeyError:
            return Response(status=400)

        if dpid not in self.app.l2_controller.datapaths:
            return Response(status=404)

        mac_table_raw = self.app.l2_controller.datapaths.get(dpid, {})
        mac_table = [{"mac": key, "port": port} for (key, port) in mac_table_raw.items()]
        body = json.dumps(mac_table)
        return Response(content_type='application/json', text=body)

    @route('iptable', '/iptable/{dpid}', methods=['GET'],
                requirements={'dpid': dpid_lib.DPID_PATTERN})
    def ip_table(self, req, **kwargs):
        if not self.app.configured:
            return Response(content_type='application/json', body=json.dumps({}))
        try:
            dpid = dpid_lib.str_to_dpid(kwargs['dpid'])
        except KeyError:
            return Response(status=400)

        if dpid not in self.app.l3_controller.datapaths:
            return Response(status=404)

        # Get the IP table from the controller
        ip_table_raw = self.app.l3_controller.ip_table(dpid)
        if ip_table_raw is None:
            ip_table_raw = {}
        
        # Trim the unnecessary information
        ip_table = [{"destination": key, "gateway": val['src_ip']} for (key, val) in ip_table_raw.items()]
        body = json.dumps(ip_table)
        return Response(content_type='application/json', text=body)

    def _l2_switches(self, req, **kwargs):
        dpid = None
        if 'dpid' in kwargs:
            dpid = dpid_lib.str_to_dpid(kwargs['dpid'])
        switches = get_switch(self.app, dpid)
        l2_switches = []
        for switch in switches:
            if switch.dp.id in self.app.l2_controller.datapaths:
                l2_switches.append(switch)
        body = json.dumps([switch.to_dict() for switch in l2_switches])
        return Response(content_type='application/json', body=body)

    def _l3_switches(self, req, **kwargs):
        dpid = None
        if 'dpid' in kwargs:
            dpid = dpid_lib.str_to_dpid(kwargs['dpid'])
        if dpid is None:
            l3_switches = self.app.l3_controller.datapaths.keys()
        else:
            if dpid in self.app.l3_controller.datapaths:
                l3_switches = [dpid]
            else:
                l3_switches = []
        
        l3_switches_data = [
            {
                "dpid": dpid_lib.dpid_to_str(dpid),
                "ports": [
                    {
                        "dpid": dpid_lib.dpid_to_str(dpid),
                        "hw_addr": port.mac,
                        "name": "",
                        "port_no": port.ip
                    }
                    for port in self.app.l3_controller.ports(dpid).values()
                ]
            }
            for dpid in l3_switches
        ]
        body = json.dumps(l3_switches_data)
        return Response(content_type='application/json', body=body)

    def _links(self, req, **kwargs):
        dpid = None
        if 'dpid' in kwargs:
            dpid = dpid_lib.str_to_dpid(kwargs['dpid'])
        links = get_link(self.app, dpid)
        body = json.dumps([link.to_dict() for link in links])
        return Response(content_type='application/json', body=body)

    def _hosts(self, req, **kwargs):
        dpid = None
        if 'dpid' in kwargs:
            dpid = dpid_lib.str_to_dpid(kwargs['dpid'])
        hosts = get_host(self.app, dpid)
        body = json.dumps([host.to_dict() for host in hosts])
        return Response(content_type='application/json', body=body)

    @route('explore', '/explore/{src_ip}/{dst_ip}', methods=['GET'])
    def explore(self, req, **kwargs):
        try:
            src_ip = kwargs['src_ip']
            dst_ip = kwargs['dst_ip']
        except KeyError:
            return Response(status=400)
        
        # Check that source and destination are not the same
        if src_ip == dst_ip:
            return Response(status=400)
        
        # Get the id of the L2 switches directly linked to the hosts
        hosts = get_host(self.app)
        src_dpid = None
        dst_dpid = None
        for host in hosts:
            host = host.to_dict()
            if host['ipv4']:
                if host['ipv4'][0] == src_ip:
                    src_dpid = dpid_lib.str_to_dpid(host['port']['dpid'])
                elif host['ipv4'][0] == dst_ip:
                    dst_dpid = dpid_lib.str_to_dpid(host['port']['dpid'])
        if src_dpid is None or dst_dpid is None:
            return Response(status=404)

        # Get the datapath ids of the gateways
        src_gateway_dpid, src_gateway_port = self.app.l3_controller.get_gateway_dpid_and_port(src_ip)
        dst_gateway_dpid, dst_gateway_port = self.app.l3_controller.get_gateway_dpid_and_port(dst_ip)
        src_gateway_mac = self.app.l3_controller.datapaths[src_gateway_dpid]['ports'][src_gateway_port].mac
        dst_gateway_mac = self.app.l3_controller.datapaths[dst_gateway_dpid]['ports'][dst_gateway_port].mac

        path = [src_dpid]
        gw_to_dst_path = []
        # From the source host to the gateway the path only traverses l2 switches
        curr_dpid = src_dpid
        while curr_dpid != src_gateway_dpid:
            curr_dpid = self.get_next_switch(curr_dpid, 
                    self.app.l2_controller.datapaths[curr_dpid][src_gateway_mac])
            path.append(curr_dpid)

        if src_gateway_dpid != dst_gateway_dpid:
            # Now compute the path between the layer 3 switches
            curr_dpid = src_gateway_dpid
            while curr_dpid != dst_gateway_dpid:
                curr_dpid = self.get_next_switch(curr_dpid,
                        self.app.l3_controller.datapaths[curr_dpid]['ip_table'][dst_ip]['src_port_no'])
                path.append(curr_dpid)
        else:
            path.pop()
        
        # Now compute the path from the from the destination host to its gateway
        curr_dpid = dst_dpid
        while curr_dpid != dst_gateway_dpid:
            gw_to_dst_path = [curr_dpid] + gw_to_dst_path
            curr_dpid = self.get_next_switch(curr_dpid, 
                    self.app.l2_controller.datapaths[curr_dpid][dst_gateway_mac])

        # the final path
        path = path + gw_to_dst_path
        path = {"path": [dpid for dpid in path]}
        return Response(status=200, content_type='application/json',
                body=json.dumps(path))
    
    def get_next_switch(self, prev_dp_id, out_port):
        links = get_link(self.app, prev_dp_id)
        for link in links:
            link = link.to_dict()
            if hexstr_to_int(link['src']['port_no']) == out_port:
                return dpid_lib.str_to_dpid(link['dst']['dpid'])
        
