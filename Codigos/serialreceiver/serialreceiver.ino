char buff[50];
volatile byte indx;
#define RXD2 16
#define TXD2 17


void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  Serial2.begin(9600, SERIAL_8N1, RXD2, TXD2);
}

void loop() {
  // put your main code here, to run repeatedly:
  if (Serial2.available() > 0){
    byte c = Serial2.read();
    if (indx < sizeof buff){
      buff[indx++] = c;
    }
    if (c == '\r'){
      Serial.print(buff);
      indx = 0;
    }
  }
}
