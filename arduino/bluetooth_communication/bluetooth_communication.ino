 #include <SoftwareSerial.h>

#define BUFF_SIZE 256
int blue_Tx = 2;  //블루투스 모듈의 T(Transmitt)x를 Digital pin 9번에 연결
int blue_Rx = 3;  //블루투스 모듈의 R(Receive)x를 Digital pin 10번에 연결
int led_pin = 13;
int bufferSize = 0;
String maxid = "00:21:13:01:51:5D";
//00:21:13:01:51:5D//??

int index;
char data;
char buffer[] = {'\r','m','s','g','\n'};
String mode = "serial";
SoftwareSerial BTSerial(blue_Tx, blue_Rx);  //쓰기,읽기(RX,TX) 블루투스 모듈과 교차하여 연결됨
/*안드로이드에서 전송 된 데이터는 블루투스 모듈로 수신되면 
 * 블루투스는 아두이노에 쓰기 작업 (Tx)
 * 아두이노는 읽기 작업 (Rx)
 * 즉 SoftwareSerial BTSerial(RX,TX) 선언 시
 * Rx는 블루투스 모듈의 Tx와 연결
 * Tx는 블루투스 모듈의 Rx와 연결
/*
*블루투스는 서버로 작동되어 클라이언트(안드로이드) 접속을 기다림.
*Connection 시 이전 페어링 기록이 있으면 비밀번호 입력 할 필요 없음.
*초기 접속시에는 비밀번호 입력
*/
void setup(){
  BTSerial.begin(9600);
  Serial.begin(9600);
  index =0;
  bufferSize = 0;
}
void loop(){
  if(mode.equals("serial")){
    serialMode();
  }else if(mode.equals("bluetooth")){
    bluetoothMode();
    if(BTSerial.available()){
      Serial.write(BTSerial.read());
      delay(500);
    }
  }
}

void bluetoothMode(){
  if(Serial.available()){
    char data = (char)Serial.read();
    delay(1);
    if(data == '0'){ //이벤트 발생 시
      index = 0;
      while(index < sizeof(buffer)){
        BTSerial.print(buffer[index++]);
        delay(8); 
        /*1bit 당 1초 delay를 줌.
         * 현재는 8bit를 print 하기 때문에
        */
    }
    // 초기화
       index = 0;
       bufferSize = 0;
    }
  }
}

void serialMode(){
  if(BTSerial.available()){
    String resp = BTSerial.readString();
    Serial.print(resp);
    delay(500);
  }
  /*안드로이드 -> 아두이노
   * AT : AT mode 시작
   * AT+NAME이름 : 블루투스 이름 변경
   * 
   */
  if(Serial.available()){
    String tmp = Serial.readString();
    Serial.print(tmp);
    if(tmp.equals("bluetooth")){
        Serial.println("change mode");
        mode = "bluetooth"; 
    }else if("maxid"){
        Serial.println(maxid);
    }else{
       BTSerial.print(tmp);
    }
    delay(500);
  }
}


