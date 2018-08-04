#include <SoftwareSerial.h>
#include <Wire.h>
#include "TTP229.h"
#define BUFF_SIZE 256
#define SCL_PIN 2
#define SDO_PIN 3
#define TTP_229_TYPE 0
TTP229 ttp229(SCL_PIN, SDO_PIN); // TTP229(sclPin, sdoPin)

int ledPin = 13;

// IRQ:2, SCL: A5, SDA: A4,..
int touchPadMode = TTP_229_TYPE;

// TTP229 를 사용하였을 때,
byte touchedKey;
volatile uint16_t btnState;
static uint16_t oldState = 9999;

// MPR121을 사용하였을 때
uint16_t lastTouched = 0;
uint16_t currTouched = 0;


int joystick_x = A0;
int joystick_y = A1;
int joystick_press = 9;
int blue_Tx = 7;  //블루투스 모듈의 T(Transmitt)x를 Digital pin 9번에 연결
int blue_Rx = 4;  //블루투스 모듈의 R(Receive)x를 Digital pin 10번에 연결

int bufferSize = 0;
//String mac_addr = "rn00:21:13:01:51:5D\n"; // 박효정 MAC_ID
//String mac_addr = "\r20:16:05:19:90:62\n"; // 이호찬 MAC_ID

String mac_addr = "\rFC:A8:9A:00:20:E2\n"; // 이호찬 MAC_ID
//00:21:13:01:51:5D//??
int isSerialMode = 0;

int top_m = 0;
int bottom_m = 0;
int left_m = 0;
int right_m = 0;
int sel_m = 0;

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
  BTSerial.begin(9600);
  Serial.begin(9600);

  pinMode(ledPin, OUTPUT);
  
  pinMode(joystick_press, INPUT);
  digitalWrite(joystick_press, HIGH);

  pinMode(SCL_PIN, OUTPUT);
  pinMode(SDO_PIN, INPUT);
 
  index =0;
  bufferSize = 0;
}

void loop(){
    serialMode();
    if (isSerialMode == 0) {   // While Arduino is being in serial mode, then ignore others.
      bluetoothMode();
      sendJoyStickInput();
      checkTouchPad();
    }
    
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
      isSerialMode = 0;
      command = "";
      delay(1);
    } else {
      isSerialMode = 1;
      command.concat(c);
      delay(1);
    }
         
    if(command.equals("MAC_ADDR")){
      Serial.println(mac_addr);
      
 //     digitalWrite(ledPin, HIGH);
   //   delay(1000);
     // digitalWrite(ledPin, LOW);
     // delay(1000);
      writeStringUsb(mac_addr);
    }
  } //
}

/**
 *  string data를 편하게 USB로 write하기 위해 사용.
 */
void writeStringUsb(String stringData) {
  for (int i = 0; i < stringData.length(); i++)
  {
    Serial.write(stringData[i]);   // Push each char 1 by 1 on each loop pass
    delay(1);
  }
}

/**
 *  string data를 편하게 bluetooth로 write하기 위해 사용.
 */
void writeStringBt(String stringData) {
  index = 0;
  while (index < sizeof(stringData)) {
    BTSerial.write(stringData[index++]);
    delay(8);
  }
}

/**
 * Send Bluetooth based on JoyStick inputs.
 */
void sendJoyStickInput() {
  // put your main code here, to run repeatedly:
  int x = analogRead(joystick_x);
  int y = analogRead(joystick_y);
  //Serial.println(String(x)+", "+String(y));
  // Process clicking the button.
  if (digitalRead(joystick_press) == LOW && 
        x >= 400 && x <= 600 && y >= 400 && y <= 600 && sel_m == 0) {
      Serial.print("s");
      // If clicked the joystick button
      char dir[] = "\rds\n";
      writeStringBt(dir);
      sel_m = 1;
  }
  if (digitalRead(joystick_press) == HIGH) sel_m = 0;

  // Tasks per joystick direction.
  if (y >= 400 && y <= 600) {
    if (x >= 600 && x <= 1023 && right_m == 0) {
      Serial.print("r");
      setJoyStickDirection(0, 0, 0, 1);
      char dir[] = "\rdr\n";
      writeStringBt(dir);
    } else if(x >= 0 && x <= 400 && left_m == 0) {
      Serial.print("l");
      setJoyStickDirection(0, 0, 1, 0);
      char dir[] = "\rdl\n";
      writeStringBt(dir);
    }
    if (x >= 400 && x <= 600)
      setJoyStickDirection(0, 0, 0, 0);
  } else if (x >= 400 && x <= 600) {
    if (y >= 600 && y <= 1023 && bottom_m == 0) {
      Serial.print("b");
      setJoyStickDirection(0, 1, 0, 0);
      char dir[] = "\rdb\n";
      writeStringBt(dir);
    } else if(y >= 0 && y <=400 && top_m == 0) {
      Serial.print("u");
      setJoyStickDirection(1, 0, 0, 0);
      char dir[] = "\rdt\n";
      writeStringBt(dir);
    }
    if (y >= 400 && y <= 600)
      setJoyStickDirection(0, 0, 0, 0);
  }
}

/**
 *  Set the flags.
 */
void setJoyStickDirection(int _top_m, int _bottom_m, int _left_m, int _right_m) {
  top_m = _top_m;
  bottom_m = _bottom_m;
  left_m = _left_m;
  right_m = _right_m;
}

/**
 * Check touchpad and send the values to the smartphone.
 */
void checkTouchPad() {
  uint16_t key = 0;
  if (touchPadMode == TTP_229_TYPE) {
    key = ttp229.ReadKey16(); // Blocking
  } /*else if (touchPadMode == MPR_121_TYPE) {
    key =  checkMPR121Key();
  }*/
  if (key) {
    //if (key >=1 && key <=11) 
    Serial.println(key);
    String dir;
    if (key == 3) { // 0
      dir = "\rb0\n";
    } else if (key == 2) { // 1
      dir = "\rb1\n"; 
    } else if (key == 1) { // 2
      dir = "\rb2\n"; 
    } else if (key == 7) { // 3
      dir = "\rb3\n"; 
    } else if (key == 6) { // 4
      dir = "\rb4\n"; 
    } else if (key == 5) { // 5
      dir = "\rb5\n"; 
    } else if (key == 9) { // remove
      dir = "\rbr\n"; 
    } else if (key == 4) { // mode
      dir = "\rbm\n"; 
    } else if (key == 8) { // complete
      dir = "\rbc\n"; 
    } else if (key == 7) { // dobule
      dir = "\rbd\n";
    } else if (key == 11) { // remove all
      dir = "\rbra\n"; 
    }
   
    writeStringBt(dir);
    delay(100);
  }
  return;
}

