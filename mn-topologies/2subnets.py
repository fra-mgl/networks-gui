from mininet.net import Mininet
from mininet.node import RemoteController
from mininet.log import setLogLevel
from mininet.cli import CLI

# Wrapper of Mininet configuration

def deployNetwork():
    net = Mininet(topo=None, controller=RemoteController)

    # remote Ryu controller
    net.addController('C1')
    # 2 routers
    r1 = net.addSwitch('R1', dpid="0000000000000001")
    r2 = net.addSwitch('R2', dpid="0000000000000002")
    # 2 switches
    s1 = net.addSwitch('S1', dpid="0000000000000003")
    s2 = net.addSwitch('S2', dpid="0000000000000004")
    # 4 hosts
    h1 = net.addHost('H1')
    h2 = net.addHost('H2')
    h3 = net.addHost('H3')
    h4 = net.addHost('H4')

    # network nodes are linked together

    # first subnetwork
    net.addLink(h1, s1, intfName1='H1-eth0', intfName2='S1-eth0')
    net.addLink(h2, s1, intfName1='H2-eth0', intfName2='S1-eth1')
    # THIS LINK IS ASSIGNED TO PORT 1 OF R1
    net.addLink(s1, r1, intfName1='S1-eth2', intfName2='R1-eth0')

    # second subnetwork
    net.addLink(h3, s2, intfName1='H3-eth0', intfName2='S2-eth0')
    net.addLink(h4, s2, intfName1='H4-eth0', intfName2='S2-eth1')
    # THIS LINK IS ASSIGNED TO PORT 1 OF R1
    net.addLink(s2, r2, intfName1='S2-eth2', intfName2='R2-eth0')

    # THIS LINK IS ASSIGNED TO PORT 2, FOR BOTH R1 AND R2
    net.addLink(r1, r2, intfName1='R1-eth1', intfName2='R2-eth1')

    net.start()

    # 10.0.1.0/255 network
    h1.cmd('ifconfig H1-eth0 10.0.1.1 netmask 255.255.255.0')
    h1.cmd('ifconfig H1-eth0 up')
    h2.cmd('ifconfig H2-eth0 10.0.1.2 netmask 255.255.255.0')
    h2.cmd('ifconfig H2-eth0 up')
    # router 1 default gateway
    h1.cmd('ip route add default via 10.0.1.254 dev H1-eth0')
    h2.cmd('ip route add default via 10.0.1.254 dev H2-eth0')

    # 10.0.2.0/255 network
    h3.cmd('ifconfig H3-eth0 10.0.2.1 netmask 255.255.255.0')
    h3.cmd('ifconfig H3-eth0 up')
    h4.cmd('ifconfig H4-eth0 20.0.2.2 netmask 255.255.255.0')
    h4.cmd('ifconfig H4-eth0 up')
    # router 2 default gateway
    h3.cmd('ip route add default via 10.0.2.254 dev H3-eth0')
    h4.cmd('ip route add default via 10.0.2.254 dev H4-eth0')

    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    deployNetwork()
