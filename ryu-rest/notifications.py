import requests
import json

from ryu.app.wsgi import ControllerBase
from ryu.app.wsgi import Response
from ryu.app.wsgi import route

NOTIFICATION_CONSUMER_ENDPOINT = ('127.0.0.1', 8000)
NETCONF_BACKEND_URL = 'http://net-conf:4000/'
IP_ADDRESSES_ENDPOINT = NETCONF_BACKEND_URL + 'allDataPathIps'
IP_TABLES_ENDPOINT = NETCONF_BACKEND_URL + 'allIpTables'

# Controller that exposes an endpoint to allow the main
# controller to receive notifications from the network
# configuration service. When a configuration is available
# it configures the network

class NotificationsController(ControllerBase):
    def __init__(self, req, link, data, **config):
        super(NotificationsController, self).__init__(
            req, link, data, **config)
        self.app = data['app']

    @route('notification', '/notification', methods=['GET'])
    def notification_handler(self, req, **kwargs):
        # Ip addresses and ip routing tables are queried
        # to the notification service
        ip_addresses = HTTPClient.get(IP_ADDRESSES_ENDPOINT, None)
        ip_tables = HTTPClient.get(IP_TABLES_ENDPOINT, None)

        for dpid in ip_addresses:
            # The switch is registered as a L3 switch
            dp_ips = ip_addresses[dpid]
            dp_ip_table = ip_tables[dpid]
            datapath = self.app.not_configured_datapaths[int(dpid)]
            self.app.l3_controller.register_datapath(
            datapath, dp_ips, dp_ip_table)
            del self.app.not_configured_datapaths[int(dpid)]

        # switches that did not get ip addresses assigned are
        # configured as L3 switches
        for dpid in self.app.not_configured_datapaths:
                datapath = self.app.not_configured_datapaths[dpid]
                self.app.l2_controller.register_datapath(datapath)
            
        self.app.not_configured_datapaths = None
        self.app.configured = True
        return Response(body=json.dumps({"message": "Configuration applied!"}))

# Simple client to perform HTTP requests

class HTTPClient:

    @classmethod
    def get(cls, url, params):
        response = requests.get(url=url, params=params)
        if response.status_code != 200:
            raise Exception(
                f"server {url} responded with: {response.status_code}")
        return response.json()

    def post(url):
        response = requests.post(url=url)
        if response.status_code != 200:
            print(f"server {url} responded with: {response.status_code}")

