#include <Time.h>
#include <TimeLib.h>

#include <SPI.h>  
#include "RF24.h"
#include <stdio.h> 
#include <stdlib.h> 
#include <time.h> 
#include <iostream>
#include <string>
#include <chrono>
#include <ctime> 
#include <RF24Network.h>

#define N_TRAMAS 1000

RF24 myRadio (0, 5);
RF24Network network(myRadio); 

uint16_t counter;

char array_file[ N_TRAMAS * 29 ];
int role = 0; // P/ ativar a transmissao quando quiser 

const uint16_t this_node = 01;
const uint16_t other_node = 00; 

const unsigned long interval = 10; //intervalo entre envios
unsigned long last_sent;           // When did we last send?

struct package {
  uint16_t seq;
  byte  payload[29];;
  uint8_t crc;
};

typedef struct package Package;
Package data;


void setup() {
  Serial.begin(115200);
  myRadio.begin();  
  network.begin(115, this_node); //channel; node address
 
  //Preencher Array p/ enviar --> Na proxima fase este array vai ser o ficheiro
  for (int i = 0; i<= N_TRAMAS * 29; i++){
    array_file[i] = 'a' + (rand()%26);
  }
  
  counter = 1;  
}

void loop(void) {  

 if(role == 1){
    network.update();
   
    unsigned long now = millis();  
    if( now - last_sent >= interval ){
      last_sent = now;
      Package data;
      
      data.seq = counter;
      memcpy(data.payload, array_file, sizeof(char)*29);
      strncpy(array_file, array_file + 29, sizeof(array_file) - 29);
      data.crc = genCRC((uint8_t*)&data.payload, sizeof(data.payload));
      

      counter = counter + 1;
  
      RF24NetworkHeader header(/*to node*/ other_node); // Criar Header com o address do outro node
      bool ok = network.write(header, &data, sizeof(data));
      
      if(ok){
        Serial.print("\nEnviado");
        Serial.print("\nPacote:");
        Serial.print(data.seq);
        Serial.print("\n");
        Serial.println((char*)data.payload);
        Serial.print("CRC: ");
        Serial.println(data.crc); 
       
      }else{
        Serial.println("Failed to comunicate");
      } 
    }

    
    if(counter == N_TRAMAS + 1){
      Serial.println("\nFicheiro Transmitido");
      role = 0;
      counter = 0;
    }
 }
 
/**************************SET ROLE ********************************************************/

  if( Serial.available() ){
    char c = toupper(Serial.read());
    if( c == 'T' && role == 0 ){
      Serial.print(F("*** CHANGING TO TRANSMIT ROLE -- PRESS 'S' TO STOPP"));
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
