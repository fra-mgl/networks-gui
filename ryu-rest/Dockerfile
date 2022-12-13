FROM ubuntu:20.04
RUN apt-get update && apt-get install build-essential nano -y
RUN apt-get install python3 pip -y
RUN apt-get install python3.8-venv -y
WORKDIR /usr/ryu-app
RUN python3 -m venv .venv
RUN . .venv/bin/activate
COPY requirement.txt ./
RUN pip install --no-cache-dir -r requirement.txt
COPY . .
CMD ryu-manager --observe-links app.py