 #include <SoftwareSerial.h>


int blue_Tx = 9;  //블루투스 모듈의 T(Transmitt)x를 Digital pin 9번에 연결
int blue_Rx = 10;  //블루투스 모듈의 R(Receive)x를 Digital pin 10번에 연결
int led_pin = 13;
//00:21:13:01:51:5D//??

byte data;
uint8_t buffer[1024];
int index;
SoftwareSerial BTSerial(blue_Tx, blue_Rx);  //쓰기,읽기
/*안드로이드에서 전송 된 데이터는 블루투스 모듈로 수신되면 
 * 블루투스는 아두이노에 쓰기 작업 (Tx)
 * 아두이노는 읽기 작업 (Rx)
 * 즉 SoftwareSerial BTSerial(RX,TX) 선언 시
 * Rx는 블루투스 모듈의 Tx와 연결
 * Tx는 블루투스 모듈의 Rx와 연결
 */

/*
*블루투스는 서버로 작동되어 클라이언트(안드로이드) 접속을 기다림.
*Connection 시 이전 페어링 기록이 있으면 비밀번호 입력 할 필요 없음.
*초기 접속시에는 비밀번호 입력
*/
void setup(){
  BTSerial.begin(9600);
  Serial.begin(9600);
  index = 0;
 
}

void loop(){
  index = 0;
      /*Serial 창에 문자가 한개 이상 입력 될 경우 
     * 연결된 Bluetooth 기기로 문자 전송.
     * PC(Serial 창) -> 아두이노 -> 블루투스 모듈 -> 안드로이드
     */
  if((Serial.available())>0){ 
       uint8_t d  = Serial.read();
       //buffer[index++] = d;
      
       Serial.print("start to send msg : ");
       Serial.println(d,BIN);
       BTSerial.flush();
       BTSerial.println(d);
       delay(1);     
    }
    /*
    data = B10010;
    BTSerial.println(Serial.read());
     delay(1);
     BTSerial.println("abcdef");
     i++;
  } */

  
}
  /*
  //
  while (BTSerial.available()){
    digitalWrite(led_pin, LOW);
    data = BTSerial.read(); //
    delay(1);
  }
*/



