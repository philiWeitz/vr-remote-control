

void printlnDebug(String msg) {
  #ifdef DEBUG
  Serial.println(msg);
  #endif
}

void printlnDebug(int msg) {
  #ifdef DEBUG
  Serial.println(msg);
  #endif
}

void printDebug(String msg) {
  #ifdef DEBUG
  Serial.print(msg);
  #endif
}

