import React from 'react';
import axios from 'axios';

import webSocketUtil from './util/web-socket-util';
import localWsClientUtil from './util/local-ws-client-util';
import config from './util/simple-peer-config';


const Peer = require('simple-peer');

const CORS_PROXY = 'https://cors-anywhere.herokuapp.com';
//const CORS_PROXY = 'https://vr-remote-control-cors-proxy.herokuapp.com';

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
      this.setState({
        peerConnection: this.getPeerConnection(this.props.stream),
      })
    });

    this.state = {
      peerConnection: null,
      sdp: null,
      clientSdp: null,
      clientId: null,
      roomId: roomFromLocationHash || '987654321xxxx2',
      wssUrl: null,
      errorMessage: null,
      isInitiator: '',
      candidates: [],
    };
  }


  getPeerConnection(stream) {
    const peerConnection = new Peer({ initiator: true, trickle: true, stream, reconnectTimer: 1000, config });

    peerConnection.on('error', (err) => {
      console.log('error', err);
    });

    peerConnection.on('signal', (data) => {
      const { candidates } = this.state;

      if (data.type === 'offer') {
        this.setState({ sdp: data });
        this.connectToRoom();

      } else if (data.candidate) {
        candidates.push(data);
        this.setState({ candidates });
        console.log(data.candidate);
      }
    });

    peerConnection.on('close', (data) => {
      console.log('Peer connection closed');
      this.disconnectFromRoom(this.state);
      this.setState({ peerConnection: this.getPeerConnection(this.props.stream), candidates: [] });
    });

    peerConnection.on('data', (data) => {
      const message = String.fromCharCode.apply(null, data);
      localWsClientUtil.sendMessage(message);
    });

    return peerConnection;
  }


  disconnectFromRoom = (params) => {
    if (!params) {
      return Promise.resolve();
    }

    const { clientId, roomId } = params;
    webSocketUtil.close();

    if (clientId && roomId) {
      return axios.post(`${CORS_PROXY}/https://appr.tc/leave/${roomId}/${clientId}`).then(() => {
        console.log(`Closed room ${roomId} (client: ${clientId})`);
        this.setState({ clientSdp: null, candidates: [] });

      }).catch((error) => {
        this.setState({errorMessage: error.toString(), candidates: []});
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
        const { peerConnection } = this.state;

        if (message.type === 'answer') {
          peerConnection.signal(message);
          this.setState({clientSdp: message})
        } else if(message.type === 'candidate') {
          const iceCandidate = new RTCIceCandidate(message);
          iceCandidate.sdpMid = message.id;
          iceCandidate.sdpMLineIndex = message.label;
          
          peerConnection._pc.addIceCandidate(iceCandidate)
            .then(() => console.log('Remote candidate added'))
            .catch((e) => console.log('Error adding remote candidate', e));
        }

        if (message.type === 'offer') {
          this.setState({errorMessage: 'Server is not the initializer!'});
        } else if (message.type === 'bye') {
          console.log('Client closed connection');

          peerConnection.destroy();
        }

      } catch(error) {
        console.error('Error reading wss message', error);
      }
    };
  };


  sendSDPOffer = () => {
    const { sdp, roomId, clientId, candidates } = this.state;

    return axios.post(`${CORS_PROXY}/https://appr.tc/message/${roomId}/${clientId}`, sdp)
      .then((response) => {
        console.log('SDP registered:', response);
        console.log('Register candidates...');

        Promise.all(candidates.map(item => {
          const { candidate } = item;

          candidate.type = 'candidate';
          candidate.id = candidate.sdpMid;
          candidate.label = candidate.sdpMLineIndex;

          return axios.post(`${CORS_PROXY}/https://appr.tc/message/${roomId}/${clientId}`, candidate)
            .then(() => console.log('Registered candidate', candidate))
            .catch(e => console.error('Error registering candidate', e))
        })).then(() => console.log('All candidates registered'))
    }).catch((error) => {
      this.setState({ errorMessage: error.toString() });
    });
  };


  connectToRoom = () => {
    const { roomId } = this.state;

    axios.post(`${CORS_PROXY}/https://appr.tc/join/${roomId}`, {
      headers: {
        'Origin': 'null',
      },
    }).then((response) => {
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


  handleChange = (event) => {
    this.setState({sdp: JSON.parse(event.target.value)});
  };


  renderSDP() {
    const { sdp } = this.state;

    if(!sdp) {
      return (
        <div>
          Getting SDP...
        </div>
      )
    }
    return (
      <div style={{ marginBottom: '20px' }}>
        <p>Own SDP received:</p>
        <textarea onChange={this.handleChange} style={{ width: '99%', height: '200px' }} value={JSON.stringify(sdp)}/>
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
