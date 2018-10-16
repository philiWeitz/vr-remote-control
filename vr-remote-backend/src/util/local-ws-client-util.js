
let ws = null;
let isConnected = false;

const localWsClientUtil = {

  connect: () => {
    if (ws) {
      return;
    }

    ws = new WebSocket('ws://localhost:8080');

    ws.onopen = () => {
      isConnected = true;
      ws.send('Ping Pong')
    };
  },

  sendMessage: (msg) => {
    if (isConnected) {
      ws.send(msg);
    }
  },

  getWebSocket: () => {
    return ws;
  },
};

export default localWsClientUtil;
