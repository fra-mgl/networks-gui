import json

from ryu.app.wsgi import ControllerBase
from ryu.app.wsgi import Response
from ryu.app.wsgi import route
from ryu.lib import dpid as dpid_lib
from ryu.topology.api import get_switch, get_link, get_host


class TopologyController(ControllerBase):
    def __init__(self, req, link, data, **config):
        super(TopologyController, self).__init__(req, link, data, **config)
        self.app = data['topology_api_app']

    @route('topology', '/topology/l2switches',
           methods=['GET'])
    def list_switches(self, req, **kwargs):
        return self._l2_switches(req, **kwargs)

    @route('topology', '/topology/routers/{dpid}',
           methods=['GET'], requirements={'dpid': dpid_lib.DPID_PATTERN})
    def get_switch(self, req, **kwargs):
        return self._l2_switches(req, **kwargs)

    @route('topology', '/topology/l3switches',
           methods=['GET'])
    def list_switches(self, req, **kwargs):
        return self._l3_switches(req, **kwargs)

    @route('topology', '/topology/l3switches/{dpid}',
           methods=['GET'], requirements={'dpid': dpid_lib.DPID_PATTERN})
    def get_switch(self, req, **kwargs):
        return self._l3_switches(req, **kwargs)

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

    def _l2_switches(self, req, **kwargs):
        dpid = None
        if 'dpid' in kwargs:
            dpid = dpid_lib.str_to_dpid(kwargs['dpid'])
        switches = get_switch(self.app, dpid)
        l2_switches = []
        for switch in switches:
            if switch.dp.id in self.app.l2_datapaths:
                l2_switches.append(switch)
        body = json.dumps([switch.to_dict() for switch in l2_switches])
        return Response(content_type='application/json', body=body)

    def _l3_switches(self, req, **kwargs):
        dpid = None
        if 'dpid' in kwargs:
            dpid = dpid_lib.str_to_dpid(kwargs['dpid'])
        switches = get_switch(self.app, dpid)
        l3_switches = []
        for switch in switches:
            if switch.dp.id in self.app.l3_datapaths:
                l3_switches.append(switch)
        body = json.dumps([switch.to_dict() for switch in l3_switches])
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

    @route('mactable', '/mactable/{dpid}', methods=['GET'],
                requirements={'dpid': dpid_lib.DPID_PATTERN})
    def list_mac_table(self, req, **kwargs):
        try:
            dpid = int(kwargs['dpid'])
        except ValueError:
            return Response(status=400)

        if dpid not in self.app.l2_controller.switches_list:
            return Response(status=404)

        mac_table_raw = self.app.l2_controller.switches_list.get(dpid, {})
        print(mac_table_raw)
        mac_table = [{"mac": key, "port": port} for (key, port) in mac_table_raw.items()]
        body = json.dumps(mac_table)
        return Response(content_type='application/json', text=body)