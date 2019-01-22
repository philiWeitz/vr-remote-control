
#define INPUT_BUFFER_SIZE 40

#define INPUT_END_CHAR '$'
#define INPUT_DELIMITER ","


int inputBufferPtr = 0;
char inputBuffer[INPUT_BUFFER_SIZE];


/*
 * Example Serial commands:
 * 
 * - HEAD,90,90$
 */

void readInput() {

  while (Serial.available() > 0) {
    char inputChar = Serial.read();

    // input end character found -> execute command
    if(inputChar == INPUT_END_CHAR) {
      inputBuffer[inputBufferPtr] = '\0';
      // Serial.println(inputBuffer);
      parseInputBuffer();
      resetBuffer();
      
    // add char to input buffer
    } else {
      inputBuffer[inputBufferPtr] = inputChar;
      ++inputBufferPtr;
    } 

    // buffer overflow -> reset input buffer
    if(inputBufferPtr >= INPUT_BUFFER_SIZE) {
      resetBuffer();
    }   
  }
}


void parseInputBuffer() {
  char* subStr = strtok(inputBuffer, INPUT_DELIMITER);

  if(!subStr) {
    return;
  }
  if (strcmp(subStr, "HEAD") == 0) {
    parseHeadPosition();
  } else if (strcmp(subStr, "DEMO") == 0) {
    debugRotation();
  }
}


void resetBuffer() {
  inputBufferPtr = 0;
  memcpy(inputBuffer, "0", INPUT_BUFFER_SIZE);
}


void parseHeadPosition() {
  char* subStr = strtok (NULL, INPUT_DELIMITER);
  if(!subStr) { return; }
  // we need to invert the direction because of the construction
  uint32_t horizontalPwmValue = max(0, min(180, atoi(subStr)));
  horizontalServo.write(horizontalPwmValue);

  subStr = strtok (NULL, INPUT_DELIMITER);
  if(!subStr) { return; }
  uint32_t verticalPwmValue = max(70, min(170, atoi(subStr) + 10));
  verticalPwmValue = 180 - verticalPwmValue;
  verticalServo.write(verticalPwmValue);

  #ifdef DEBUG
  Serial.print("Write PWM to horizontal servo: ");
  Serial.println(horizontalPwmValue);
  Serial.print("Write PWM to vertical servo: ");
  Serial.println(verticalPwmValue);
  #endif
}

