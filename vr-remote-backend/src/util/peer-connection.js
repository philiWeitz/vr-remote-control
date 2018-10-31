import Peer from 'simple-peer';
import localWsClientUtil from './local-ws-client-util';


// see: https://gist.github.com/sagivo/3a4b2f2c7ac6e1b5267c2f1f59ac6c6b
const peerConnectionConfig = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
  ]
};


class PeerConnection {

  sdp = null;
  candidates = [];
  peerConnection = null;
  initiator = true;

  stream = null;
  onSignal = null;
  onClose = null;


  signal(data) {
    if (this.peerConnection) {
      this.peerConnection.signal(data);
    }
  }

  addIceCandidate(candidate) {
    if (this.peerConnection) {
      return this.peerConnection._pc.addIceCandidate(candidate);
    }
    return Promise.resolve();
  }

  closePeerConnection() {
    if (this.peerConnection) {
      this.peerConnection.destroy();
      this.peerConnection = null;
      this.candidates = [];
      this.sdp = null;
    }
  }

  reopenConnection() {
    return this.openPeerConnection(this.stream, this.onSignal, this.onClose);
  }

  openPeerConnection(stream, onSignal, onClose, initiator = true) {
    this.stream = stream;
    this.onSignal = onSignal;
    this.onClose = onClose;
    this.initiator = initiator;

    // close old peer connection first
    if (this.peerConnection) {
      this.closePeerConnection();
    }

    this.peerConnection = new Peer({
      initiator,
      trickle: true,
      stream,
      reconnectTimer: 4000,
      peerConnectionConfig,
    });

    this.peerConnection.on('error', (err) => {
      console.log('Peer connection error:', err);
    });

    this.peerConnection.on('signal', (data) => {
      onSignal(data);

      if (data.type === 'offer') {
        this.sdp = data;
      }

      if (data.candidate) {
        // send the candidate
        this.candidates.push(data);
        console.log(data.candidate);
      }
    });

    this.peerConnection.on('close', (data) => {
      onClose(data);
      this.closePeerConnection();
    });

    this.peerConnection.on('data', (data) => {
      const message = String.fromCharCode.apply(null, data);
      localWsClientUtil.sendMessage(message);
    });
  }
}

export default new PeerConnection();
