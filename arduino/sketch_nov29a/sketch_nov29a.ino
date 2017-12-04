#include <SoftwareSerial.h>

int blue_Tx = 10; //TX 보내는 핀
int blue_Rx = 9; //받는 핀
int led_pin = 13;
String str="";
SoftwareSerial blue_serial(blue_Tx,blue_Rx);

void setup() {
  // put your setup code here, to run once:
    Serial.begin(9600);
    Serial.println("ready");
    pinMode(led_pin, OUTPUT);
    blue_serial.begin(9600);
}
void initializeBluetooth(){
  if(blue_serial.available()){
      blue_serial.write("AT");
      blue_serial.write("AT+NAMETEST");
      blue_serial.write("AT+PIN2017");
  }
}

void loop() {
  // put your main code here, to run repeatedly:
    while(blue_serial.available()){ //mySerial에 전송된 값이 있으면
      char ch = (char)blue_serial.read();
      str +=ch;
      delay(5);
    }
    if(!str.equals("")){
      Serial.println(str);
      str="";
    }
}
