import { Button } from './enum/Button';
import { MAX_PWM_VALUE, setPWM } from './util/pwm-util';
import { GPIO } from './enum/GpioPin';

const gamepad = require('gamepad');


let maxSpeedPercentage = 0;
let leftRightDiffPercentage = 0;


function calculateAndSetPWM() {
  const maxPWM = Math.abs(MAX_PWM_VALUE * maxSpeedPercentage);

  const highValue = maxPWM;
  const lowValue = highValue - Math.abs(highValue * leftRightDiffPercentage);

  if (maxSpeedPercentage < -0.1) {
    // forward movement
    setPWM(0, GPIO.PWM_LEFT_BACKWARDS);
    setPWM(0, GPIO.PWM_RIGHT_BACKWARDS);

    // left movement
    if (leftRightDiffPercentage < 0) {
      setPWM(highValue, GPIO.PWM_LEFT_FORWARD);
      setPWM(lowValue, GPIO.PWM_RIGHT_FORWARD);

    } else if (leftRightDiffPercentage > 0) {
      setPWM(lowValue, GPIO.PWM_LEFT_FORWARD);
      setPWM(highValue, GPIO.PWM_RIGHT_FORWARD);
    }

  } else if (maxSpeedPercentage > 0.1) {
    // backwards movement
    setPWM(0, GPIO.PWM_LEFT_FORWARD);
    setPWM(0, GPIO.PWM_RIGHT_FORWARD);

    // left movement
    if (leftRightDiffPercentage < 0) {
      setPWM(highValue, GPIO.PWM_LEFT_BACKWARDS);
      setPWM(lowValue, GPIO.PWM_RIGHT_BACKWARDS);

    } else if (leftRightDiffPercentage > 0) {
      setPWM(lowValue, GPIO.PWM_LEFT_BACKWARDS);
      setPWM(highValue, GPIO.PWM_RIGHT_BACKWARDS);
    }

  } else {
    setPWM(0, GPIO.PWM_LEFT_BACKWARDS);
    setPWM(0, GPIO.PWM_RIGHT_BACKWARDS);
    setPWM(0, GPIO.PWM_LEFT_FORWARD);
    setPWM(0, GPIO.PWM_RIGHT_FORWARD);
  }

}

function initGamepad() {
  // Initialize the library
  gamepad.init();

  // Create a game loop and poll for events
  setInterval(gamepad.processEvents , 100);

  // Scan for new gamepads as a slower rate
  setInterval(gamepad.detectDevices , 2000);

  // Listen for move events on all gamepads
  gamepad.on('move' , (id , axis , value) => {

    if (axis === Button.LEFT_AXIS_VERTICAL) {
      // sets the max speed forwards
      // -1 => forward, 1 => backwards
      maxSpeedPercentage = value;
    }

    if (axis === Button.LEFT_AXIS_HORIZONTAL) {
      // sets the difference between left and right
      // -1 => left, 1 = right
      leftRightDiffPercentage = value;
    }

    calculateAndSetPWM();
    // console.log('move' , { id, axis, value });
  });


  // Listen for button up events on all gamepads
  gamepad.on('up' , (id , num) => {
    console.log('up' , { id , num });
  });

  // Listen for button down events on all gamepads
  gamepad.on('down' , (id , num) => {
    console.log('down' , { id , num });
  });
}

export default initGamepad;
