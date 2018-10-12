import { client as WebSocketClient } from 'websocket';
import config from './config';
import { setServoPWM } from './util/servo-util';
import { HeadPosition } from './model/head-position';
import { GPIO } from './enum/GpioPin';
import initGamepad from './gampad';

const client = new WebSocketClient();


client.on('connectFailed', (error) => {
  console.log(`Connect Error: ${error.toString()}`);
  reconnectToServer();
});

client.on('connect', (connection) => {
  console.log('WebSocket Client Connected');

  connection.on('error', (error) => {
    console.log(`Connection Error: ${error.toString()}`);
    reconnectToServer();
  });

  connection.on('close', () => {
    console.log('Connection Closed');
    reconnectToServer();
  });

  connection.on('message', (message) => {
    if (message.type === 'utf8' && message.utf8Data) {
      try {
        const value = <HeadPosition> JSON.parse(message.utf8Data);

        setServoPWM(value.vertical, GPIO.SERVO_VERTICAL);
        setServoPWM(value.horizontal, GPIO.SERVO_HORIZONTAL);

      } catch (e) {}
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
initGamepad();
