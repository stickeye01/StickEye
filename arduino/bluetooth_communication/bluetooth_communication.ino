 #include <SoftwareSerial.h>

#define BUFF_SIZE 256
int blue_Tx = 2;  //블루투스 모듈의 T(Transmitt)x를 Digital pin 9번에 연결
int blue_Rx = 3;  //블루투스 모듈의 R(Receive)x를 Digital pin 10번에 연결
int led_pin = 13;
int bufferSize = 0;
//String mac_addr = "00:21:13:01:51:5D"; // 박효정 MAC_ID
String mac_addr = "20:16:05:19:90:62"; // 이호찬 MAC_ID
//00:21:13:01:51:5D//??

int index;
char data;
char buffer[] = {'\r','m','s','g','\n'};
SoftwareSerial BTSerial(blue_Tx, blue_Rx);  //쓰기,읽기(RX,TX) 블루투스 모듈과 교차하여 연결됨

int is_mac_sent = 1; // MAC_ID는 한번만 보내면 된다.

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
    serialMode();
    //bluetoothMode();
    //if(BTSerial.available()){
    //  Serial.write(BTSerial.read());
    //  delay(500);
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
  if(Serial.available()){
    String tmp = Serial.readString();
    Serial.print(tmp);
    /*
     *  "MAC_ADDR" 이라는 명령을 안드로이드로부터 받으면,
     *  실제 mac_address 전송.
     */
    if(tmp.equals("MAC_ADDR") and is_mac_sent == 1){
        Serial.println(mac_addr);
        writeString(mac_addr);
        is_mac_sent = 0;
    }
    delay(50);
  }
}

/**
 *  string data를 편하게 write하기 위해 사용.
 */
void writeString(String stringData) {
  for (int i = 0; i < stringData.length(); i++)
  {
    Serial.write(stringData[i]);   // Push each char 1 by 1 on each loop pass
  }
}
