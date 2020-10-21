char buff[50];
volatile byte indx;


void setup() {
  // put your setup code here, to run once:

  Serial.begin(115200);

}

void loop() {
  // put your main code here, to run repeatedly:
  if (Serial.available() > 0){
    byte c = Serial.read();
    if (indx < sizeof buff){
      buff[indx++] = c;
    }
    if (c == '\r'){
      Serial.print(buff);
      indx = 0;
    }
  }
}
