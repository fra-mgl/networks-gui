from mininet.net import Mininet
from mininet.node import RemoteController
from mininet.log import setLogLevel
from mininet.cli import CLI

# Wrapper of Mininet configuration

def deployNetwork():
    net = Mininet(topo=None, controller=RemoteController)

    # remote Ryu controller
    c1 = net.addController('C1')
    # 4 routers
    r1 = net.addSwitch('R1', dpid="0000000000000001")
    r2 = net.addSwitch('R2', dpid="0000000000000002")
    r3 = net.addSwitch('R3', dpid="0000000000000003")
    r4 = net.addSwitch('R4', dpid="0000000000000004")

    r1.start( [c1] )
    r2.start( [c1] )
    r3.start( [c1] )
    r4.start( [c1] )

    # 6 switches
    s1 = net.addSwitch('S1', dpid="0000000000000005")
    s2 = net.addSwitch('S2', dpid="0000000000000006")
    s3 = net.addSwitch('S3', dpid="0000000000000007")
    s4 = net.addSwitch('S4', dpid="0000000000000008")
    s5 = net.addSwitch('S5', dpid="0000000000000009")
    s6 = net.addSwitch('S6', dpid="000000000000000A")

    s1.start( [c1] )
    s2.start( [c1] )
    s3.start( [c1] )
    s4.start( [c1] )
    s5.start( [c1] )
    s6.start( [c1] )

    # 4 hosts
    h1 = net.addHost('H1', ip='10.0.1.1/24')
    h2 = net.addHost('H2', ip='10.0.1.2/24')
    h3 = net.addHost('H3', ip='10.0.1.3/24')

    h4 = net.addHost('H4', ip='10.0.2.1/24')
    h5 = net.addHost('H5', ip='10.0.2.2/24')
    h6 = net.addHost('H6', ip='10.0.2.3/24')
    h7 = net.addHost('H7', ip='10.0.2.4/24')
    h8 = net.addHost('H8', ip='10.0.2.5/24')

    h9 = net.addHost('H9', ip='10.0.3.1/24')


    # network nodes are linked together

    # FIRST SUBNETWORK
    net.addLink(h1, s1, intfName1='H1-eth0', intfName2='S1-eth0')
    net.addLink(h2, s1, intfName1='H2-eth0', intfName2='S1-eth1')
    
    net.addLink(h3, s2, intfName1='H3-eth0', intfName2='S2-eth0')

    # connections switches-gateway and gateway-R4
    net.addLink(s1, r1, intfName1='S1-eth2', intfName2='R1-eth0')
    net.addLink(s2, r1, intfName1='S2-eth1', intfName2='R1-eth1')
    net.addLink(r1, r4, intfName1='R1-eth2', intfName2='R4-eth0')

    # SECOND SUBNETWORK
    net.addLink(h4, s3, intfName1='H4-eth0', intfName2='S3-eth0')
    net.addLink(h5, s3, intfName1='H5-eth0', intfName2='S3-eth1')

    net.addLink(h6, s4, intfName1='H6-eth0', intfName2='S4-eth0')

    net.addLink(h7, s5, intfName1='H7-eth0', intfName2='S5-eth0')
    net.addLink(h8, s5, intfName1='H8-eth0', intfName2='S5-eth1')

    # connections switches-gateway and gateway-R4
    net.addLink(s3, r2, intfName1='S3-eth2', intfName2='R2-eth0')
    net.addLink(s4, r2, intfName1='S4-eth1', intfName2='R2-eth1')
    net.addLink(s5, r2, intfName1='S5-eth2', intfName2='R2-eth2')
    net.addLink(r2, r4, intfName1='R2-eth3', intfName2='R4-eth1')

    # THIRD SUBNETWORK
    net.addLink(h9, s6, intfName1='H9-eth0', intfName2='S6-eth0')
    
    # connections switches-gateway and gateway-R4
    net.addLink(s6, r3, intfName1='S6-eth1', intfName2='R3-eth0')
    net.addLink(r3, r4, intfName1='R3-eth1', intfName2='R4-eth2')


    net.start()

    # 10.0.1.0/255 network
    h1.cmd('ifconfig H1-eth0 10.0.1.1 netmask 255.255.255.0')
    h1.cmd('ifconfig H1-eth0 up')
    h2.cmd('ifconfig H2-eth0 10.0.1.2 netmask 255.255.255.0')
    h2.cmd('ifconfig H2-eth0 up')
    h3.cmd('ifconfig H3-eth0 10.0.1.3 netmask 255.255.255.0')
    h3.cmd('ifconfig H3-eth0 up')
    # router 1 default gateway
    h1.cmd('ip route add default via 10.0.1.254 dev H1-eth0')
    h2.cmd('ip route add default via 10.0.1.254 dev H2-eth0')
    h3.cmd('ip route add default via 10.0.1.254 dev H3-eth0')

    # 10.0.2.0/255 network
    h4.cmd('ifconfig H4-eth0 10.0.2.1 netmask 255.255.255.0')
    h4.cmd('ifconfig H4-eth0 up')
    h5.cmd('ifconfig H5-eth0 10.0.2.2 netmask 255.255.255.0')
    h5.cmd('ifconfig H5-eth0 up')
    h6.cmd('ifconfig H6-eth0 10.0.2.3 netmask 255.255.255.0')
    h6.cmd('ifconfig H6-eth0 up')
    h7.cmd('ifconfig H7-eth0 10.0.2.4 netmask 255.255.255.0')
    h7.cmd('ifconfig H7-eth0 up')
    h8.cmd('ifconfig H8-eth0 10.0.2.5 netmask 255.255.255.0')
    h8.cmd('ifconfig H8-eth0 up')
    # router 2 default gateway
    h4.cmd('ip route add default via 10.0.2.254 dev H4-eth0')
    h5.cmd('ip route add default via 10.0.2.254 dev H5-eth0')
    h6.cmd('ip route add default via 10.0.2.254 dev H6-eth0')
    h7.cmd('ip route add default via 10.0.2.254 dev H7-eth0')
    h8.cmd('ip route add default via 10.0.2.254 dev H8-eth0')

    # 10.0.3.0/255 network
    h9.cmd('ifconfig H9-eth0 10.0.3.1 netmask 255.255.255.0')
    h9.cmd('ifconfig H9-eth0 up')
    # router 3 default gateway
    h9.cmd('ip route add default via 10.0.3.254 dev H9-eth0')
    

    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    deployNetwork()
