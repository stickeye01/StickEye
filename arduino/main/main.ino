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
#define NUM_ULTRA 2
#define RL 0 // Right and left
#define UB 1 // Uppder and Bottom
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
const int limit[] = {100, 50};

const int servoMotorPin[] = {A2, A3, A1};
const int echoPin[] = {11, 5};
const int trigPin[] = {10, 3};

float distance[2] = {0}; //장애물과의 거리: index 0은 좌/우 측정, 1은 상/하

// 회전 가능 각도 (최대/최소)
int minAngle = 100;
int maxAngle = 180;

Servo servo[3];
Servo handle;

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
  pinMode( 6 , OUTPUT);
  //init
  servo[C].write(90);
  delay(15);
  servo[UB].write(0);
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
      1) 내리막길 감지-: isCliff();
      2) 오르막길 감지-: isUpHill();
      3) 단순 장애물 감지: 상하 -> 좌우
      @}
  */
  currentTime = millis();
  if (currentTime - preTime >= duration) { // 3초마다 모터 움직이도록 조정

    double roll = ceil(getRoll());

    //printStr("각도 : ", roll);
    updateGyroValue();
    rotateServoMotorForwards();
    int startAng = getStartAng(180, roll);
    int endAng = getEndAng(180, roll);
    
   //printStr("pitch 각도 : ", getPitch());
   //printStr("servo : ", servo[C].read());
   
    bool mIsBlocked = moveUltraMotorUpAndDown_2(startAng, 90);
    //위아래 측정시 영역안에 장애물이 한개라도 있을 경우 true 아니면 false
    if (mIsBlocked) {
      checkRightLeft();
    } else {
      Serial.println("중앙");
      changeHandleAngle(CENTER);
    }
    delayMicroseconds(10);
    preTime = currentTime;

  } else {
    updateGyroValue();
  }
}

bool moveUltraMotorUpAndDown_2(int startAngle, int endAngle) {
  int sum = 0;
  int result  = 0;
  int count=0;
  int ecount=0;
  int preVal=0;
  int curVal=0;
  /*
   * preVal : 이전 장애물 있는지 확인
   * curVal : 현재 장애물 있는지 확인
   * result : (count-ecount)/count * 100
   */
  if (startAngle <= maxAngle || startAngle >= 0)
  {
    for (int ang = startAngle; ang < endAngle; ang++) // for문을 돌며 모터 각도를 설정.
    {
      if (ang % 10 == 0) rotateServoMotorForwards();
      servo[UB].write(ang);
      delay(5);
      //Serial.println("자이로센서 값 : "+String(getPitch()));
      preVal = curVal;
      curVal = isBlocked(UB); // 해당 거리에 물체가 있는가?
      /*
       *  장애물을 연속으로 2번 파악한 경우 -> 11 인 경우
       *  count를 2로 초기화 / ecount도 2로 초기화
       *  ecount : 
       *  2로 초기화하는건, 첫줄에서 언급한 11때문에.
       */

    //curVal == 1 일때
      if(curVal==1){
         //최초로 1,1이 나타났을 떄 초기화
         if(count >= 2 ){  //11.......01 일때와 11......11일경우
             Serial.println("11....?1.");
             count ++;
             ecount++;
         }else if(count < 2 && preVal == 1){
             //최초 11 나왔을 때
             Serial.println("11......");
              count = 2;
              ecount = 2;
         }
      }else{
        //curVal이 0일때
        //11....................00
        if(count >= 2){
          /* 
           *  00이 나온 경우
           *  1이 5개 이상 존재하는가? 
           *  장애물이라고 측정된 비율이 80%이상인가?
          */
         
          if(preVal == 0){
             Serial.println("11.....00");
             float ratio = ((float)ecount/count)*100;
            if(ecount >= 5 && ratio > 80){
              Serial.println("장애물이 존재합니다.."+String(ecount)+"/"+String(count)+"*100="+String(ratio));
              count = 0;
              ecount = 0;
              return;
            }else{
               Serial.println("장애물은 없습니다.."+String(ecount)+"/"+String(count)+"*100="+String(ratio));
                count = 0;
                ecount = 0;
            }
          }else{
            Serial.println("11.....10...");
            count++;
          }
        }
        
      }
       
    }
    Serial.println();
  }
  return false;
}
/*****************************************************************************************************/


/**
   시스템 구동시, 모터 초기 각도 계산.
   tilt : 자이로센서로 구한 지팡이 기울기(각도º)
   HYPO_TOP : 위아래 장애물을 감지하는 센서의 지팡이 상의 위치
   height : 위아래 장애물 감지 센서의 바닥과의 직각 거리 (높이),  l*sin(cane)
*/

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

/*
  상하 서보 모터 움직이며 장애물 측정
*/
bool moveUltraMotorUpAndDown_1(int startAngle, int endAngle, int tilt) {
  int result  = 0;
  float distance = 0; //거리
  float currHeight = 0;
  float cosVal = 0;
  float currGap = 0;
  //=========== 계단 검사 ===============//
  boolean startedChecking = false;
  int stairsCount = 0; // 처음 currgap이 6이상인 순간 부터 gap이 abs(2)이하인 구간이 연속되면 나오는
  int stairsCount2 = 0; //
  int degree = 0;
  float preHeightForStairs = 0;
  //========== 낭떠러지 검사 =============//
  int boundary = tilt / 2; //낭떠러지 검사시 기울어짐으로 인한 초음파 센서 이상값 발생을 막기 위해 측정 바운더리 지정
  int slopeCount = 0; // 낭떠러지 검사 시 낭떠러지로 추정되는 값이 4개 이상 나와야 낭떠러지로 판단함
  float preHeightForSlope = 0;
  //====================================//
 
  if (startAngle <= maxAngle || startAngle >= 0)
  {
    for (int ang = 0 ; ang < endAngle; ang++) // for문을 돌며 모터 각도를 설정.
    {
      //5번에 한번씩 정면 보도록 중앙 서보모터 움직임
      
      if (ang % 10 == 0) rotateServoMotorForwards();
      servo[UB].write(ang);
      delay(1);
      
      //-------- 계단 & 낭떠러지 검사------------//
      distance = sensingUltra(UB);
      degree = 90 - tilt + ang;
      cosVal = cos(radians(degree));
      preHeightForStairs = currHeight; //지팡이에서 땅까지의 직각거리
      currHeight = distance * cosVal; //지팡이에서 땅까지의 직각거리
      
      //========= 계단 검사 =======================//
      if (cosVal >= 0 && ang != 0) {
        currGap = preHeightForStairs - currHeight;
        //Serial.println("currGap  = "+String(currGap)+"....");
        if(!startedChecking){
           if((stairsCount = checkStairs(stairsCount,currGap,startedChecking))==1){
              startedChecking = true;
           }
        }else if(startedChecking){
          stairsCount = checkStairs(stairsCount, currGap,startedChecking);
          if(stairsCount == 4){
            stairsCount2++;
            stairsCount = 0;
            startedChecking= false;
           // Serial.println("계단으로 의심되는 구간 " + String(stairsCount2) + "...");
          }
        }
        if(stairsCount2 > 4){
          //계단 알림
          alarm(2,15,30);
          stairsCount2 = 0;
        }
      }
      
      //========== 낭떠러지 검사 =============//
      // 0도에서 boundary(tilt/2)까지 앞에 낭떠리지 검사 : tilt/2이상일때 기울어짐으로 초음파 값 제대로 측정 안됨.
      if (ang < boundary) {
          currGap = preHeightForSlope - currHeight;
          slopeCount = checkSlope(slopeCount , currGap);
        if (slopeCount == 0  || preHeightForSlope == 0) {
          preHeightForSlope = currHeight;
        }
      }
      //========== 전방 장애물 검사 =============//
      if (ang >= startAngle) {
        if (result != 1 ) result = isBlocked(UB); //해당 거리에 물체가 있는가?
        else {
          //result가 1일 경우 시작 각도에서 초음파 센서의 맨 처음 측정값이 0이 나올 경우를 스킵하기 위한 조건문
          if (ang != startAngle && cosVal < 0)  return true;
        }
      }
    }
    Serial.println();
  }
  return false;
}
/*
   계단 체크
*/
int checkStairs(int count, float gap, boolean startedChecking) {
    if(!startedChecking){
      if(gap > 5 && gap <= 10){
        return 1;
      }
    }else{
      if (abs(gap) < 2) {
        //Serial.println("계단 체크 " + String(count + 1) + "....");
        return count + 1;
      }
    }
  return 0;
}

/* 바닥에 경사 확인
   startAng까지 앞 쪽에 낭떠러지가 있는지 확인한다.
*/
int checkSlope(int count, float gap) {
  /*전값과의 차이가 +15 이상일때 낭떠러지 구간을 측정
     count 중 일 때는 preHeight을 갱신하지 않는다.*///지팡이에서 땅까지의 직각거리
   if (gap < -20) {
    if (count > 4) {
      Serial.println(".......낭떠러지");
      alarm(2,5,10);
      return 0;
    }
    return count + 1;
  }
  return 0;
}

bool moveUltraMotorUpAndDown(int startAngle, int endAngle) {
  int sum = 0;
  int result  = 0;
  float pre = 0;
  if (startAngle <= maxAngle || startAngle >= 0)
  {
    for (int ang = startAngle; ang < endAngle; ang++) // for문을 돌며 모터 각도를 설정.
    {
      if (ang % 10 == 0) rotateServoMotorForwards();
      servo[UB].write(ang);
      delay(5);
      //Serial.println("자이로센서 값 : "+String(getPitch()));
      pre = result;
      result = isBlocked(UB); // 해당 거리에 물체가 있는가?
      /**/
      
      //========== 낭떠러지 검사 =============//
      // 0도에서 boundary(tilt/2)까지 앞에 낭떠리지 검사 : tilt/2이상일때 기울어짐으로 초음파 값 제대로 측정 안됨.
      //if (ang < boundary) {
      //    currGap = preHeightForSlope - currHeight;
      //    slopeCount = checkSlope(slopeCount , currGap);
      //  if (slopeCount == 0  || preHeightForSlope == 0) {
      //    preHeightForSlope = currHeight;
      //  }
      /*
         시작 각도에서 초음파 센서의 맨 처음 측정값이 0이 나올 경우를 스킵하기 위한 조건문
         시작 각도에서는 무조건 pass
      */
      if (result == 1 &&  ang != startAngle && pre == 1) {
        Serial.println("안전 구간 탐색을 시작합니다...");
        return true;
      }
    }
    Serial.println();
  }
  return false;
}
/**
   좌/우 방향 판단 (optimized version).
   현재는 아무것도 안하고 serial print로 출력함.
   막혔을 시 진동모터 진동
*/
void checkRightLeft() {
  int direction = moveUltraMotorRightAndLeft_1();
  if (direction != 0){
      //Serial.println("Direction: "+ String(direction));
      changeHandleAngle(direction);
  } else {
    Serial.println("막힘.....");
    alarm(2,100,500);
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
   좌우로 움직이며 초음파 센서로 장애물 여부를 판단 (optimized version).
   여기서 optimized는 time optimization이다.
   기존 함수는 한 바퀴 모두 돈 뒤에 안전 방향을 결정하였다.
   하지만, 이 버전의 함수에서는
   모터가 회전하며 센서가 탐지하는 동안,
   SEQUENCE_LIMIT 만큼의 빈칸이 있을 경우, 한 바퀴 도는 것을 멈추고 바로
   방향 안내한다.
*/
int moveUltraMotorRightAndLeft() {
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
    if ((curVal = isBlocked(RL)) == preVal && preVal == 0) {
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

    // @{ 만약 빈 공간이 통과할 만큼 많다?, 바로 방향 알려주고 나가기.
    if (count >= SEQUENCE_LIMIT) {
      end = angle;
      mDirection = calculateDirection(start, end);
      //angle = GAP_END - GAP_START;
      break;
    }
    // @}
    //Serial.print(curVal);
    preVal = curVal;
  }
  //Serial.println();
  if (angle == GAP_END && count < SEQUENCE_LIMIT) Serial.println("빈 공간 못찾음:" + String(count));
  return mDirection;
}


/*
 * 중앙 고려를 위한 버전 2
 * 지나갈수있다고 판단하는 지형인 30이므로 중앙이 나오려면  105
*/
int moveUltraMotorRightAndLeft_1() {
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
  //20~70/ 70~120 / 120~160//
  for (int i = 0; i < GAP_END - GAP_START ; i++, angle ++ ) {
    servo[RL].write(angle);
    delay(5);
    // @{ 연속적으로 빈 공간인지 체크하기 위한 조건문.
    if ((curVal = isBlocked(RL)) == preVal && preVal == 0) {
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
      mDirection = calculateDirection(start, end);
      //angle = GAP_END - GAP_START;
      Serial.println("start : "+ String(start) +" end: "+ String(end));
      if(angle == 115){
        mDirection = CENTER; 
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

/**
   현재 핸들의 방향과 변경할 핸들의 방향이 다르면 핸들 방향 변경.
*/
void changeHandleAngle(int pos) {
  if (handleAngle != pos) {
    handleAngle = pos;
    if(pos == RIGHT)  Serial.println("오른쪽으로 이동하세요");
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
