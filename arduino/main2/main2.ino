/*
   arduino mini's PMW pins (d3,d5,d6,d9,d10,d11)
*/
#include <Servo.h>
#include <Wire.h>
#include <Kalman.h> //Source: https://github.com/TKJElectronics/

/*=======자이로 센서 사용 핀=================================
  kalAngleX값은 roll(좌우의 기울기). 초음파 센서의 기울어짐으로 인한 이상값 측정을 보정하기 위해 사용. 초음파 센서가 항상 정면의 위아래를 측정하도록 유도.
  kalAngleY값은 pitch(위아래 기울기). 지팡이의 기울기(tilt)
  Gyro Sensor
  SCL : A5
  SDA : A4
  INT : D2
  x : roll
  =====================================================*/

#define RIGHT 30
#define LEFT 150
#define CENTER 90

#define SEQUENCE_LIMIT 30.0 //(degree)
#define LIMIT_RL 100 //(height(cm) - 100cm)
#define LIMIT_UL 60

#define GAP 140
#define GAP_START 20
#define GAP_END 160
#define NUM_ULTRA 3
#define RL 0 // Right and left
#define UB 1 // Uppder and Bottom

#define SHANG 0
#define ZONG 1
#define HA 2

#define C 2 //Bottom

#define HYPO_BOTTOM 19 //바닥 측정을 위해 달린 초음파 센서의 지팡이 상의 빗변 길이
#define HYPO_TOP 45 //위에 달린 초음파 센서의 지팡이 상의 빗변 길이
/*========================================================
  vibration
  ========================================================*/
const int vibrationPin = 6;

/*========================================================
   ultra sensor and servo motor
  ========================================================*/
// index 0은 좌/우 측정, 1은 상/하 측정에 사용.
const int limit[] = {100, 180};

const int servoMotorPin[] = {A2, A3, A1};
// const int echoPin[] = {11, 5};
// const int trigPin[] = {10, 3};

const int echoPin[] = {11,9,5};
const int trigPin[] = {10,6,3};

float distance[2] = {0}; //장애물과의 거리: index 0은 좌/우 측정, 1은 상/하

// 회전 가능 각도 (최대/최소)
int minAngle = 100;
int maxAngle = 180;

Servo servo[3];
Servo handle;
float avg = 0;
int handleAngle = CENTER; // 초기 지팡이 핸들 각도: 기본 센터,
const int handleMotorPin = A0;

/* ========================================================
   timer
  ===========================================================*/
unsigned long preTime = 0;
unsigned long  currentTime = 0;
unsigned int duration = 1000;

/*===================================================
  setup
  =====================================================*/

void setup() {
  Serial.begin(9600);
  // @{
  // set servo motors for ultrasonography
  //
  for (int i = 0 ; i < 3 ; i++) {
    servo[i].attach(servoMotorPin[i]); //A2,A3,A1
    delay(15);
  }
  //init vibration motor pin
  //pinMode( 6 , OUTPUT);
  //init
  servo[C].write(90);
  delay(15);
  servo[RL].write(90);
  delay(15);
  handle.attach(handleMotorPin);
  delay(15);
  handle.write(handleAngle);
  delay(15);
  initGyro();
  delay(15);
  //
  // @}
  //

  // @{
  // set ultrasonography sensors
  //
  for (int i = 0 ; i < NUM_ULTRA ; i++) {
    pinMode(trigPin[i], OUTPUT);
    pinMode(echoPin[i], INPUT);
    delay(3);
  }
  //
  // @}
  //
  // 칼만 필터에서 사용 되는 타이머
 avg = getAvg();
  preTime = millis();
}

/*===========================================================================
   Loop
  ============================================================================== */
void loop() {
  
  startObstacDetect();
}

void startObstacDetect() {
  /* @{
     startObstacDetect:
      순차적으로 장애물을 감지한다.
      0) 3초 단위로 작업 수행
      3) 장애물 감지: 상하 -> 좌우
      @}
  */
  currentTime = millis();
  if (currentTime - preTime >= duration) { // 3초마다 모터 움직이도록 조정
    
    updateGyroValue();
    rotateServoMotorForwards();
    checkBlock();
    
    preTime = currentTime;
  } else {
    updateGyroValue();
  }
  
}
/*
* 상 중 하를 향하는 서보 모터가 고정된 형태로 수행함.
*/

void checkBlock(){
  int limit = 80;
  int result = 0;
  result |= testIsBlocked(0,limit) | testIsBlocked(1,limit) |  testIsBlocked(2,avg);
  
  if(result == 1){
     int direction = moveUltraMotorRightAndLeft();
    /*
    * 해당 방향에 맞게 손잡이 회전 또는 진동 알림
    */
      if (direction == RIGHT) {
            Serial.println("오른쪽");
             changeHandleAngle(direction);
          } else if (direction == CENTER) {
            Serial.println("중앙");
            changeHandleAngle(direction);
          } else if(direction == LEFT){
            Serial.println("왼쪽");
            changeHandleAngle(direction);
          }else if(direction == 0){
             Serial.println("막힘");
             alarm(2,100,500);
        }
  }else{
      changeHandleAngle(CENTER);
  }
  
}

float getAvg(){
  float sum = 0;
  for(int i = 0 ; i< 10 ; i++ ){
      if(i !=  0 ) sum+= sensingUltra(2);
  }
  Serial.println("구해진 평균은 : "+String(sum/9));
  return sum/9;
}


int moveUltraMotorRightAndLeft(){
  int angle = GAP_START;
  int start = GAP_START;
  int end = GAP_END;
  int count = 0; // 빈 공간 각도 개수.
  int eCount = 0; // 문제 있는 공간 (error place) 의 개수. (emptyRate를 계산하기 위해 사용한다.)
  int preVal = 0; // 연속적으로 빈 공간이 있는지 확인하기 위한 변수. 이전 장애물 여부 값.
  int curVal = 0; // 위와 같은 의도로 사용되는 변수. 현재 장애물 여부 값.
  int mDirection = 0; // 방향.
  float emptyRate = 0.0f; // 중간 중간 이상한 값이 나타날 경우 (이상한 값 기준: 1이거나 -1등 에러 값)
  // 해당 값을 무시하기 위해 비율을 계산한다.
  // 만약 20% 이하로 전방이 비어있다면, 해당 방향으로 길 안내를 제시한다.
  for (int i = 0; i < GAP_END - GAP_START ; i++, angle ++ ) {
    servo[RL].write(angle);
    delay(5);
    // @{ 연속적으로 빈 공간인지 체크하기 위한 조건문.
    if ((curVal = (testIsBlocked(SHANG,30) | testIsBlocked(ZONG,30)  | testIsBlocked(HA,30))) == preVal && preVal == 0) {
   if (count == 0) start = angle;  // 시작각도 구하기.
      count ++;
    } else if (count > 0) {
      eCount ++;
      emptyRate = eCount / count * 100;
      //Serial.println(String(eCount)+"/"+String(count)+"*100 ="+String(emptyRate));
      if (emptyRate < 10) {
        curVal = 0; // emptyRate가 20%보다 작을 경우, 강제로 현재 장애물 여부 변수를
        // '장애물 없음'으로 지정한다. 따라서 다음 iteration에는
        // 장애물이 연속적으로 없다고 판단하고 측정한다.
        continue;
      }
      else {    // 만약 emptyRate가 20%보다 클 경우에는 이미 빈공간이 아니라는 판단을 한 것.
        count = 0;
        eCount = 0;
      }
    }
    // @}

    // @{ 만약 빈 공간이 통과할 만큼 많다?, 바로 방향 알려주고 나가기. SEQUENCE_LIMIT : 30
    //
    if (count >= SEQUENCE_LIMIT) {
      end = angle;
      if(mDirection == 0)
           mDirection = calculateDirection(start, end);
      
      //angle = GAP_END - GAP_START;
      //Serial.println("start : "+ String(start) +" end: "+ String(end));
      if(angle == 115){
        mDirection = CENTER; 
        Serial.println("중앙으로 가세요....");
        break;
      }
    }
    // @}
    //Serial.print(curVal);
    preVal = curVal;
  }
  //Serial.println();
  if (angle == GAP_END && count < SEQUENCE_LIMIT) Serial.println("빈 공간 못찾음:" + String(count));
  return mDirection;
}

/*****************************************************************************************************/

/**
   시스템 구동시, 모터 초기 각도 계산.
   tilt : 자이로센서로 구한 지팡이 기울기(각도º)
   HYPO_TOP : 위아래 장애물을 감지하는 센서의 지팡이 상의 위치
   height : 위아래 장애물 감지 센서의 바닥과의 직각 거리 (높이),  l*sin(cane)
*/

/**
   구간의 시작 각도와 끝각도를 가지고 방향 결정.
*/
int calculateDirection (int s, int e) {
  int mid = (s + e) / 2;
  delay(1);
  if (mid < 90) {
   
    return RIGHT;
  } else if (mid == 90) {
    return CENTER;
  } else {
   
    return LEFT;
  }
}

float getStartAng(int r, float tilt)
{
  float height = HYPO_TOP * sin(radians(tilt));
  float x = height / HYPO_TOP;
  float rad_x = acos(x);
  float ang_x = rad_x / 3.141592654 * limit[1];
  return ang_x;
}

/*
   시스템 구동시, 모터 종료 각도 계산
   tilt : 자이로센서로 구한 지팡이 기울기(각도º)
   HYPO_TOP : 위아래 장애물을 감지하는 센서의 지팡이 상의 위치
   height : 위아래 장애물 감지 센서의 바닥과의 직각 거리 (높이),  l*sin(caneTilt)
*/
float getEndAng(int r, float tilt)
{
  float height = HYPO_TOP * sin(radians(tilt));
  float z = (180 - height) / r;
  float rad_z = asin(z);
  float ang_z = rad_z / 3.141592654 * limit[1];

  return ceil(ang_z) + 100;
}


void rotateServoMotorForwards() {
  updateGyroValue();
  double pitch = ceil(getPitch());
  if (abs(pitch) >= 10) {
    int currentServoAng = servo[C].read();
     //Serial.println("pitch = "+String(pitch)+", currentServoAng ="+ String(currentServoAng)+" , 조정 값=" + (currentServoAng - pitch));
    if (currentServoAng - pitch > 0 and currentServoAng - pitch < 180) {
      currentServoAng = currentServoAng - pitch;
      servo[C].write(currentServoAng);
      delay(15);
      updateGyroValue();
    }
  }
}

void alarm(int cnt, int delay1, int delay2) {
  //진동모터 알림
  for(int i = 0 ; i< cnt ; i++){
     Serial.println("알람...."+String(i));
     analogWrite(vibrationPin,150);
     delay(delay1);
     analogWrite(vibrationPin,0);
     analogWrite(vibrationPin,150);
     delay(delay2);
     analogWrite(vibrationPin,0);
  }
}

/**
   현재 핸들의 방향과 변경할 핸들의 방향이 다르면 핸들 방향 변경.
*/

void changeHandleAngle(int pos) {
  if (handleAngle != pos) {
    handleAngle = pos;
    if(pos == RIGHT) Serial.println("오른쪽으로 이동하세요");
    if(pos == CENTER) Serial.println("중앙으로 이동하세요");
    if(pos == LEFT) Serial.println("왼쪽으로 이동하세요");
    handle.write(handleAngle);
    delay(20);
  }
}

void printStr(String head, float value) {
  Serial.print(head);
  Serial.print(" : ");
  Serial.println(value);
}
