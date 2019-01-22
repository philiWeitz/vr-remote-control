//#define DEBUG


#include <Servo.h>

#define VERTICAL_PWM_PIN 3
#define HORIZONTAL_PWM_PIN 5

Servo verticalServo;
Servo horizontalServo;


void setup() {
  verticalServo.attach(VERTICAL_PWM_PIN);
  horizontalServo.attach(HORIZONTAL_PWM_PIN);

  verticalServo.write(90);
  horizontalServo.write(80);

  Serial.begin(9600);
}

void loop() {
  readInput();
  //debugRotation();
}

void debugRotation() {
  for(int i = 0; i < 180; ++i) {
    horizontalServo.write(i);
    verticalServo.write(min(120, i));
    delay(20);
  }
  for(int i = 180; i > 0; --i) {
    horizontalServo.write(i);
    verticalServo.write(min(120, 180-i));
    delay(20);
  }

  verticalServo.write(90);
  horizontalServo.write(80);
}

