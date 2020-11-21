#include <SPI.h>  
#include "RF24.h"


RF24 myRadio (0, 5);
byte addresses[][6] = {"0"};
uint16_t counter;
char array_file[29000];


struct package {
  uint16_t seq;
  char  payload[300];
  uint8_t crc;
};

typedef struct package Package;
Package data;


void setup() {
  Serial.begin(115200);
  delay(1000);
  counter = 1;
  myRadio.begin();  
  myRadio.setChannel(115); 
  myRadio.setPALevel(RF24_PA_MAX);
  myRadio.setAutoAck(false);
  myRadio.setDataRate( RF24_2MBPS );
  
  //Preencher Array p/ enviar --> Na proxima fase este array vai ser o ficheiro
  std::fill(std::begin(array_file), std::begin(array_file) + 29000, 'a');
  

  
  
  myRadio.openWritingPipe(addresses[0]);
}

void loop() {
 
  counter = counter + 1;
  data.seq = counter;
  memcpy(data.payload, array_file, sizeof(char)*29);
  strncpy(array_file, array_file + (counter * 29), sizeof(array_file) - (counter*29));
  
  data.crc = 0;
  myRadio.write(&data, sizeof(data)); 
  Serial.print("\nEnviado");
  Serial.print("\nPacote:");
  Serial.print(data.seq);
  Serial.print("\n");
  Serial.println(data.payload);
  Serial.println(data.crc);
  delay(100);

  if(counter == 1000){
    while(1){}
  }
 
}
