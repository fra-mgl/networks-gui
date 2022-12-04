from mininet.net import Mininet
from utils import Result

# Class wrapping a Mininet emulated network

class SDNetwork:

    def __init__(self):
        self.net = Mininet()
        self.hosts = {}
        self.switches = {}

    def nHosts(self):
        return len(self.hosts)

    def nSwitches(self):
        return len(self.switches)

    # Adds an host to the network topology. Giving it the name `h<ID>`

    def addHost(self):
        hostName = f"h{self.nHosts()}"
        host = self.net.addHost(hostName)
        self.hosts[hostName] = host

        return hostName

    # Adds a switch to the network topology. Giving it the name `s<ID>`

    def addSwitch(self):
        switchName = f"s{self.nSwitches()}"
        switch = self.net.addSwitch(switchName)
        self.hosts[switchName] = switch

        return switchName

    # Links two network entities together

    def addLink(self, name1, name2) -> Result:
        if name1 == name2:
            return Result.Error(f"cannot link `{name1}` with itself")

        # Check wether the first network entity actually exists. It can either
        # be an host or a switch.

        item1 = None
        if name1 in self.hosts.keys():
            item1 = self.hosts[name1]
        elif name1 in self.switches.keys():
            item1 = self.switches[name1]
        else:
            return Result.Error(f"unknown host or switch `{name1}`")

        # Check also wether the second network entity actually exists.

        item2 = None
        if name2 in self.hosts.keys():
            item2 = self.hosts[name2]
        elif name2 in self.switches.keys():
            item2 = self.switches[name2]
        else:
            return Result.Error(f"unknown host or switch `{name2}`")

        self.net.addLink(item1, item2)

        return Result.Ok(None)

    # Starts the network emulation

    def start(self):
        self.net.start()
