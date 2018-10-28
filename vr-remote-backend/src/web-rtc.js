import React from 'react';


import webSocketUtil from './util/web-socket-util';
import localWsClientUtil from './util/local-ws-client-util';

import api from './util/external-api-util';
import peerConnection from './util/peer-connection';


class WebRtcComponent extends React.Component {

  constructor(props) {
    super(props);

    const roomFromLocationHash = location.hash.substr(1,location.hash.length);

    // connect to local web socket
    localWsClientUtil.connect();

    // get the last connection parameter
    const localStorageState = JSON.parse(localStorage.getItem('vr-remote-state'));

    // disconnect from last room
    this.disconnectFromRoom(localStorageState).then(() => {
      // open the connection
      peerConnection.openPeerConnection(props.stream, this.onSignal, this.onClose);
    });

    this.state = {
      clientSdp: null,
      clientId: null,
      roomId: roomFromLocationHash || '987654321xxxx2',
      wssUrl: null,
      errorMessage: null,
      isInitiator: '',
    };
  }


  onSignal = (data) => {
    if (data.type === 'offer') {
      this.connectToRoom();
    }
  };


  onClose = () => {
    console.log('Peer connection closed');
    this.disconnectFromRoom(this.state).then(() => {
      setTimeout(() => peerConnection.reopenConnection(), 2000);
    });
  };


  disconnectFromRoom = (params) => {
    if (!params) {
      return Promise.resolve();
    }

    // close the web socket connection
    webSocketUtil.close();

    const { clientId, roomId } = params;

    // leave the room
    if (clientId && roomId) {
      return api.leaveRoom(roomId, clientId).then(() => {
        this.setState({ clientSdp: null });
        console.log(`Closed room ${roomId} (client: ${clientId})`);

      }).catch((error) => {
        this.setState({errorMessage: error.toString()});
      });
    }
    return Promise.reject();
  };


  registerWebSocket = () => {
    const { wssUrl, roomId, clientId } = this.state;

    // connect to web socket
    webSocketUtil.init(wssUrl);

    const wss = webSocketUtil.getWebSocket();

    wss.onopen = () => {
      wss.send(JSON.stringify({
        cmd: "register",
        roomid: roomId,
        clientid: clientId
      }));
    };

    wss.onmessage = (event) => {
      console.log('wss event triggered:', event);

      try {
        const data = JSON.parse(event.data);

        if (data.error) {
          console.error('wss error detected:', data.error);
          this.setState({ errorMessage: `wss - ${data.error}` });
          return;
        }

        const message = JSON.parse(data.msg);

        if (message.type === 'answer') {
          peerConnection.signal(message);
          this.setState({clientSdp: message})

        } else if(message.type === 'candidate') {
          const iceCandidate = new RTCIceCandidate(message);
          iceCandidate.sdpMid = message.id;
          iceCandidate.sdpMLineIndex = message.label;
          
          peerConnection.addIceCandidate(iceCandidate)
            .then(() => console.log('Remote candidate added'))
            .catch((e) => console.log('Error adding remote candidate', e));

        } else if (message.type === 'offer') {
          this.setState({errorMessage: 'Server is not the initializer!'});

        } else if (message.type === 'bye') {
          console.log('Client closed connection');
          peerConnection.closePeerConnection();
        }

      } catch(error) {
        console.error('Error reading wss message', error);
      }
    };
  };


  sendSDPOffer = () => {
    const { roomId, clientId } = this.state;

    return api.sendOffer(roomId, clientId, peerConnection.sdp)
      .then((response) => {
        console.log('SDP registered:', response);
        console.log('Register candidates...');

        Promise.all(peerConnection.candidates.map(item => {
          const { candidate } = item;

          candidate.type = 'candidate';
          candidate.id = candidate.sdpMid;
          candidate.label = candidate.sdpMLineIndex;

          return api.registerCandidate(roomId, clientId, candidate)
            .then(() => console.log('Registered candidate', candidate))
            .catch(e => console.error('Error registering candidate', e))
        })).then(() => {
          console.log('All candidates registered');
          this.setState({ errorMessage: 'All candidates registered' })
        })

    }).catch((error) => {
      this.setState({ errorMessage: error.toString() });
    });
  };


  connectToRoom = () => {
    const { roomId } = this.state;

    api.joinRoom(roomId).then((response) => {
      console.log('Room join result:', response.data);
      const { params } = response.data;

      if(response.data.result === 'SUCCESS') {
        const webRtcData = {
          clientId: params.client_id,
          roomId: params.room_id,
          wssUrl: params.wss_url,
          isInitiator: params.is_initiator,
        };
        this.setState(webRtcData);

        // save the new connection details to the local storage
        localStorage.setItem('vr-remote-state', JSON.stringify(webRtcData));

        // register the web socket client
        this.registerWebSocket();
        // send SDP offer
        this.sendSDPOffer();

      } else if (response.data.result === 'FULL') {
        this.setState({ errorMessage: 'Room is already full.' })
      }

    }).catch((error) => {
      this.setState({ errorMessage: error.toString() });
    });
  };


  onDisconnectFromRoomPress = () => {
    this.disconnectFromRoom(this.state);
    this.setState({ errorMessage: 'You are disconnected from the room!' });
  };


  renderSDP() {
    if(!peerConnection.sdp) {
      return (
        <div>
          Getting SDP...
        </div>
      )
    }
    return (
      <div style={{ marginBottom: '20px' }}>
        <p>Own SDP received:</p>
        <textarea
          onChange={() => {}}
          style={{ width: '99%', height: '200px' }}
          value={JSON.stringify(peerConnection.sdp)}
        />
      </div>
    )
  }


  renderClientSDP() {
    const { clientSdp } = this.state;

    if(!clientSdp) {
      return (
        <div style={{ marginBottom: '20px' }}>
          Waiting for client...
        </div>
      )
    }
    return (
      <div>
        <p>Client SDP received:</p>
        <textarea onChange={() => {}} style={{ width: '99%', height: '200px' }} value={JSON.stringify(clientSdp)}/>
      </div>
    )
  }


  renderRoom() {
    const { roomId } = this.state;
    return (
      <div style={{ marginBottom: '20px' }}>
        <div>Room</div>
        {roomId}
      </div>
    )
  }


  renderInitiator() {
    const { isInitiator } = this.state;
    return (
      <div style={{ marginBottom: '20px' }}>
        <div>Initiator</div>
        {isInitiator}
      </div>
    )
  }


  renderErrorMessage() {
    const { errorMessage } = this.state;
    if (errorMessage) {
      return (
        <div style={{ marginTop: '20px', color: 'red' }} >{errorMessage}</div>
      )
    }
    return null;
  }


  render() {
    return (
      <div>
        <p>Connecting...</p>

        {this.renderRoom()}
        {this.renderInitiator()}

        <button onClick={this.onDisconnectFromRoomPress}>
          Disconnect from room
        </button>

        {this.renderSDP()}
        {this.renderClientSDP()}
        {this.renderErrorMessage()}
      </div>
    )
  }
}

export default WebRtcComponent;
