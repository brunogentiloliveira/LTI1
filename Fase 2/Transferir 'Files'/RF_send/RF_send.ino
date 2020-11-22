#include <SPI.h>  
#include "RF24.h"
#include <stdio.h> 
#include <stdlib.h> 
#include <time.h> 
#include <iostream>
#include <string>
#include <chrono>
#include <ctime> 


RF24 myRadio (0, 5);
byte addresses[][6] = {"0"};
uint16_t counter;
char array_file[29000];
char aux[50];
int role = 0; // P/ ativar a transmissao quando quiser 
unsigned long start_time;


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
  counter = 1;
  myRadio.begin();  
  myRadio.setChannel(115); 
  myRadio.setPALevel(RF24_PA_MAX);
  myRadio.setAutoAck(false);
  myRadio.setDataRate( RF24_2MBPS );
  
  //Preencher Array p/ enviar --> Na proxima fase este array vai ser o ficheiro
  for (int i = 0; i<= 29000; i++){
    array_file[i] = 'a' + (rand()%26);
  }

  myRadio.openWritingPipe(addresses[0]);
  
}

void loop() {  
 if(role == 1) {
    counter = counter + 1;
   
    memset(data.payload, 0, sizeof(data.payload));
    data.seq = counter;
    memcpy(data.payload, array_file, sizeof(char)*29);
    strncpy(array_file, array_file + (counter * 29), sizeof(array_file) - (counter*29));
    data.crc = genCRC((uint8_t*)&data.payload, sizeof(data.payload));

    start_time = micros();
    myRadio.write(&data, sizeof(data)); 
    
    Serial.print("\nEnviado");
    Serial.print("\nPacote:");
    Serial.print(data.seq);
    Serial.print("\n");
    //Serial.println((char*)data.payload);
    Serial.print("CRC: ");
    Serial.println(data.crc);
    unsigned long elapsedTime = micros() - start_time;
    Serial.print("Elapsed Time:  ");
    Serial.print(elapsedTime);
    Serial.println(" microssegundos");
   
    delay(100);
  
    if(counter == 1000){
      while(1){}
    }

 }


/**************************SET ROLE ********************************************************/

  if( Serial.available() ){
    char c = toupper(Serial.read());
    if( c == 'T' && role == 0 ){
      Serial.print(F("*** CHANGING TO TRANSMIT ROLE -- PRESS 'S' TO STOP"));
      role = 1;
    }else if( c == 'S' && role == 1){
      Serial.print(F("*** Transmission Stopped - PRESS 'T' TO SWITCH BACK"));
      role = 0;
    }
  }
 
} 
/****************************** END LOOP ****************************************************/

uint8_t genCRC(uint8_t *data, size_t len){
  uint8_t crc = 0xff;
  size_t i, j;
  for (i = 0; i < len; i++) {
    crc ^= data[i];
    for (j = 0; j < 8; j++) {
      if ((crc & 0x80) != 0){
        crc = (uint8_t)((crc << 1) ^ 0x31);
      }else{
        crc <<= 1;
      }
    }
  }
  return crc;
}
