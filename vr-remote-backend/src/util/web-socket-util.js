import api from './external-api-util';

class WebRtcWebSocketClient {

  roomId = null;
  clientId = null;

  constructor() {
    this.wss = null;
  }

  init(url, roomId, clientId) {
    this.roomId = roomId;
    this.clientId = clientId;

    this.close();
    this.wss = new WebSocket(url);

    console.log('WebRTC websocket opened')
  }

  close() {
    if (this.wss) {
      this.wss.send(JSON.stringify({ cmd: "send", msg: "{\"type\": \"bye\"}" }));
      api.deleteWebSocketConnection(this.roomId, this.clientId);

      this.wss.close();
      this.wss = null;

      console.log('WebRTC websocket closed')
    }
  }

  getWebSocket() {
    return this.wss;
  }
}

export default new WebRtcWebSocketClient();
