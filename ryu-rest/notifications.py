import requests
import json

from ryu.app.wsgi import ControllerBase
from ryu.app.wsgi import Response
from ryu.app.wsgi import route

NOTIFICATION_CONSUMER_ENDPOINT = ('127.0.0.1', 8000)

# Simple client to perform HTTP requests

class HTTPClient:
    
    @classmethod
    def get(cls, url, params):
        response = requests.get(url=url, params=params)
        if response.status_code != 200:
            raise Exception(f"server {url} responded with: {response.status_code}")
        return response.json()

    def post(url):
        response = requests.post(url=url)
        if response.status_code != 200:
            print(f"server {url} responded with: {response.status_code}")

# Controller that exposes an endpoint to allow the main
# controller to receive notifications from the network 
# configuration service

class NotificationsController(ControllerBase):
    def __init__(self, req, link, data, **config):
        super(NotificationsController, self).__init__(req, link, data, **config)
        self.app = data['app']

    @route('notification', '/notification', methods=['GET'])
    def list_l2switches(self, req, **kwargs):
        self.app.configured = True
        return Response(body=json.dumps({"message": "Hello, world!"}))
