#include <Servo.h>
#include <SoftwareSerial.h>

#define BUFF_SIZE 256
#define NUM_ULTRA 3

#define RIGHT 60
#define LEFT 120
#define CENTER 90
#define LIMIT 58
/*
 * Button 
 * const int buttonPin = 2;
 */
const int buttonPin = 2;
uint16_t lasttouched = 0;
uint16_t currtouched = 0;

/*
 * bluetooth 
 *  블루투스 모듈의 T(Transmitt)x를 Digital pin 4번에 연결
 *  블루투스 모듈의 R(Receive)x를 Digital pin 5번에 연결
 */
int blue_Tx = 4; 
int blue_Rx = 5;  
int bufferSize = 0;
//String mac_addr = "rn00:21:13:01:51:5D\n"; // 박효정 MAC_ID
String mac_addr = "\r20:16:05:19:90:62\n"; // 이호찬 MAC_ID
char buffer[] = {'\r','m','s','g','\n'};
SoftwareSerial BTSerial(blue_Tx, blue_Rx);  //쓰기,읽기(RX,TX) 블루투스 모듈과 교차하여 연결됨
String command = "";

/*
 * arduino touchpad
 *  Hardware: 3.3V Arduino Pro Mini
           SDA -> A4
           SCL -> A5
           IRQ -> D2
 * Interrupt 
LOW to trigger the interrupt whenever the pin is low,
CHANGE to trigger the interrupt whenever the pin changes value
RISING to trigger when the pin goes from low to high,
FALLING for when the pin goes from high to low.
 The Due, Zero and MKR1000 boards allows also: 
 HIGH to trigger the interrupt whenever the pin is high.
 */
volatile byte state = HIGH; //전역 변수

/*=======================================
 * ultra sensor
 ========================================*/
int echoPin[] ={12,10,8};
int trigPin[] ={13,11,9};
float distance[NUM_ULTRA][5]={0};
int distIndex[3] = {0};
float avg[NUM_ULTRA] = {0};


/*====================================
 * Servo motor
 * const int motorPin = 9;
 =====================================*/
const int motorPin = 7;
Servo servo; 
int angle = 0; // servo position in degrees to 0(0.5ms purse) from 180(2.5ms purse)

/*
 * timer setting
 */
unsigned long preTime = 0;
unsigned long  currentTime = 0;
unsigned int duration = 3000;


void setup() {
  Serial.begin(9600);
  BTSerial.begin(9600);
  
  /*----------------------------------------
   * servo motor
   -----------------------------------------*/
  servo.attach(motorPin);
  
  /*----------------------------------------
   * button
   -----------------------------------------*/
  //pinMode(irqpin, INPUT);
  //digitalWrite(irqpin, HIGH);
  //pinMode(buttonPin, INPUT_PULLUP);
  //attachInterrupt(digitalPinToInterrupt(buttonPin), myKeyPad, FALLING);

  /*----------------------------------------
   * ultra pad
   -----------------------------------------*/
  // trig를 출력모드로 설정, echo를 입력모드로 설정
  for(int i = 0 ; i<NUM_ULTRA ; i++){
      pinMode(trigPin[i], OUTPUT);
      pinMode(echoPin[i], INPUT);
      delay(3);
  }
   preTime = millis();
}


void loop() {
  myTimer();
}
/*
 * 1. 블루투스 통신
 * 2. 초음파 센서 -> 진동
 * 3. 모터
 */
void myTimer(){
   currentTime = millis();
  if (currentTime - preTime >= duration) {
     //duration 마다 distance 검사 -> 가까운 곳 물체 있으면 진동 수행
      turnHandle();
      preTime = currentTime;
  }else {
      //getPhoneNumber();
      ultra();
      //readBluetoothData();
  }
}

void clickButton(){
  //Part for testing interrupt pin.
  Serial.println("click");
  changeServoMotorAngle('c');
}

void turnHandle(){
    bool isBlocked[NUM_ULTRA] ={false};
    for(int i = 0 ; i<NUM_ULTRA ; i++ ){
      int avg = 0;
      int numberOfZero = 0;
      for(int j = 0; j < 5 ; j++){
          if(distance[i][j]!=0)
            avg += distance[i][j];
          else
            numberOfZero++;
       }
       avg /= (5-numberOfZero);
      if(avg <LIMIT){
        isBlocked[i] = true;
      }
    }
    if(isBlocked[2]){ //center에 방해물이 있으면
    if(isBlocked[0] && !isBlocked[1]){ //right쪽에 방해물이 있으면
      if(angle != LEFT){
        Serial.println(" ........오른쪽 막힘.. 왼쪽 이동");
        changeServoMotorAngle('l');
      }else{
        Serial.println(" ........오른쪽 막힘..이미 왼쪽 방향");
      }
    }else if(isBlocked[1] && !isBlocked[0]){
      if(angle != RIGHT){
          Serial.println(" ........왼쪽 막힘.. 오른쪽 이동");
          changeServoMotorAngle('r');
      }else{
        Serial.println(" ........오른쪽 막힘..이미 오른쪽 방향");
      }
     }
    else if(isBlocked[0] && isBlocked[1]){ // right , left 둘다 방해물이 있으면
      Serial.println(" ........막힘 진동");
    }
     else if(!isBlocked[0] &&!isBlocked[1]){ //  right , left 둘다 방해물이 없으면
      if(angle != RIGHT && angle != LEFT){
        Serial.println(" ........둘다 괜찮음... 오른쪽 이동");
        changeServoMotorAngle('r');
      }else{
         Serial.println(" ........둘다 괜찮음... 이미 방향 바꿈");
      }
     }
  }else{
    if(angle != CENTER){
      Serial.println("...직진");
      changeServoMotorAngle('c');
    }
  }
}


void changeServoMotorAngle(char c){
  switch(c){
    case 'r':
    angle = RIGHT;
    break;
    case 'c':
    angle = CENTER;
    break;
     case 'l':
    angle = LEFT;
    break;
  } 
    servo.write(angle);
    delay(100);
}

void ultra(){
  for(int i = 0 ; i<NUM_ULTRA  ; i++){
  // 초음파를 보낸다. 다 보내면 echo가 HIGH 상태로 대기하게 된다.
    digitalWrite(trigPin[i], LOW);
    digitalWrite(echoPin[i], LOW);
    
    delayMicroseconds(2);
    digitalWrite(trigPin[i], HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin[i], LOW);
     
    // echoPin 이 HIGH를 유지한 시간을 저장 한다.
   unsigned long  mDuration = pulseIn(echoPin[i], HIGH); 
   //delayMicroseconds(100);
     // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
     float mDistance = mDuration / 29.0 / 2.0;
    int state = 0;
    if(mDistance < 3000){
      if(distIndex[i] >= 5){
         distIndex[i] = 0;
      }
      distance[i][(distIndex[i]++)] = mDistance;
    }else{
        Serial.print(i);
        Serial.print(" : ");
        Serial.print(mDistance);
        Serial.println(" .. 길이 이상");
    }
    delayMicroseconds(200);
  }

}

void readBluetoothData(){
  if(Serial.available()){
    char data = (char)Serial.read();
    delay(1);
    if(data == '0'){ //이벤트 발생 시
      int index = 0;
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
  }
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




