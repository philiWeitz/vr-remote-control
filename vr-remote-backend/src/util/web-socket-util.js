
let wss = null;


const webSocketUtil = {

  init: (url) => {
    webSocketUtil.close();
    wss = new WebSocket(url);
    console.log('WebRTC websocket opened')
  },

  close: () => {
    if (wss) {
      wss.close(1000, 'remote closed');
      wss = null;
      console.log('WebRTC websocket closed')
    }
  },

  getWebSocket: () => {
    return wss;
  }

};

export default webSocketUtil;
