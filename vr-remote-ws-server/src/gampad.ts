import { setPWM } from './util/pwm-util';
import { GPIO } from './enum/GpioPin';

const gamepad = require('gamepad');

function initGamepad() {
  // Initialize the library
  gamepad.init();

  // Create a game loop and poll for events
  setInterval(gamepad.processEvents , 100);

  // Scan for new gamepads as a slower rate
  setInterval(gamepad.detectDevices , 2000);

  // Listen for move events on all gamepads
  gamepad.on('move' , (id , axis , value) => {
    console.log('move' , { id, axis, value });
  });

  // Listen for button up events on all gamepads
  gamepad.on('up' , (id , num) => {
    console.log('up' , { id , num });

    setPWM(100, GPIO.PWM_LEFT_FORWARD);
    setPWM(100, GPIO.PWM_RIGHT_FORWARD);
  });

  // Listen for button down events on all gamepads
  gamepad.on('down' , (id , num) => {
    console.log('down' , { id , num });

    setPWM(0, GPIO.PWM_LEFT_FORWARD);
    setPWM(0, GPIO.PWM_RIGHT_FORWARD);
  });
}

export default initGamepad;
