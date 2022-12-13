import requests

def main():
    with open("mn-topologies/2subnets.json", "r") as f:
        json_string = f.read()
        response = requests.post("http://localhost:4000/netConf", data=json_string)
        print(response.status_code)

if __name__ == '__main__':
    main()