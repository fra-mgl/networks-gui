# networks-gui
Final project for Networking 2 course (University of Trento)

## Launch the application
In order to launch the application, follow these simple steps. You should issue all commands from the root of the project.

### Mininet emulation

To run the emulation, issue these commands:
```
cd mn-topologies
sudo python3 topology.py
```
where `topology.py` is the example you chose.\
Your password may be required (Mininet needs `sudo` privilegies to run).

### Docker compose

A docker compose manages all backend containers. To run it, issue this command:
```
docker compose run --build
```


### Graphical interface

To run the application, issue these commands:
```
cd gui-java/app
java -jar piNet.jar
```
The app will ask you to provide a JSON file. Use the file with the same name as the topology you chose.\
The JSON file provides information regarding all routers in the network.

You may need to install the Java Runtime Environment (version 19). To do that, download it from [here](https://jdk.java.net/19/), extract it into a known path and add the path of the `bin` folder to the shell configuration file (e.g. if you are using bash, add `export PATH=$PATH:<your-jdk-path>/jdk-19.0.1/bin` to your `.bashrc` file).

---

## Add host to Mininet
As an example, let's suppose you want to add host H6 to your network and link it to switch S5.\
To do that, in Mininet shell, you should issue these commands:
```
py net.addHost('H6')
py net.addLink(S5, net.get('H6'), intfName1='S5-eth2', intfName2='H6-eth0')
py S5.attach('S5-eth2')
py net.get('H6').setIP('10.0.5.2')
py net.get('H6').cmd('ifconfig H6-eth0 10.0.5.2 netmask 255.255.255.0')
py net.get('H6').cmd('ifconfig H6-eth0 up')
py net.get('H6').cmd('ip route add default via 10.0.5.254 dev H6-eth0')
```

After that, at some point, a pop-up will appear in the app, asking you to refresh network data.

---

## Endpoints
All the endpoints implemented as Ryu-based RestAPI:

### Endpoints:

* `/topology/l2switches`: list of all layer 2 OF switches.
* `/topology/l3switches`: list of all layer 3 OF switches.
* `/topology/hosts`: list of all host connected to the network.
* `/topology/links`: list of all links between OF switches.
* `/mactable/{datapath_id}`: mac table of a layer 2 OF switch.
* `/iptable/{datapath_id}`: ip routing table of a layer 3 OF switch
* `/explore/{src_ip}/{dst_ip}`: list of OF switches that are part of the path from the source host to the destination host.

---

## Credits
1. All icons have been edited, according to Flaticon license.\
These are the original sources:

    - Host icons:\
    [Ui icons created by rukanicon - Flaticon](https://www.flaticon.com/free-icons/ui)


    - Switches icons:\
    [Network-switch icons created by Chattapat - Flaticon](https://www.flaticon.com/free-icons/network-switch)


    - Routers icons:\
    [Internet icons created by Freepik - Flaticon](https://www.flaticon.com/free-icons/internet)
