#define DEBUG

#include <Servo.h>

#define VERTICAL_PWM_PIN 3
#define HORIZONTAL_PWM_PIN 9

Servo verticalServo;
Servo horizontalServo;


void setup() {
  verticalServo.attach(VERTICAL_PWM_PIN);
  horizontalServo.attach(HORIZONTAL_PWM_PIN);

  verticalServo.write(0);
  horizontalServo.write(0);

  #ifdef DEBUG
  Serial.begin(9600);
  #endif
}

void loop() {
  readInput();
}
