import json
from mn import SDNetwork
from flask import Flask, request

app = Flask(__name__)
net = SDNetwork()

@app.get("/")
async def root():
    return {"message": "Hello World"}

# Endpoint to add an host to the network

@app.route('/addhost', methods=['GET'])
def addHost():
    hostName = net.addHost()
    return hostName, 200

# Endpoint to add a switch to the network

@app.route('/addswitch', methods=['GET'])
def addSwitch():
    switchName = net.addSwitch()
    return switchName, 200

# Endpoint to add a link between two nodes in the network

@app.route('/addlink', methods=['GET'])
def addLink():
    node1 = request.args.get('node1')
    node2 = request.args.get('node2')
    if node1 is None or node2 is None:
        return "invalid request format", 400

    res = net.addLink(node1, node2)
    if res.isError():
        return res.errMsg, 404
    
    return "", 200

# Endpoint to create a batch of nodes and links between them

@app.route('/addnodes', methods=['POST'])
def addNodes():

    # The request is parsed

    data = json.loads(request.data)
    try:
        hosts = data['hosts']
        switches = data['switches']
        links = data['links']
    except KeyError:
        return "invalid request body format", 400

    # Network nodes are created
    
    for host in hosts:
        net.addHost(name=host)
    for switch in switches:
        net.addSwitch(name=switch)

    # Network links are added

    for link in links:
        try:
            node1 = link['node1']
            node2 = link['node2']
        except KeyError:
            return "invalid request body format", 400
        
        res = net.addLink(node1, node2)
        if res.isError():
            return res.errMsg, 404
    
    return "", 200


if __name__ == '__main__':
    net.start()
    app.run()