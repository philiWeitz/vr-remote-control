#!/bin/bash


###### start backend
cd vr-remote-ws-server
xterm -e ./bin/start-server.sh &
cd -

# wait for backend to start up
until nc -z localhost 8080; do
  echo "Backend is not ready yet"
  sleep 1
done
echo "-> Backend ready"

sleep 2


###### start frontend
xterm -e node vr-remote-backend/server.js &
until nc -z localhost 3000; do
  echo "Frontend is not ready yet"
  sleep 1
done
echo "-> Frontend ready"

sleep 2