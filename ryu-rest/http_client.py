import requests

# Simple client to perform HTTP requests

class HTTPClient:
    
    @classmethod
    def get(url, params):
        response = requests.get(url=url, params=params)
        if response.status_code != 200:
            raise Exception(f"server {url} responded with: {response.status_code}")
        return response.json()

    def post(url):
        response = requests.post(url=url)
        if response.status_code != 200:
            print(f"server {url} responded with: {response.status_code}")