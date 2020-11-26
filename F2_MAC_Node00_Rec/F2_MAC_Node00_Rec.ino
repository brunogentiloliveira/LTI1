#include <SPI.h>  
#include "RF24.h" 
#include <RF24Network.h>


RF24 myRadio (0, 5); 
RF24Network network(myRadio);

const uint16_t this_node = 00;    // Address of our node in Octal format ( 04,031, etc)
const uint16_t other_node = 01;   // Address of the other node in Octal format

byte addresses[][6] = {"0", "1"};
int error_count = 0;
char aux_RTT[10] = "Recebido";


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
  network.begin(115, this_node);
  
  data_aux.seq = 0;
}


void loop(void)  {
  network.update();
  if(network.available()){

      while( network.available() ){
        
        RF24NetworkHeader header;        // If so, grab it and print it out
        network.read(header, &data, sizeof(data));
      }

      if(data.seq != data_aux.seq+1){
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
          Serial.print("\nTramas perdidas: ");
          Serial.println(error_count);   
  }
  
  
}
  
 
