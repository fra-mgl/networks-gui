import logging
from ryu.app.wsgi import ControllerBase
from ryu.exception import OFPUnknownVersion
from ryu.lib import dpid as dpid_lib
from utils import *
from router import Router


class L3Controller(ControllerBase):

    def __init__(self, req, link, data, **kwargs):
        super(L3Controller, self).__init__(req, link, data, **kwargs)
        self.waiters = data['waiters']
        self.routers_list = {}
        self._LOGGER = None
    
    def set_logger(self, logger):
        self._LOGGER = logger
        self._LOGGER.propagate = False
        hdlr = logging.StreamHandler()
        fmt_str = '[RT][%(levelname)s] switch_id=%(sw_id)s: %(message)s'
        hdlr.setFormatter(logging.Formatter(fmt_str))
        self._LOGGER.addHandler(hdlr)

    def register_datapath(self, dp):
        dpid = {'sw_id': dpid_lib.dpid_to_str(dp.id)}
        try:
            router = Router(dp, self._LOGGER)
        except OFPUnknownVersion as message:
            return
        self.routers_list.setdefault(dp.id, router)

        self._access_router('0000000000000003', 0, 'set_data',
        {'address': '10.0.0.254/24'})
        self._access_router('0000000000000003', 0, 'set_data', 
        {'address': '20.0.0.254/24'})

        self._LOGGER.info('Join as router.', extra=dpid)

    def unregister_datapath(self, dp):
        if dp.id in self.routers_list:
            self.routers_list[dp.id].delete()
            del self.routers_list[dp.id]

            dpid = {'sw_id': dpid_lib.dpid_to_str(dp.id)}
            self._LOGGER.info('Leave router.', extra=dpid)

    def packet_in_handler(self, msg):
        dp_id = msg.datapath.id
        if dp_id in self.routers_list:
            router = self.routers_list[dp_id]
            router.packet_in_handler(msg)

    def _access_router(self, switch_id, vlan_id, func, params):
        rest_message = []
        routers = self._get_router(switch_id)
        for router in routers.values():
            function = getattr(router, func)
            data = function(vlan_id, params, self.waiters)
            rest_message.append(data)

        return rest_message

    def _get_router(self, switch_id):
        routers = {}

        if switch_id == REST_ALL:
            routers = self.routers_list
        else:
            sw_id = dpid_lib.str_to_dpid(switch_id)
            if sw_id in self.routers_list:
                routers = {sw_id: self.routers_list[sw_id]}

        if routers:
            return routers
        else:
            raise NotFoundError(switch_id=switch_id)