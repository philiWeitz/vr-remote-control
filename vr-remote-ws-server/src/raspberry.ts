import { GPIO } from './enum/GpioPin';


export function setPWM(pulseWidth: number, gpioPin: GPIO) {
  console.log(`${pulseWidth} on ${gpioPin.toString()}`);
}


/*
 * For the Raspberry Client install the packages and uncomment the following code
 *
 * npm install pigpio @types/pigpio
 */

/*
const Gpio = require('pigpio').Gpio;

const pinMap = {
  [GPIO.VERTICAL]: new Gpio(GPIO.VERTICAL, {mode: Gpio.OUTPUT}),
  [GPIO.HORIZONTAL]: new Gpio(GPIO.HORIZONTAL, {mode: Gpio.OUTPUT}),
};


export function setPWM(pulseWidth: number, gpioPin: GPIO) {
  const pin = pinMap[gpioPin];
  if (pin) {
    pin.servoWrite(pulseWidth);
  }
}
*/
