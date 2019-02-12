//#define DEBUG

// baud rate is set to 4 (115200)
// default is 0 (9600)

#include <Servo.h>

#define VERTICAL_PWM_PIN 3
#define HORIZONTAL_PWM_PIN 5

#define MOTOR_LEFT_PWM_PIN 6
#define MOTOR_LEFT_FORWARD 7
#define MOTOR_LEFT_BACKWARDS 8

#define MOTOR_RIGHT_FORWARD 9
#define MOTOR_RIGHT_BACKWARDS 10
#define MOTOR_RIGHT_PWM_PIN 11

Servo verticalServo;
Servo horizontalServo;


void setup() {
  pinMode(VERTICAL_PWM_PIN, OUTPUT); 
  pinMode(HORIZONTAL_PWM_PIN, OUTPUT); 
  
  pinMode(MOTOR_LEFT_FORWARD, OUTPUT); 
  pinMode(MOTOR_LEFT_BACKWARDS, OUTPUT); 
  pinMode(MOTOR_RIGHT_FORWARD, OUTPUT); 
  pinMode(MOTOR_RIGHT_BACKWARDS, OUTPUT); 
  pinMode(MOTOR_LEFT_PWM_PIN, OUTPUT); 
  pinMode(MOTOR_RIGHT_PWM_PIN, OUTPUT); 

  digitalWrite(MOTOR_LEFT_FORWARD, LOW);
  digitalWrite(MOTOR_LEFT_BACKWARDS, LOW);
  digitalWrite(MOTOR_RIGHT_FORWARD, LOW);
  digitalWrite(MOTOR_RIGHT_BACKWARDS, LOW);
  analogWrite(MOTOR_LEFT_PWM_PIN, 0);
  analogWrite(MOTOR_RIGHT_PWM_PIN, 0);
 
  verticalServo.attach(VERTICAL_PWM_PIN);
  horizontalServo.attach(HORIZONTAL_PWM_PIN);

  verticalServo.write(90);
  horizontalServo.write(90);

  Serial.begin(9600);
  //Serial.println("AT+BAUD4");
  //Serial.begin(115200);
}

void loop() {
  readInput();
}

void debugRotation() {
  rotateHead();
  rotateCar();
}

void rotateHead() {
  for(int i = 10; i < 170; ++i) {
    horizontalServo.write(i);
    verticalServo.write(min(120, i));
    delay(20);
  }
  
  for(int i = 170; i > 10; --i) {
    horizontalServo.write(i);
    verticalServo.write(min(120, 180-i));
    delay(20);
  }
  
  verticalServo.write(90);
  horizontalServo.write(90);
}

void rotateCar() {
  delay(2000);
  
  analogWrite(MOTOR_LEFT_PWM_PIN, 250);
  analogWrite(MOTOR_RIGHT_PWM_PIN, 250);
  
  digitalWrite(MOTOR_LEFT_FORWARD, HIGH);
  digitalWrite(MOTOR_LEFT_BACKWARDS, LOW);
  digitalWrite(MOTOR_RIGHT_FORWARD, LOW);
  digitalWrite(MOTOR_RIGHT_BACKWARDS, HIGH);

  delay(2000);
  digitalWrite(MOTOR_LEFT_FORWARD, LOW);
  digitalWrite(MOTOR_LEFT_BACKWARDS, HIGH);
  digitalWrite(MOTOR_RIGHT_FORWARD, HIGH);
  digitalWrite(MOTOR_RIGHT_BACKWARDS, LOW);

  delay(2000);

  digitalWrite(MOTOR_LEFT_BACKWARDS, LOW);
  digitalWrite(MOTOR_RIGHT_FORWARD, LOW);
  analogWrite(MOTOR_LEFT_PWM_PIN, 0);
  analogWrite(MOTOR_RIGHT_PWM_PIN, 0);
}

