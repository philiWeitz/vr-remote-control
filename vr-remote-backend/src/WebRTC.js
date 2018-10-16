import React from 'react';
import axios from 'axios';

import webSocketUtil from './util/web-socket-util';
import localWsClientUtil from './util/local-ws-client-util';


const Peer = require('simple-peer');


class WebRtcComponent extends React.Component {

  constructor(props) {
    super(props);

    // connect to local web socket
    localWsClientUtil.connect();

    this.state = {
      peerConnection: this.getPeerConnection(this.props.stream),
      sdp: null,
      clientId: null,
      roomId: '123456789abc',
      wssUrl: null,
      errorMessage: null
    };
  }


  getPeerConnection(stream) {
    const peerConnection = new Peer({ initiator: true, trickle: false, stream, reconnectTimer: 1000 });

    peerConnection.on('error', (err) => {
      console.log('error', err)
    });

    peerConnection.on('signal', (data) => {
      this.setState({ sdp: data });

      try {
        // get the last connection parameter
        const localStorageState = JSON.parse(localStorage.getItem('vr-remote-state'));

        // disconnect from last room
        this.disconnectFromRoom(localStorageState).then(() => {
          // connect to the room using the SDP
          this.connectToRoom();
        });
      } catch(e) {
        console.error(e);
      }
    });

    peerConnection.on('close', (data) => {
      console.log(data);
      this.setState({ peerConnection: this.getPeerConnection(this.props.stream) });
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
      return axios.post(`https://cors-anywhere.herokuapp.com/https://appr.tc/leave/${roomId}/${clientId}`).then(() => {
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
      const data = JSON.parse(event.data);
      const message = JSON.parse(data.msg);

      if (message.type === 'answer') {
        this.state.peerConnection.signal(message);
      } else if (message.type === 'bye') {
        console.log('Client closed connection');

        this.state.peerConnection.destroy();
      }
      console.log(data);
    };
  };


  sendSDPOffer = () => {
    const { sdp, roomId, clientId } = this.state;

    return axios.post(`https://cors-anywhere.herokuapp.com/https://appr.tc/message/${roomId}/${clientId}`,
      sdp).then((response) => {

      console.log('SDP registered:', response);
    }).catch((error) => {
      this.setState({ errorMessage: error.toString() });
    });
  };


  connectToRoom = () => {
    const { roomId } = this.state;

    axios.post(`https://cors-anywhere.herokuapp.com/https://appr.tc/join/${roomId}`, {
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
        };
        this.setState(webRtcData);

        // save the new connection details to the local storage
        localStorage.setItem('vr-remote-state', JSON.stringify(webRtcData));

        // register the web socket client
        this.registerWebSocket();
        // send SDP offer
        this.sendSDPOffer().then(() => {
          // disconnect immediately
          //this.disconnectFromRoom(this.state);
        });

      } else if (response.data.result === 'FULL') {
        this.setState({ errorMessage: 'Room is already full.' })
      }

    }).catch((error) => {
      this.setState({ errorMessage: error.toString() });
    });
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
      <div>
        <p>SDP received:</p>
        <div>{JSON.stringify(sdp)}</div>
      </div>
    )
  }


  renderErrorMessage() {
    const { errorMessage } = this.state;
    if (errorMessage) {
      return (
        <div>{errorMessage}</div>
      )
    }
    return null;
  }


  render() {
    return (
      <div>
        <p>Connecting...</p>
        {this.renderSDP()}
        {this.renderErrorMessage()}
      </div>
    )
  }
}

export default WebRtcComponent;
