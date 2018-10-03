import { client as WebSocketClient } from 'websocket';
import config from './config';
import { setPWM } from './raspberry';
import { HeadPosition } from './model/head-position';
import { GPIO } from './enum/GpioPin';

const client = new WebSocketClient();


client.on('connectFailed', function(error) {
  console.log('Connect Error: ' + error.toString());
  reconnectToServer();
});

client.on('connect', function(connection) {
  console.log('WebSocket Client Connected');

  connection.on('error', function(error) {
    console.log("Connection Error: " + error.toString());
    reconnectToServer();
  });

  connection.on('close', function() {
    console.log('Connection Closed');
    reconnectToServer();
  });

  connection.on('message', function(message) {
    if (message.type === 'utf8' && message.utf8Data) {
      try {
        const value = <HeadPosition> JSON.parse(message.utf8Data);

        setPWM(value.vertical, GPIO.VERTICAL);
        setPWM(value.horizontal, GPIO.HORIZONTAL);

      } catch(e) {}
    }
  });

  connection.send('subscribe');
});

function reconnectToServer() {
  setTimeout(() => connectToServer(), 4000);
}

function connectToServer() {
  client.connect(`${config.WS_SERVER_HOST}:${config.WS_SERVER_PORT}`);
}

connectToServer();
