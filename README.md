# networks-gui
Final project for Networking 2 course (University of Trento)

## Flask Mininet App

Steps to setup your environment:

* ```cd mn-flask```
* ```sudo python3 -m venv .venv --system-site-packages``` to create a python virtual environment. 
    * Don't forget to run the command with `sudo` privileges.
    * The `--system-site-packages` flag is needed for the environment to be able to access the globally installed mininet package.
* ```sudo flask run``` to run the webserver. `sudo` privileges are needed in order to start mininet.
