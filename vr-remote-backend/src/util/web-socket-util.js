
class WebRtcWebSocketClient {

  constructor() {
    this.wss = null;
  }

  init(url) {
    this.close();
    this.wss = new WebSocket(url);

    console.log('WebRTC websocket opened')
  }

  close() {
    if (this.wss) {
      this.wss.close(1000, 'remote closed');
      this.wss = null;

      console.log('WebRTC websocket closed')
    }
  }

  getWebSocket() {
    return this.wss;
  }
}

export default new WebRtcWebSocketClient();
