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
from notifications import HTTPClient, NotificationsController
from threading import Semaphore

NETCONF_BACKEND_URL = 'http://net-conf:4000/'
IP_ADDRESSES_ENDPOINT = NETCONF_BACKEND_URL + 'allDataPathIps'
IP_TABLES_ENDPOINT = NETCONF_BACKEND_URL + 'allIpTables'

class App(app_manager.RyuApp):

    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]

    _CONTEXTS = {'wsgi': WSGIApplication}

    def __init__(self, *args, **kwargs):
        super(App, self).__init__(*args, **kwargs)
        # When the ryu application starts, the controller
        # waits for L3 configuration from the network configuration service.
        self.configured = False
        self.first_thread = True
        self.finished_configuration = False
        self.config_semaphore = Semaphore(1)

        # A data structure to keep track of switches that are
        # waiting to be configured
        self.not_configured_datapaths = dict()
        # The topology REST API
        wsgi = kwargs['wsgi']
        wsgi.register(TopologyController, {'app': self})
        wsgi.register(NotificationsController, {'app': self})
        # The controller responsible of configuring L2 switches
        self.l2_controller = L2Controller()

        # The controller responsible of configuring L3 switches
        self.l3_controller = L3Controller()

    @set_ev_cls(ofp_event.EventOFPStateChange,
                 [MAIN_DISPATCHER])
    def _state_change_handler(self, ev):
        # For the time being, the controller does not handle the
        # situation where a new switch is added to the physical network
        if self.configured and self.finished_configuration:
            raise Exception(f"A new switch entered the network, dpid: {ev.datapath.id}")
        else:
            print(f"Datapath {ev.datapath.id} needs configuration")
            self.not_configured_datapaths[ev.datapath.id] = ev.datapath

    
    @set_ev_cls(ofp_event.EventOFPSwitchFeatures, CONFIG_DISPATCHER)
    def switch_features_handler(self, ev):
        # Install table-miss flow entry as the default rule, so that when
        # an OFSwitch receives a packet that it doesn't know how to handle, 
        # it sends it to the controller.
        datapath = ev.msg.datapath
        ofproto = datapath.ofproto
        parser = datapath.ofproto_parser
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
        # while the network has yet to be configured at layer 3,
        # all packets are dropped
        if self.configured:
            # If the configuration has been applyed, there is no need to bother
            # with trying to enter the critical section
            if self.finished_configuration:

                if ev.msg.datapath.id in self.l2_controller.datapaths:
                    self.l2_controller.packet_in_handler(ev.msg)
                elif ev.msg.datapath.id in self.l3_controller.datapaths:
                    self.l3_controller.packet_in_handler(ev.msg)
                else:
                    raise Exception(f"Unknown datapath {ev.msg.datapath.id}\n")
            
            # Otherwise if the l3 configuration is now available, retrieve it
            # from the network configuration service. 
            else:
                # The first event handling thread that is able to acquire the
                # semaphore configures the controller. All other threads will
                # walk past the 'if' statement
                self.config_semaphore.acquire()
                if self.first_thread:
                    ip_addresses = HTTPClient.get(IP_ADDRESSES_ENDPOINT, None)
                    ip_tables = HTTPClient.get(IP_TABLES_ENDPOINT, None)

                    for dpid in ip_addresses:
                        # The switch is registered as a L3 switch
                        dp_ips = ip_addresses[dpid]
                        dp_ip_table = ip_tables[dpid]
                        datapath = self.not_configured_datapaths[int(dpid)]
                        self.l3_controller.register_datapath(datapath, dp_ips, dp_ip_table)
                        del self.not_configured_datapaths[int(dpid)]
                    
                    # switches that did not get ip addresses assigned are
                    # configured as L3 switches
                    for dpid in self.not_configured_datapaths:
                        datapath = self.not_configured_datapaths[dpid]
                        self.l2_controller.register_datapath(datapath)

                    # Trigger garbage collection, as this data structure will not
                    # be used anymore
                    self.not_configured_datapaths = None

                    self.first_thread = False
                    self.finished_configuration = True

                    # Release the semaphore to unblock other threads
                    self.config_semaphore.release()