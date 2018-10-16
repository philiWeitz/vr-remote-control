import * as express from 'express';
import * as http from 'http';
import * as WebSocket from 'ws';
import config from './config';
import { GPIO } from './enum/GpioPin';
import { HeadPosition } from './model/head-position';
import { setServoPWM } from './util/servo-util';
import initGamepad from './gampad';

const app = express();

const server = http.createServer(app);

const wsServer = new WebSocket.Server({ server });

wsServer.on('connection', (ws: WebSocket) => {

  ws.on('message', (message: string) => {
    try {
      const value = <HeadPosition> JSON.parse(message);

      setServoPWM(value.vertical, GPIO.SERVO_VERTICAL);
      setServoPWM(value.horizontal, GPIO.SERVO_HORIZONTAL);

    } catch (e) {
      console.warn(message);
    }
  });

  console.debug('Client connected');
});


// start the server
server.listen(config.WS_SERVER_PORT, () => {
  console.log(`Server started on port ${config.WS_SERVER_PORT}`);
});

initGamepad();
