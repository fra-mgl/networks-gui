from utils import *

class PortData(dict):
    def __init__(self, ports):
        super(PortData, self).__init__()
        for port in ports.values():
            data = Port(port.port_no, port.hw_addr)
            self[port.port_no] = data


class Port(object):
    def __init__(self, port_no, hw_addr):
        super(Port, self).__init__()
        self.port_no = port_no
        self.mac = hw_addr


class AddressData(dict):
    def __init__(self):
        super(AddressData, self).__init__()
        self.address_id = 1

    def add(self, l2interface, address):
        err_msg = 'Invalid [%s] value.' % REST_ADDRESS
        nw_addr, mask, default_gw = nw_addr_aton(address, err_msg=err_msg)

        # Check overlaps
        for other in self.values():
            other_mask = mask_ntob(other.netmask)
            add_mask = mask_ntob(mask, err_msg=err_msg)
            if (other.nw_addr == ipv4_apply_mask(default_gw, other.netmask) or
                    nw_addr == ipv4_apply_mask(other.default_gw, mask,
                                               err_msg)):
                msg = 'Address overlaps [address_id=%d]' % other.address_id
                raise CommandFailure(msg=msg)

        address = Address(self.address_id, l2interface, nw_addr, mask, default_gw)
        ip_str = ip_addr_ntoa(nw_addr)
        key = '%s/%d' % (ip_str, mask)
        self[key] = address

        self.address_id += 1
        self.address_id &= UINT32_MAX
        if self.address_id == COOKIE_DEFAULT_ID:
            self.address_id = 1

        return address

    def delete(self, address_id):
        for key, value in self.items():
            if value.address_id == address_id:
                del self[key]
                return

    def get_default_gw(self):
        return [address.default_gw for address in self.values()]

    def get_data(self, addr_id=None, ip=None):
        for address in self.values():
            if addr_id is not None:
                if addr_id == address.address_id:
                    return address
            else:
                assert ip is not None
                if ipv4_apply_mask(ip, address.netmask) == address.nw_addr:
                    return address
        return None


class Address(object):
    def __init__(self, address_id, eth_name, nw_addr,
                    netmask, default_gw):
        super(Address, self).__init__()
        self.address_id = address_id
        self.eth_name = eth_name
        self.nw_addr = nw_addr
        self.netmask = netmask
        self.default_gw = default_gw

    def __contains__(self, ip):
        return bool(ipv4_apply_mask(ip, self.netmask) == self.nw_addr)


class RoutingTable(dict):
    def __init__(self):
        super(RoutingTable, self).__init__()
        self.route_id = 1

    def add(self, dst_nw_addr, gateway_ip):
        err_msg = 'Invalid [%s] value.'

        if dst_nw_addr == DEFAULT_ROUTE:
            dst_ip = 0
            netmask = 0
        else:
            dst_ip, netmask, dummy = nw_addr_aton(
                dst_nw_addr, err_msg=err_msg % REST_DESTINATION)

        gateway_ip = ip_addr_aton(gateway_ip, err_msg=err_msg % REST_GATEWAY)

        # Check overlaps
        overlap_route = None
        if dst_nw_addr == DEFAULT_ROUTE:
            if DEFAULT_ROUTE in self:
                overlap_route = self[DEFAULT_ROUTE].route_id
        elif dst_nw_addr in self:
            overlap_route = self[dst_nw_addr].route_id

        if overlap_route is not None:
            msg = 'Destination overlaps [route_id=%d]' % overlap_route
            raise CommandFailure(msg=msg)

        routing_data = Route(self.route_id, dst_ip, netmask, gateway_ip)
        ip_str = ip_addr_ntoa(dst_ip)
        key = '%s/%d' % (ip_str, netmask)
        self[key] = routing_data

        self.route_id += 1
        self.route_id &= UINT32_MAX
        if self.route_id == COOKIE_DEFAULT_ID:
            self.route_id = 1

        return routing_data

    def delete(self, route_id):
        for key, value in self.items():
            if value.route_id == route_id:
                del self[key]
                return

    def get_gateways(self):
        return [routing_data.gateway_ip for routing_data in self.values()]

    def get_data(self, gw_mac=None, dst_ip=None):
        if gw_mac is not None:
            for route in self.values():
                if gw_mac == route.gateway_mac:
                    return route
            return None

        elif dst_ip is not None:
            get_route = None
            mask = 0
            for route in self.values():
                if ipv4_apply_mask(dst_ip, route.netmask) == route.dst_ip:
                    # For longest match
                    if mask < route.netmask:
                        get_route = route
                        mask = route.netmask

            if get_route is None:
                get_route = self.get(DEFAULT_ROUTE, None)
            return get_route
        else:
            return None


class Route(object):
    def __init__(self, route_id, dst_ip, netmask, gateway_ip):
        super(Route, self).__init__()
        self.route_id = route_id
        self.dst_ip = dst_ip
        self.netmask = netmask
        self.gateway_ip = gateway_ip
        self.gateway_mac = None