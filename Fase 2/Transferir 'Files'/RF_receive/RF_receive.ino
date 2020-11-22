#include <SPI.h>  
#include "RF24.h" 

RF24 myRadio (0, 5); 
byte addresses[][6] = {"0"};

struct package {
  uint16_t seq;
  byte  payload[29];
  uint8_t crc;  
};

typedef struct package Package;
Package data;

void setup() {
  Serial.begin(115200);
  delay(1000);

  myRadio.begin(); 
  myRadio.setChannel(115); 
  myRadio.setPALevel(RF24_PA_MAX);
  myRadio.setAutoAck(false);
  myRadio.setDataRate( RF24_2MBPS );
 
  
  myRadio.openReadingPipe(1, addresses[0]);
  myRadio.startListening();
}


void loop()  {
  if ( myRadio.available()) {
    while (myRadio.available()){
      myRadio.read( &data, sizeof(data) );
    }
         
    Serial.print("\nRecebido");
    Serial.print("\nPacote:");
    Serial.print(data.seq);
    Serial.print("\n");
    Serial.println((char*)data.payload);
    Serial.print("CRC:");
    Serial.println(data.crc);
   
      
  
  }
 
}
