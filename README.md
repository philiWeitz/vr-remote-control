
# Raspberry PI autostart script
sudo nano /home/pi/.config/lxsession/LXDE-pi/autostart
@sh <path to script>

# Camera remote control via Mobile VR

This project tries to create remote controlled WebCam which can be
controlled via Mobile VR. It includes a simple websocket server, an
Android WebRTC project and a Unity project.

# Installation
TODO: instructions will follow soon

# Sample communication with AppRTC Signalling Server being the initiator

 Send POST request to:
```
POST https://appr.tc/join/{{roomId}}
```

**Connect to the Websocket server via "wss_url" provided in the response:**
```
wss://apprtc-ws.webrtc.org:443/ws
```

**Send the register command via WS using the "room_id" and "client_id" provided in the response:**
```
{"cmd":"register","roomid":"{{room_id}}","clientid":"{{client_id}}"}
```

**Send a SDP offer**
```
POST https://appr.tc/message/{{roomId}}/{{clientId}}
```

```
BODY

{
    "sdp": "<your valid SDP>",
    "type": "offer"
}
```

**Wait for a client to connect. When the client connects to the room, its SDP candidates will be send via WS**

**Extract the client SDP and use it to connect - DONE**

**Disconnect from the room via**
```
POST https://appr.tc/leave/{{room_d}}/{{client_id}}
```


## Thanks to the following repositories
- https://gist.github.com/JBurkeKF/897834b7c374e5c32dabe206896c1cb0
- https://github.com/androidthings/sample-videoRTC/tree/master/app/src/main