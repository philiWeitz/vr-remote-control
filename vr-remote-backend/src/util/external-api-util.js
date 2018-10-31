import axios from 'axios';


// const CORS_PROXY = 'https://cors-anywhere.herokuapp.com';
const CORS_PROXY = 'https://vr-remote-control-cors-proxy.herokuapp.com';


const api = {

  joinRoom: (roomId) =>
    axios.post(`${CORS_PROXY}/https://appr.tc/join/${roomId}`),

  leaveRoom: (roomId, clientId) =>
    axios.post(`${CORS_PROXY}/https://appr.tc/leave/${roomId}/${clientId}`),

  deleteWebSocketConnection: (roomId, clientId) =>
    axios.delete(`${CORS_PROXY}/https://apprtc-ws.webrtc.org:443/${roomId}/${clientId}`),

  sendOffer: (roomId, clientId, sdp) =>
    axios.post(`${CORS_PROXY}/https://appr.tc/message/${roomId}/${clientId}`, sdp),

  registerCandidate: (roomId, clientId, candidate) =>
    axios.post(`${CORS_PROXY}/https://appr.tc/message/${roomId}/${clientId}`, candidate),

};

export default api;
