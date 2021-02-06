#include <SPI.h>  
#include "RF24.h" 
#include <RF24Network.h>

#define LIMITE_RETRANSMISSOES 3

RF24 myRadio (0, 5); 
byte addresses[][6] = {"0"};
bool portaReceiving = false, recievedPacote = false; 
int perdas = 0, lidos = 115, counter = 1, counter_aux = 1,nPac = 0, retransmissoes = 0, contador = 0; 

struct pacote{
  uint8_t seq;
  byte  payload[29];
  uint8_t crc;
};typedef struct pacote Pacote; Pacote pacote; Pacote pacoteRecebido; Pacote pacotesPorta[3500];

struct confirmation{
  uint16_t type;  /* ACK - 0 | NACK - 1 */
  uint16_t seq;
};
typedef struct confirmation Confirmation;
Confirmation message, messageRecebida;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  delay(1000);

  myRadio.begin(); 
  myRadio.setChannel(115); 
  myRadio.setPALevel(RF24_PA_MIN);
  myRadio.setDataRate( RF24_2MBPS );
  myRadio.setAutoAck(false);
  myRadio.openReadingPipe(1, addresses[0]);
  myRadio.startListening();
   
}

void loop() { 
  if(myRadio.available()){
    recievedPacote = true;
    sendConfirmation();    
  }
  serialEvent();
  if(portaReceiving){ 
    ////////////Serial.println("Siga para o outro radio");
     //nPac = 0;
     portaReceiving = false;
     sendToRadio2();
     
  }
}


void serialEvent(){
  if(Serial.available()){
    portaReceiving = true;
    int lidoS = 0;
    while(Serial.available()){
      nPac++;
      pacotesPorta[nPac].seq = nPac;
      lidoS += Serial.readBytes(pacotesPorta[nPac].payload, 29);
      pacotesPorta[nPac].crc = genCRC((uint8_t*)&pacotesPorta[nPac].payload, sizeof(pacotesPorta[nPac].payload));
      if(lidoS >= Serial.available())break;
    }
    //////Serial.println(lidos);
    //////Serial.print("Numero de pacotes a enviar: "); ////Serial.println(nPac);
  }
}
void sendToRadio2(){
  int role = 1;
   while(1){
     if(role == 1){         /** Transmitir pacote(counter) **/
        for(counter=counter_aux; counter <= nPac; counter++){
           //////////Serial.print("Sending "); //////////Serial.println(counter);
           myRadio.stopListening(); myRadio.openWritingPipe(addresses[0]);
           myRadio.write(&pacotesPorta[counter], sizeof(pacotesPorta[counter]));
           myRadio.openReadingPipe(1, addresses[0]);  myRadio.startListening(); 
           role = 2;
           break;
         }
         if(counter > nPac){
            myRadio.stopListening(); myRadio.openWritingPipe(addresses[0]);

            pacotesPorta[counter].seq = 0;
            memcpy(pacotesPorta[counter].payload, pacotesPorta[counter-1].payload, 29);
            pacotesPorta[counter].crc = pacotesPorta[counter-1].crc;
            memset(pacotesPorta[counter].payload,0,29);
            myRadio.write(&pacotesPorta[counter], sizeof(pacotesPorta[counter]));
           // myRadio.flush();

            myRadio.flush_tx();
            myRadio.openReadingPipe(1, addresses[0]);  myRadio.startListening(); 
            counter = 1;counter_aux = 1; nPac = 0; 
           
            break;
         }
       
     }
     else if(role == 2){ 
       
        if(myRadio.available()){
          while(myRadio.available()){
            myRadio.read(&messageRecebida, sizeof(messageRecebida));
          }
            memset(pacotesPorta[counter].payload, 0, sizeof(pacotesPorta[counter].payload));
           //////////////Serial.print("RECEBIDA MENSAGEM CONFIRMAÇAO "); ////////////Serial.println(messageRecebida.seq);
           if(messageRecebida.seq == counter_aux && messageRecebida.type == 0){ /**Passa p/ Trama seguinte **/
              //////////////Serial.println("ACK RECEIVED...");
              counter_aux= counter +1;
              role = 1;
           }else{
              //////////////Serial.println("NACK RECEIVED...");
              counter_aux = counter;
              role = 3;
           }
          }    
     }
     else if(role == 3){              /** RETRANSMISSAO **/
      //////////////Serial.println("A retransmitir ...");
        if(retransmissoes <= LIMITE_RETRANSMISSOES-1){
            myRadio.stopListening(); myRadio.openWritingPipe(addresses[0]);
               myRadio.write(&pacotesPorta[counter], sizeof(pacotesPorta[counter]));
               retransmissoes++;
            myRadio.openReadingPipe(1, addresses[0]); myRadio.startListening();
            //////////////Serial.print("Reenviado:");////////////Serial.println(pacotesPorta[counter].seq);
            role = 2;
        }else{
            counter_aux = counter+1;
            perdas++;
            retransmissoes = 0;
            role = 1;
        }
     } 
  }
}

void sendConfirmation(){
  
  while(myRadio.available()){
    myRadio.read(&pacoteRecebido, sizeof(pacoteRecebido));
  }
    if(pacoteRecebido.seq == 0){
      //////Serial.println("FIM");
      return;
    }else{
    ////////////Serial.println(pacoteRecebido.seq);
      uint8_t crc_aux = genCRC((uint8_t*)&pacoteRecebido.payload, sizeof(pacoteRecebido.payload));

      ////Serial.print("Sending Confirmation "); 
      if(crc_aux == pacoteRecebido.crc){Serial.write(pacoteRecebido.payload, sizeof(pacoteRecebido.payload)); message.type = 0;}  /** ACK **/ 
      else{   message.type = 1; }   
      message.seq = pacoteRecebido.seq;
      memset(pacoteRecebido.payload, 0, 29);

      
      myRadio.stopListening(); myRadio.openWritingPipe(addresses[0]);
      myRadio.write(&message, sizeof(message));
      myRadio.openReadingPipe(1, addresses[0]);  myRadio.startListening();
    }
  
}

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
