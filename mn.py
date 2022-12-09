from mininet.net import Mininet
from mininet.node import RemoteController
from mininet.log import setLogLevel
from mininet.cli import CLI

# Wrapper of Mininet configuration

def deployNetwork():
    net = Mininet(topo=None, controller=RemoteController)

    # remote Ryu controller
    net.addController('C1')
    # 3 switches
    s1 = net.addSwitch('S1')
    s2 = net.addSwitch('S2')
    s3 = net.addSwitch('S3')
    # 4 hosts
    h1 = net.addHost('H1')
    h2 = net.addHost('H2')
    h3 = net.addHost('H3')
    h4 = net.addHost('H4')

    # network nodes are linked together

    # first subnetwork
    net.addLink(h1, s1, intfName1='H1-eth0', intfName2='S1-eth0')
    net.addLink(h2, s1, intfName1='H2-eth0', intfName2='S1-eth1')
    net.addLink(s1, s3, intfName1='S1-eth2', intfName2='S3-eth0')

    # second subnetwork
    net.addLink(h3, s2, intfName1='H3-eth0', intfName2='S2-eth0')
    net.addLink(h4, s2, intfName1='H4-eth0', intfName2='S2-eth1')
    net.addLink(s2, s3, intfName1='S2-eth2', intfName2='S3-eth1')

    net.start()

    # 10.0.0/255 network
    h1.cmd('ifconfig H1-eth0 10.0.0.1 netmask 255.255.255.0')
    h1.cmd('ifconfig H1-eth0 up')
    h2.cmd('ifconfig H2-eth0 10.0.0.2 netmask 255.255.255.0')
    h2.cmd('ifconfig H2-eth0 up')
    h1.cmd('ip route add default via 10.0.0.254 dev H1-eth0')
    h2.cmd('ip route add default via 10.0.0.254 dev H2-eth0')

    # 20.0.0/255 network
    h3.cmd('ifconfig H3-eth0 20.0.0.1 netmask 255.255.255.0')
    h3.cmd('ifconfig H3-eth0 up')
    h4.cmd('ifconfig H4-eth0 20.0.0.2 netmask 255.255.255.0')
    h4.cmd('ifconfig H4-eth0 up')
    h3.cmd('ip route add default via 20.0.0.254 dev H3-eth0')
    h4.cmd('ip route add default via 20.0.0.254 dev H4-eth0')

    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    deployNetwork()
