 #include <SoftwareSerial.h>

#define BUFF_SIZE 256
int joystick_x = A0;
int joystick_y = A1;
int joystick_press = 1;
int blue_Tx = 2;  //블루투스 모듈의 T(Transmitt)x를 Digital pin 9번에 연결
int blue_Rx = 3;  //블루투스 모듈의 R(Receive)x를 Digital pin 10번에 연결
int bufferSize = 0;
//String mac_addr = "rn00:21:13:01:51:5D\n"; // 박효정 MAC_ID
String mac_addr = "\r20:16:05:19:90:62\n"; // 이호찬 MAC_ID
//00:21:13:01:51:5D//??

int index;
char data;
char buffer[] = {'\r','m','s','g','\n'};
SoftwareSerial BTSerial(blue_Tx, blue_Rx);  //쓰기,읽기(RX,TX) 블루투스 모듈과 교차하여 연결됨
String command = "";

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
  pinMode(joystick_press, INPUT_PULLUP);
  BTSerial.begin(9600);
  Serial.begin(9600);
  index =0;
  bufferSize = 0;
}

void loop(){
    serialMode();
    Serial.print("Test");
    sendJoyStickInput();
    //bluetoothMode();
    //if(BTSerial.available()){
    //  Serial.write(BTSerial.read());
    //  delay(500);
}

/**
 *  Bluetooth Networking 처리.
 */
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

/**
 *  USB Networking 처리.
 */
void serialMode(){
  /*
   *  "MAC_ADDR" 이라는 명령을 안드로이드로부터 받으면,
   *  실제 mac_address 전송.
   */
   if(Serial.available()){
    char c = (char) Serial.read();
    delay(1);
    if (c == '\0') {
      command = "";
      delay(1);
    } else {
      command.concat(c);
      delay(1);
    }
         
    if(command.equals("MAC_ADDR")){
      //Serial.println(mac_addr);
      writeString(mac_addr);
    }
  } //
}

/**
 *  string data를 편하게 write하기 위해 사용.
 */
void writeString(String stringData) {
  for (int i = 0; i < stringData.length(); i++)
  {
    Serial.write(stringData[i]);   // Push each char 1 by 1 on each loop pass
    delay(1);
  }
}

/**
 * JoyStick 입력에 따라 Bluetooth 전송.
 */
void sendJoyStickInput() {
    // put your main code here, to run repeatedly:
  int x = analogRead(joystick_x);
  int y = analogRead(joystick_y);
  int sel = digitalRead(joystick_press);

  if (y >= 400 && y <= 600) {
    if (x >= 600 && x <= 1023) {
      Serial.print("우");
      delay(100);
    } else if(x >= 0 && x <= 400) {
      Serial.print("좌");
      delay(100);
    }
  } else if (x >= 400 && x <= 600) {
    if (y >= 600 && y <= 1023) {
      Serial.print("하");
      delay(100);
    } else if(y >= 0 && y <=400) {
      Serial.print("상");
      delay(100);
    }
  }
}

