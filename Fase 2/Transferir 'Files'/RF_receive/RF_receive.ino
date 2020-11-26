
#include <SPI.h>  
#include "RF24.h" 

RF24 myRadio (0, 5); 
byte addresses[][6] = {"0", "1"};
int i;
int error_count = 0;


struct package {
  uint16_t seq;
  byte  payload[29];
  uint8_t crc;  
};

typedef struct package Package;
Package data;
Package data_aux;

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
  data_aux.seq = 0;
}


void loop()  {
    if ( myRadio.available()) {
      while (myRadio.available()){
        myRadio.read( &data, sizeof(data) ); 
      }
      if (data.seq != data_aux.seq+1){
        error_count++;
      }
      data_aux.seq = data.seq;
      
      
  
      Serial.print("\nRecebido");
      Serial.print("\nPacote:");
      Serial.print(data.seq);
      Serial.print("\n");
      Serial.println((char*)data.payload);
      Serial.print("CRC:");
      Serial.println(data.crc);
      Serial.print("Tramas perdidas: ");
      Serial.println(error_count);
  
      myRadio.stopListening();
      myRadio.openWritingPipe(addresses[1]);
     
  
      char ack[10] = "Recebido";
      myRadio.write(&ack, sizeof(ack));
      myRadio.openReadingPipe(1, addresses[0]);
      myRadio.startListening();  
    }
  
  
 
}
