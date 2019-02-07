
void setMotorSpeed(uint8_t forwardPin, uint8_t backwardsPin, int speedValue) {
  int pwmValue = (250 * min(100, abs(speedValue))) / 100;

  printDebug("Set motor speed PWM value ");
  printlnDebug(pwmValue);
  
  if (speedValue > 0) {
    analogWrite(backwardsPin, 0);
    analogWrite(forwardPin, pwmValue);
    printlnDebug("Driving forward");
    
  } else if  (speedValue < 0) {
    analogWrite(forwardPin, 0);
    analogWrite(backwardsPin, pwmValue);
    printlnDebug("Driving backwards");
    
  } else {
    analogWrite(forwardPin, 0);
    analogWrite(backwardsPin, 0);
    printlnDebug("Stopping");
  }
}
