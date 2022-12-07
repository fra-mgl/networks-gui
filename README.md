# networks-gui
Final project for Networking 2 course (University of Trento)

## Ryu rest API

Steps to setup your environment:

* ```$ cd ryu-rest```
* ```$ python3 -m venv .venv``` to create a python virtual environment. You might need to first install the python package required to create virtual enviroments. In that case you will get an error telling you to do that.
* ```$ . .venv/bin/activate``` to activate the virtual environment.
* ```$ pip install -r requirements.txt``` to install external dependencies.
* ```$ ryu-manager --observe-links app.py``` to run the application.


## Mininet emulation

To run the emulation, simply issue the command:
```
$ sudo python3 mn.py
```
Make sure to also have the Ryu controller running.
