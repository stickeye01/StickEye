 #include <SoftwareSerial.h>
 #include <Wire.h>
 #include "Adafruit_MPR121.h"

#define BUFF_SIZE 256

// You can have up to 4 on one i2c bus but one is enough for testing!
Adafruit_MPR121 cap = Adafruit_MPR121();

// Keeps track of the last pins touched
// so we know when buttons are 'released'
uint16_t lasttouched = 0;
uint16_t currtouched = 0;

int joystick_x = A0;
int joystick_y = A1;
int joystick_press = 9;
int blue_Tx = 7;  //블루투스 모듈의 T(Transmitt)x를 Digital pin 9번에 연결
int blue_Rx = 4;  //블루투스 모듈의 R(Receive)x를 Digital pin 10번에 연결
int bufferSize = 0;
//String mac_addr = "rn00:21:13:01:51:5D\n"; // 박효정 MAC_ID
String mac_addr = "\r20:16:05:19:90:62\n"; // 이호찬 MAC_ID
//00:21:13:01:51:5D//??

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
  pinMode(joystick_press, INPUT);
  digitalWrite(joystick_press, HIGH);
  BTSerial.begin(9600);
  Serial.begin(9600);

  while (!Serial) {
    delay(10);
  }

  if (!cap.begin(0x5A)) {
    Serial.println("MPR121 not found, check wiring?");
    while (1);
  }
  Serial.println("MPR121 found!");
  
  index =0;
  bufferSize = 0;
}

void loop(){
    checkTouchPad();
    serialMode();
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
 * JoyStick 입력에 따라 Bluetooth 전송.
 */
void sendJoyStickInput() {
  // put your main code here, to run repeatedly:
  int x = analogRead(joystick_x);
  int y = analogRead(joystick_y);

  // 조이스틱 클릭 시 사용.
  if (digitalRead(joystick_press) == LOW && 
        x >= 400 && x <= 600 && y >= 400 && y <= 600 && sel_m == 0) {
      Serial.print("선");
      // If clicked the joystick button
      char dir[] = "\rds\n";
      writeStringBt(dir);
      sel_m = 1;
  }
  if (digitalRead(joystick_press) == HIGH) sel_m = 0;

  // 조이스틱 방향 별 작업.
  if (y >= 400 && y <= 600) {
    if (x >= 600 && x <= 1023 && right_m == 0) {
      Serial.print("우");
      setJoyStickDirection(0, 0, 0, 1);
      char dir[] = "\rdr\n";
      writeStringBt(dir);
    } else if(x >= 0 && x <= 400 && left_m == 0) {
      Serial.print("좌");
      setJoyStickDirection(0, 0, 1, 0);
      char dir[] = "\rdl\n";
      writeStringBt(dir);
    }
    if (x >= 400 && x <= 600)
      setJoyStickDirection(0, 0, 0, 0);
  } else if (x >= 400 && x <= 600) {
    if (y >= 600 && y <= 1023 && bottom_m == 0) {
      Serial.print("하");
      setJoyStickDirection(0, 1, 0, 0);
      char dir[] = "\rdb\n";
      writeStringBt(dir);
    } else if(y >= 0 && y <=400 && top_m == 0) {
      Serial.print("상");
      setJoyStickDirection(1, 0, 0, 0);
      char dir[] = "\rdt\n";
      writeStringBt(dir);
    }
    if (y >= 400 && y <= 600)
      setJoyStickDirection(0, 0, 0, 0);
  }
}

/**
 *  flag 변수 설정 함수.
 */
void setJoyStickDirection(int _top_m, int _bottom_m, int _left_m, int _right_m) {
  top_m = _top_m;
  bottom_m = _bottom_m;
  left_m = _left_m;
  right_m = _right_m;
}

void checkTouchPad() {
  currtouched = cap.touched();
  
  for (uint8_t i=0; i<12; i++) {
    // it if *is* touched and *wasnt* touched before, alert!
    if ((currtouched & _BV(i)) && !(lasttouched & _BV(i)) ) {
      Serial.print(i); Serial.println(" touched");
    }
    // if it *was* touched and now *isnt*, alert!
    if (!(currtouched & _BV(i)) && (lasttouched & _BV(i)) ) {
      Serial.print(i); Serial.println(" released");
    }
  }
  
  // reset our state
  lasttouched = currtouched;

  // comment out this line for detailed data from the sensor!
  return;
}

