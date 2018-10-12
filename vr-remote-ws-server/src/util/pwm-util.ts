import { GPIO } from '../enum/GpioPin';


const MAX_PWM_VALUE = 100;

function limitPwmValue(pulseWidth: number): number {
  return Math.max(0, Math.min(MAX_PWM_VALUE, pulseWidth));
}

export function setPWMDecimalPercentage(decimalPercentage: number, gpioPin: GPIO) {
  const pwmValue = MAX_PWM_VALUE * decimalPercentage;
  setPWM(pwmValue, gpioPin);
}


/*
 * On the Raspberry Client, comment the following code
 *
 * npm install pigpio @types/pigpio
 */

export function setPWM(pulseWidth: number, gpioPin: GPIO) {
  const pwmValueLimited = limitPwmValue(pulseWidth);
  console.log(`PWM ${pwmValueLimited} on ${gpioPin.toString()}`);
}


/*
 * For the Raspberry Client install the packages and uncomment the following code
 *
 * npm install pigpio @types/pigpio
 */


/*
const Gpio = require('pigpio').Gpio;

const pinMap = {
  [GPIO.PWM_LEFT_BACKWARDS]: new Gpio(GPIO.PWM_LEFT_BACKWARDS, { mode: Gpio.OUTPUT }),
  [GPIO.PWM_LEFT_FORWARD]: new Gpio(GPIO.PWM_LEFT_FORWARD, { mode: Gpio.OUTPUT }),
  [GPIO.PWM_RIGHT_BACKWARDS]: new Gpio(GPIO.PWM_RIGHT_BACKWARDS, { mode: Gpio.OUTPUT }),
  [GPIO.PWM_RIGHT_FORWARD]: new Gpio(GPIO.PWM_RIGHT_FORWARD, { mode: Gpio.OUTPUT }),
};

export function setPWM(pulseWidth: number, gpioPin: GPIO) {
  const pin = pinMap[gpioPin];
  if (pin) {
    const pwmValueLimited = limitPwmValue(pulseWidth);
    pin.servoWrite(pwmValueLimited);
  }
}
*/
