
void setMotorSpeed(uint8_t forwardPin, uint8_t backwardsPin, int speedValue, bool isLeft) {
  int pwmValue = (250 * min(100, abs(speedValue))) / 100;

  printDebug("Set motor speed PWM value ");
  printlnDebug(pwmValue);

  if (isLeft) {
    printDebug("Setting motor speed for left side: ");
    printlnDebug(pwmValue);
    analogWrite(MOTOR_LEFT_PWM_PIN, pwmValue);
  } else {   
    printDebug("Setting motor speed for right side: ");
    printlnDebug(pwmValue);
    analogWrite(MOTOR_RIGHT_PWM_PIN, pwmValue);
  }

  if (speedValue > 0) {
    digitalWrite(backwardsPin, LOW);
    digitalWrite(forwardPin, HIGH);
    printlnDebug("Driving forward");
    
  } else if  (speedValue < 0) {
    digitalWrite(forwardPin, LOW);
    digitalWrite(backwardsPin, HIGH);
    printlnDebug("Driving backwards");
    
  } else {
    digitalWrite(forwardPin, LOW);
    digitalWrite(backwardsPin, LOW);
    printlnDebug("Stopping");
  }
}
