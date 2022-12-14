from ryu.lib.packet import arp
from ryu.lib.packet import ethernet
from ryu.lib.packet import ipv4

ETHERNET = ethernet.ethernet.__name__
IPV4 = ipv4.ipv4.__name__
ARP = arp.arp.__name__

def ip_to_bstr(ip):
    ip = ip.split('/')[0]
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

def netmask_to_str(netmask):
    netmask = 0xFFFFFFFF - ((1 << (32 - netmask)) - 1)
    digit1 = (netmask & (255 << 24)) >> 24
    digit2 = (netmask & (255 << 16)) >> 16
    digit3 = (netmask & (255 << 8)) >> 8
    digit4 = netmask & 255
    netmask_str = str(digit1) + '.' + str(digit2) + '.' + str(digit3) + '.' + str(digit4)
    return netmask_str

def ip_to_int(ip):
    ip = ip.split('/')[0]
    splitted_ip = ip.split('.')
    res = 0
    for i in range(4):
        curr_digit = int(splitted_ip[3 - i])
        curr_digit = curr_digit << (i << 8) # curr_digit * (256 ** i)
        res = res + curr_digit
    return res

def same_subnet(gw_ip, gw_netmask, src_ip):
    src_ip_bstr = ip_to_bstr(src_ip)
    gw_ip_bstr = ip_to_bstr(gw_ip)

    for i in range(gw_netmask):
        if src_ip_bstr[i] != gw_ip_bstr[i]:
            return False
    return True

def hexstr_to_int(hstr):
    result = 0
    for i in range(len(hstr)):
        str_digit = hstr[len(hstr) - 1 - i]
        if str_digit.upper() == 'A':
            digit = 10
        elif str_digit.upper() == 'B':
            digit = 11
        elif str_digit.upper() == 'C':
            digit = 12
        elif str_digit.upper() == 'D':
            digit = 13
        elif str_digit.upper() == 'E':
            digit = 14
        elif str_digit.upper() == 'F':
            digit = 15
        else:
            digit = int(str_digit)
        result += digit << (i << 2)
    return result