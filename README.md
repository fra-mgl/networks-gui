# networks-gui
Final project for Networking 2 course (University of Trento)

## Ryu rest API

Steps to setup your environment:

* ```cd ryu-rest```
* ```python3 -m venv .venv``` to create a python virtual environment. You might need to first install the python package required to create virtual enviroments. In that case you will get an error telling you to do that.
* ```. .venv/bin/activate``` to activate the virtual environment.
* ```pip install -r requirements.txt``` to install external dependencies.
* ```ryu-manager --observe-links app.py``` to run the application.


## Flask Mininet App

Steps to setup your environment:

* ```cd mn-flask```
* ```sudo python3 -m venv .venv --system-site-packages``` to create a python virtual environment. 
    * Don't forget to run the command with `sudo` privileges.
    * The `--system-site-packages` flag is needed for the environment to be able to access the globally installed mininet package.
* ```sudo flask run``` to run the webserver. `sudo` privileges are needed in order to start mininet.
