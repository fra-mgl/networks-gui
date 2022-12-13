# networks-gui
Final project for Networking 2 course (University of Trento)

## Ryu rest API

Steps to setup your environment:

* ```$ cd ryu-rest```
* ```$ python3 -m venv .venv``` to create a python virtual environment. You might need to first install the python package required to create virtual enviroments. In that case you will get an error telling you to do that.
* ```$ . .venv/bin/activate``` to activate the virtual environment.
* ```$ pip install -r requirements.txt``` to install external dependencies.
* ```$ ryu-manager --observe-links app.py``` to run the application.

### Endpoints:

* `/topology/l2switches`: list of all layer 2 OF switches.
* `/topology/l2switches/{datapath_id}`: information about a specific layer 2 switch.
* `/topology/l3switches`: list of all layer 3 OF switches.
* `/topology/l3switches/{datapath_id}`: information about a specific layer 3 switch.
* `/topology/hosts`: list of all host connected to the network.
* `/topology/hosts/{datapath_id}`: list of all hosts connected to an OF switch.
* `/topology/links/{datapath_id}`: information about links related with a specific OF switch.
* `/mactable/{datapath_id}`: mac table of a layer 2 OF switch.
* `/iptable/{datapath_id}`: ip routing table of a layer 3 OF switch

---

## Mininet emulation

To run the emulation, simply issue the command:
```
$ sudo python3 mn.py
```
Make sure to also have the Ryu controller running.

---

## Credits
1. All icons have been edited, according to Flaticon licence.\
These are the original sources:

    - Host icons:\
    [Ui icons created by rukanicon - Flaticon](https://www.flaticon.com/free-icons/ui)


    - Switches icons:\
    [Network-switch icons created by Chattapat - Flaticon](https://www.flaticon.com/free-icons/network-switch)


    - Routers icons:\
    [Internet icons created by Freepik - Flaticon](https://www.flaticon.com/free-icons/internet)
