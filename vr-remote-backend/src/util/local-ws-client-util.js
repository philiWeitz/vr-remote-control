
class LocalWebSocketClient {

  constructor() {
    this.ws = null;
    this.isConnected = false;
  }

  connect() {
    if (this.ws) {
      return;
    }

    // always connect to local backend server
    this.ws = new WebSocket('ws://localhost:8080');

    this.ws.onopen = () => {
      this.isConnected = true;
      this.ws.send('Ping Pong')
    };
  }

  sendMessage(msg) {
    if (this.isConnected) {
      this.ws.send(msg);
    } else {
      console.warn('Unable to send message - locale WS not connected', msg);
    }
  }

  moveToDefaultPosition() {
    const msg = JSON.stringify({ vertical: 0, horizontal: 0 });
    this.sendMessage(msg);
  }

  moveToMaxPosition() {
    const msg = JSON.stringify({ vertical: 0, horizontal: 4000 });
    this.sendMessage(msg);
  }

  getWebSocket() {
    return this.ws;
  }
}


export default new LocalWebSocketClient();
