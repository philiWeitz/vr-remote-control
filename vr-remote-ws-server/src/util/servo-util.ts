import { GPIO } from '../enum/GpioPin';


export function setServoPWM(pulseWidth: number, gpioPin: GPIO) {
  console.log(`Servo PWM ${pulseWidth} on ${gpioPin.toString()}`);
}


/*
 * For the Raspberry Client install the packages and uncomment the following code
 *
 * npm install pigpio @types/pigpio
 */

/*
const Gpio = require('pigpio').Gpio;

const pinMap = {
  [GPIO.SERVO_VERTICAL]: new Gpio(GPIO.SERVO_VERTICAL, { mode: Gpio.OUTPUT }),
  [GPIO.SERVO_HORIZONTAL]: new Gpio(GPIO.SERVO_HORIZONTAL, { mode: Gpio.OUTPUT }),
};


export function setServoPWM(pulseWidth: number, gpioPin: GPIO) {
  const pin = pinMap[gpioPin];
  if (pin) {
    pin.servoWrite(pulseWidth);
  }
}
*/
