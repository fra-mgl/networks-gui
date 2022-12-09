import numbers
import socket
import struct
from ryu.lib import dpid as dpid_lib
from ryu.lib.packet import tcp
from ryu.lib.packet import udp
from ryu.lib.packet import vlan
from ryu.lib.packet import arp
from ryu.lib.packet import ethernet
from ryu.lib.packet import icmp
from ryu.lib.packet import ipv4
from ryu.exception import RyuException
from ryu.lib import addrconv

UINT16_MAX = 0xffff
UINT32_MAX = 0xffffffff
UINT64_MAX = 0xffffffffffffffff

ETHERNET = ethernet.ethernet.__name__
VLAN = vlan.vlan.__name__
IPV4 = ipv4.ipv4.__name__
ARP = arp.arp.__name__
ICMP = icmp.icmp.__name__
TCP = tcp.tcp.__name__
UDP = udp.udp.__name__

MAX_SUSPENDPACKETS = 50  # Threshold of the packet suspends thread count.

ARP_REPLY_TIMER = 2  # sec
OFP_REPLY_TIMER = 1.0  # sec
CHK_ROUTING_TBL_INTERVAL = 1800  # sec

SWITCHID_PATTERN = dpid_lib.DPID_PATTERN + r'|all'
VLANID_PATTERN = r'[0-9]{1,4}|all'

VLANID_NONE = 0
VLANID_MIN = 2
VLANID_MAX = 4094

COOKIE_DEFAULT_ID = 0
COOKIE_SHIFT_VLANID = 32
COOKIE_SHIFT_ROUTEID = 16

DEFAULT_ROUTE = '0.0.0.0/0'
IDLE_TIMEOUT = 1800  # sec
DEFAULT_TTL = 64

REST_COMMAND_RESULT = 'command_result'
REST_RESULT = 'result'
REST_DETAILS = 'details'
REST_OK = 'success'
REST_NG = 'failure'
REST_ALL = 'all'
REST_SWITCHID = 'switch_id'
REST_VLANID = 'vlan_id'
REST_NW = 'internal_network'
REST_ADDRESSID = 'address_id'
REST_ADDRESS = 'address'
REST_ROUTEID = 'route_id'
REST_ROUTE = 'route'
REST_DESTINATION = 'destination'
REST_GATEWAY = 'gateway'

PRIORITY_VLAN_SHIFT = 1000
PRIORITY_NETMASK_SHIFT = 32

PRIORITY_NORMAL = 0
PRIORITY_ARP_HANDLING = 1
PRIORITY_DEFAULT_ROUTING = 1
PRIORITY_MAC_LEARNING = 2
PRIORITY_STATIC_ROUTING = 2
PRIORITY_IMPLICIT_ROUTING = 3
PRIORITY_L2_SWITCHING = 4
PRIORITY_IP_HANDLING = 5

PRIORITY_TYPE_ROUTE = 'priority_route'


def get_priority(priority_type, vid=0, route=None):
    log_msg = None
    priority = priority_type

    if priority_type == PRIORITY_TYPE_ROUTE:
        assert route is not None
        if route.dst_ip:
            priority_type = PRIORITY_STATIC_ROUTING
            priority = priority_type + route.netmask
            log_msg = 'static routing'
        else:
            priority_type = PRIORITY_DEFAULT_ROUTING
            priority = priority_type
            log_msg = 'default routing'

    if vid or priority_type == PRIORITY_IP_HANDLING:
        priority += PRIORITY_VLAN_SHIFT

    if priority_type > PRIORITY_STATIC_ROUTING:
        priority += PRIORITY_NETMASK_SHIFT

    if log_msg is None:
        return priority
    else:
        return priority, log_msg


def get_priority_type(priority, vid):
    if vid:
        priority -= PRIORITY_VLAN_SHIFT
    return priority


class NotFoundError(RyuException):
    message = 'Router SW is not connected. : switch_id=%(switch_id)s'


class CommandFailure(RyuException):
    pass

def ip_addr_aton(ip_str, err_msg=None):
    try:
        return addrconv.ipv4.bin_to_text(socket.inet_aton(ip_str))
    except (struct.error, socket.error) as e:
        if err_msg is not None:
            e.message = '%s %s' % (err_msg, e.message)
        raise ValueError(e.message)


def ip_addr_ntoa(ip):
    return socket.inet_ntoa(addrconv.ipv4.text_to_bin(ip))


def mask_ntob(mask, err_msg=None):
    try:
        return (UINT32_MAX << (32 - mask)) & UINT32_MAX
    except ValueError:
        msg = 'illegal netmask'
        if err_msg is not None:
            msg = '%s %s' % (err_msg, msg)
        raise ValueError(msg)


def ipv4_apply_mask(address, prefix_len, err_msg=None):
    import itertools

    assert isinstance(address, str)
    address_int = ipv4_text_to_int(address)
    return ipv4_int_to_text(address_int & mask_ntob(prefix_len, err_msg))


def ipv4_int_to_text(ip_int):
    assert isinstance(ip_int, numbers.Integral)
    return addrconv.ipv4.bin_to_text(struct.pack('!I', ip_int))


def ipv4_text_to_int(ip_text):
    if ip_text == 0:
        return ip_text
    assert isinstance(ip_text, str)
    return struct.unpack('!I', addrconv.ipv4.text_to_bin(ip_text))[0]


def nw_addr_aton(nw_addr, err_msg=None):
    ip_mask = nw_addr.split('/')
    default_route = ip_addr_aton(ip_mask[0], err_msg=err_msg)
    netmask = 32
    if len(ip_mask) == 2:
        try:
            netmask = int(ip_mask[1])
        except ValueError as e:
            if err_msg is not None:
                e.message = '%s %s' % (err_msg, e.message)
            raise ValueError(e.message)
    if netmask < 0:
        msg = 'illegal netmask'
        if err_msg is not None:
            msg = '%s %s' % (err_msg, msg)
        raise ValueError(msg)
    nw_addr = ipv4_apply_mask(default_route, netmask, err_msg)
    return nw_addr, netmask, default_route