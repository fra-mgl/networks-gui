from ryu.lib.packet import arp
from ryu.lib.packet import ethernet
from ryu.lib.packet import ipv4

ETHERNET = ethernet.ethernet.__name__
IPV4 = ipv4.ipv4.__name__
ARP = arp.arp.__name__

def ip_to_bstr(ip):
    splitted_ip = ip.split('.')
    bstr = ""
    for i in range(4):
        curr_digit = int(splitted_ip[i])
        curr_bstr = ""
        while curr_digit > 1:
            rem = curr_digit % 2
            curr_digit = curr_digit >> 1
            curr_bstr = str(rem) + curr_bstr
        curr_bstr = str(curr_digit) + curr_bstr
        while len(curr_bstr) < 8:
            curr_bstr = "0" + curr_bstr
        bstr = bstr + curr_bstr
    return bstr


def same_subnet(gw_ip, gw_netmask, src_ip):
    src_ip_bstr = ip_to_bstr(src_ip)
    gw_ip_bstr = ip_to_bstr(gw_ip)

    for i in range(gw_netmask):
        if src_ip_bstr[i] != gw_ip_bstr[i]:
            return False
    return True