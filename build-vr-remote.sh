#!/bin/bash


###### backend
cd vr-remote-ws-server
npm run build
cd -


###### frontend
cd vr-remote-backend
npm run build
cd -