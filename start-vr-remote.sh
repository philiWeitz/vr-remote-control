#!/bin/bash

sleep 5

sudo modprobe bcm2835-v4l2

###### start backend
cd vr-remote-ws-server
./bin/start-server.sh &
cd -

# wait for backend to start up
until nc -z localhost 8080; do
  echo "Backend is not ready yet"
  sleep 1
done
echo "-> Backend ready"

sleep 2


###### start frontend
node vr-remote-backend/server.js &
until nc -z localhost 3000; do
  echo "Frontend is not ready yet"
  sleep 1
done
echo "-> Frontend ready"

sleep 2

chromium-browser "http://localhost:3000?roomId=dafasdfadfawwe&width=352&height=288"