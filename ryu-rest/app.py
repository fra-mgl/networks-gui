from ryu.app.wsgi import WSGIApplication
from ryu.base import app_manager
from ryu.controller import ofp_event
from ryu.controller.handler import set_ev_cls
from ryu.controller.handler import MAIN_DISPATCHER, CONFIG_DISPATCHER
from ryu.ofproto import ofproto_v1_3
# DON'T REMOVE THIS IMPORT
from ryu.topology.api import get_switch, get_link, get_host
from l2_controller import L2Controller
from l3_controller import L3Controller
from topology_controller import TopologyController
from ofctl import add_flow
from http_client import HTTPClient

NETCONF_BACKEND_URL = 'http://localhost:4000/'
IP_ADDRESSES_ENDPOINT = lambda dpid : NETCONF_BACKEND_URL + 'dataPathIps/' + str(dpid)
IP_TABLES_ENDPOINT = lambda dpid : NETCONF_BACKEND_URL + 'getIpTable/' + str(dpid)

class App(app_manager.RyuApp):

    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]

    _CONTEXTS = {'wsgi': WSGIApplication}

    def __init__(self, *args, **kwargs):
        super(App, self).__init__(*args, **kwargs)
        wsgi = kwargs['wsgi']
        mapper = wsgi.mapper
        wsgi.registory['TopologyController'] = {'app': self}
        wsgi.register(TopologyController, {'app': self})

        path = '/topology/l2switches'
        mapper.connect('topology', path, controller=TopologyController,
                       action='_l2_switches',
                       conditions=dict(method=['GET']))

        self.l2_controller = L2Controller()
        self.l3_controller = L3Controller()

    @set_ev_cls(ofp_event.EventOFPStateChange,
                 [MAIN_DISPATCHER])
    def _state_change_handler(self, ev):
        dpid = ev.datapath.id
        ip_addresses = HTTPClient.get(IP_ADDRESSES_ENDPOINT(dpid), None)
        if ip_addresses:
            routing_table = HTTPClient.get(IP_TABLES_ENDPOINT(dpid), None)
            self.l3_controller.register_datapath(ev.datapath, ip_addresses, routing_table)
        else:
            self.l2_controller.register_datapath(ev.datapath)

    
    @set_ev_cls(ofp_event.EventOFPSwitchFeatures, CONFIG_DISPATCHER)
    def switch_features_handler(self, ev):
        datapath = ev.msg.datapath
        ofproto = datapath.ofproto
        parser = datapath.ofproto_parser

        # Install table-miss flow entry. When an OFSwitch receives a packet
        # for an unknown destination, it sends it to the controller. This
        # allows the controller to learn the mac tables of all switches.
        match = parser.OFPMatch()
        actions = [parser.OFPActionOutput(ofproto.OFPP_CONTROLLER,
                                          ofproto.OFPCML_NO_BUFFER)]

        # The rule is added with priority 0, applying to all packets.
        # Because 0 is the lowest priority level, any other available rule is
        # applied first. If no rule is available for the current packet, the
        # switch will contact the controller.
        add_flow(datapath, 0, match, actions)

    @set_ev_cls(ofp_event.EventOFPPacketIn, MAIN_DISPATCHER)
    def packet_in_handler(self, ev):
        if ev.msg.datapath.id in self.l2_datapaths:
            self.l2_controller.packet_in_handler(ev.msg)
        elif ev.msg.datapath.id in self.l3_datapaths:
            self.l3_controller.packet_in_handler(ev.msg)
        else:
            raise Exception(f"Unknown datapath {ev.msg.datapath.id}\n")