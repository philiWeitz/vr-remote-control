//#define DEBUG


#include <Servo.h>

#define VERTICAL_PWM_PIN 2
#define HORIZONTAL_PWM_PIN 3

#define MOTOR_LEFT_FORWARD_PWM 4
#define MOTOR_LEFT_BACKWARDS_PWM 5
#define MOTOR_RIGHT_FORWARD_PWM 6
#define MOTOR_RIGHT_BACKWARDS_PWM 7

Servo verticalServo;
Servo horizontalServo;


void setup() {
  verticalServo.attach(VERTICAL_PWM_PIN);
  horizontalServo.attach(HORIZONTAL_PWM_PIN);

  verticalServo.write(90);
  horizontalServo.write(90);

  Serial.begin(9600);
}

void loop() {
  readInput();
}

void debugRotation() {
  rotateHead();
  delay(2000);
  rotateCar();
}

void rotateHead() {
  for(int i = 0; i < 180; ++i) {
    horizontalServo.write(i);
    verticalServo.write(min(70, i));
    delay(20);
  }
  
  for(int i = 180; i > 0; --i) {
    horizontalServo.write(i);
    verticalServo.write(min(70, 180-i));
    delay(20);
  }
  
  verticalServo.write(90);
  horizontalServo.write(90);
}

void rotateCar() {
  analogWrite(MOTOR_LEFT_FORWARD_PWM, 0);
  analogWrite(MOTOR_RIGHT_BACKWARDS_PWM, 0);
  analogWrite(MOTOR_LEFT_BACKWARDS_PWM, 200);
  analogWrite(MOTOR_RIGHT_FORWARD_PWM, 200);

  delay(2000);

  analogWrite(MOTOR_LEFT_BACKWARDS_PWM, 0);
  analogWrite(MOTOR_RIGHT_FORWARD_PWM, 0);
  analogWrite(MOTOR_LEFT_FORWARD_PWM, 200);
  analogWrite(MOTOR_RIGHT_BACKWARDS_PWM, 200);

  delay(2000);

  analogWrite(MOTOR_LEFT_FORWARD_PWM, 0);
  analogWrite(MOTOR_RIGHT_BACKWARDS_PWM, 0);
}

