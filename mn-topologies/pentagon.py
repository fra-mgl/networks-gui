from mininet.net import Mininet
from mininet.node import RemoteController
from mininet.log import setLogLevel
from mininet.cli import CLI

# Wrapper of Mininet configuration

def deployNetwork():
    net = Mininet(topo=None, controller=RemoteController)

    # remote Ryu controller
    c1 = net.addController('C1')
    # 5 routers
    r1 = net.addSwitch('R1', dpid="0000000000000001")
    r2 = net.addSwitch('R2', dpid="0000000000000002")
    r3 = net.addSwitch('R3', dpid="0000000000000003")
    r4 = net.addSwitch('R4', dpid="0000000000000004")
    r5 = net.addSwitch('R5', dpid="0000000000000005")

    r1.start( [c1] )
    r2.start( [c1] )
    r3.start( [c1] )
    r4.start( [c1] )
    r5.start( [c1] )
    
    # 5 switches
    s1 = net.addSwitch('S6', dpid="0000000000000006")
    s2 = net.addSwitch('S7', dpid="0000000000000007")
    s3 = net.addSwitch('S8', dpid="0000000000000008")
    s4 = net.addSwitch('S9', dpid="0000000000000009")
    s5 = net.addSwitch('S10', dpid="000000000000000A")

    s1.start( [c1] )
    s2.start( [c1] )
    s3.start( [c1] )
    s4.start( [c1] )
    s5.start( [c1] )

    # 5 hosts
    h1 = net.addHost('H1', ip='10.0.1.1/24')
    h2 = net.addHost('H2', ip='10.0.2.1/24')
    h3 = net.addHost('H3', ip='10.0.3.1/24')
    h4 = net.addHost('H4', ip='10.0.4.1/24')
    h5 = net.addHost('H5', ip='10.0.5.1/24')

    # network nodes are linked together

    net.addLink(h1, s1, intfName1='H1-eth0', intfName2='S6-eth0')
    net.addLink(s1, r1, intfName1='S6-eth1', intfName2='R1-eth0')

    net.addLink(h2, s2, intfName1='H2-eth0', intfName2='S7-eth0')
    net.addLink(s2, r2, intfName1='S7-eth1', intfName2='R2-eth0')

    net.addLink(h3, s3, intfName1='H3-eth0', intfName2='S8-eth0')
    net.addLink(s3, r3, intfName1='S8-eth1', intfName2='R3-eth0')

    net.addLink(h4, s4, intfName1='H4-eth0', intfName2='S9-eth0')
    net.addLink(s4, r4, intfName1='S9-eth1', intfName2='R4-eth0')

    net.addLink(h5, s5, intfName1='H5-eth0', intfName2='S10-eth0')
    net.addLink(s5, r5, intfName1='S10-eth1', intfName2='R5-eth0')
    
    net.addLink(r5, r4, intfName1='R5-eth1', intfName2='R4-eth1')
    net.addLink(r4, r3, intfName1='R4-eth2', intfName2='R3-eth1')
    net.addLink(r3, r2, intfName1='R3-eth2', intfName2='R2-eth1')
    net.addLink(r2, r1, intfName1='R2-eth2', intfName2='R1-eth1')
    net.addLink(r1, r5, intfName1='R1-eth2', intfName2='R5-eth2')

    # second subnetwork

    net.start()

    # IPs are assigned to hosts
    h1.cmd('ifconfig H1-eth0 10.0.1.1 netmask 255.255.255.0')
    h1.cmd('ifconfig H1-eth0 up')
    h2.cmd('ifconfig H2-eth0 10.0.2.1 netmask 255.255.255.0')
    h2.cmd('ifconfig H2-eth0 up')
    h3.cmd('ifconfig H3-eth0 10.0.3.1 netmask 255.255.255.0')
    h3.cmd('ifconfig H3-eth0 up')
    h4.cmd('ifconfig H4-eth0 10.0.4.1 netmask 255.255.255.0')
    h4.cmd('ifconfig H4-eth0 up')
    h5.cmd('ifconfig H5-eth0 10.0.5.1 netmask 255.255.255.0')
    h5.cmd('ifconfig H5-eth0 up')

    # Default gateways
    h1.cmd('ip route add default via 10.0.1.254 dev H1-eth0')
    h2.cmd('ip route add default via 10.0.2.254 dev H2-eth0')
    h3.cmd('ip route add default via 10.0.3.254 dev H3-eth0')
    h4.cmd('ip route add default via 10.0.4.254 dev H4-eth0')
    h5.cmd('ip route add default via 10.0.5.254 dev H5-eth0')

    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    deployNetwork()