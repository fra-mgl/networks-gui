from utils import *

# Class that implements ip routing table semantics

class IpTable(dict):
    def __init__(self):
        super(IpTable, self).__init__()

    # The candidate object should be the string representation of 
    # an IP address.
    # The logic checks wether a netmask applies to the input IP

    def __contains__(self, __o: object) -> bool:
        if type(__o) != str:
            return False

        binary_ip = ip_to_bstr(__o)
        for key in self.keys():
            network_ip, netmask = key.split('/')
            network_ip = ip_to_bstr(network_ip)
            for i in range(netmask):
                if binary_ip[i] != network_ip[i]:
                    return False
        return True

    # The candidate object should be the string representation of 
    # an IP address.
    # The logic checks wether a netmask applies to the input IP. If one
    # match is found, the table entry for that netmask is returned

    def __getitem__(self, __key):
        if type(__key) != str:
            return None

        binary_ip = ip_to_bstr(__key)
        for key in self.keys():
            network_ip, netmask = key.split('/')
            netmask = int(netmask)
            network_ip = ip_to_bstr(network_ip)
            i = 0
            while i < netmask:
                if binary_ip[i] != network_ip[i]:
                    break
                i += 1
            if i == netmask:
                return super(IpTable, self).__getitem__(key)
        return None