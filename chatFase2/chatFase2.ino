#include <SPI.h>  
#include "RF24.h" 

RF24 myRadio (0, 5); 
byte addresses[][6] = {"0"};

struct package {
  char  text[300] ="";
}; 

typedef struct package Package;
Package data;
Package dataTransmit;

void setup() {
  Serial.begin(115200);
  delay(1000);

  myRadio.begin(); 
  myRadio.setChannel(115); 
  myRadio.setPALevel(RF24_PA_MAX);
  myRadio.setDataRate( RF24_250KBPS );
  
  myRadio.openReadingPipe(1, addresses[0]);
  myRadio.startListening();
}


void loop()  {
  //LER TRF
  if ( myRadio.available()) {
    while (myRadio.available()){
      myRadio.read( &data, sizeof(data) );
    }
    Serial.print(data.text);
  }


  delay(2000);
  myRadio.stopListening();
  
  //LER e enviar da consola
  memset(dataTransmit.text, 0, sizeof(dataTransmit.text));

  char inData[300];
  int index = 0;
  bool aux = false;
  while(Serial.available() > 0){
    if(index < 500){
      inData[index] = Serial.read();
      index++;
      inData[index] = '\0';
      sprintf(dataTransmit.text, "%s", inData);
      aux = true;
    }
  }
  if(aux){
      Serial.print("Eu: ");
      Serial.print(dataTransmit.text);
  }
  myRadio.openWritingPipe(addresses[0]);
  myRadio.write(&dataTransmit, sizeof(dataTransmit));
  myRadio.openReadingPipe(1, addresses[0]);
  myRadio.startListening(); 
  
}
