
#define INPUT_BUFFER_SIZE 40

#define INPUT_END_CHAR '$'
#define INPUT_DELIMITER ","


int inputBufferPtr = 0;
char inputBuffer[INPUT_BUFFER_SIZE];


/*
 * Example Serial commands:
 * 
 * - HORIZONTAL,100$
 * - HORIZONTAL,1020$
 * - VERTICAL,100$
 */

void readInput() {

  while (Serial.available() > 0) {
    char inputChar = Serial.read();

    // input end character found -> execute command
    if(inputChar == INPUT_END_CHAR) {
      inputBuffer[inputBufferPtr] = '\0';
      Serial.println(inputBuffer);
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
  if (strcmp(subStr, "HORIZONTAL") == 0) {
    parseHorizontal();
  } else if (strcmp(subStr, "VERTICAL") == 0) {
    parseVertical();
  }
}


void resetBuffer() {
  inputBufferPtr = 0;
  memcpy(inputBuffer, "0", INPUT_BUFFER_SIZE);
}


void parseHorizontal() {
  char* subStr = strtok (NULL, INPUT_DELIMITER);
  if(!subStr) { return; }

  uint32_t pwmValue = min(1023, atoi(subStr));
  horizontalServo.write(pwmValue);

  #ifdef DEBUG
  Serial.print("Write PWM to horizontal servo: ");
  Serial.println(pwmValue);
  #endif
}


void parseVertical() {
  char* subStr = strtok (NULL, INPUT_DELIMITER);
  if(!subStr) { return; }

  uint32_t pwmValue = min(1023, atoi(subStr));
  verticalServo.write(pwmValue);

  #ifdef DEBUG
  Serial.print("Write PWM to vertical servo: ");
  Serial.println(pwmValue);
  #endif
}

