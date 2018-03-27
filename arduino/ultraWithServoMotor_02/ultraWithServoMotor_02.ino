/*
   arduino mini's PMW pins (d3,d5,d6,d9,d10,d11)
*/
#include <Servo.h>
#define RIGHT 50
#define LEFT 130
#define CENTER 90

#define SEQUENCE_LIMIT 30.0 //(degree)
#define LIMIT_RL 60 //(height(cm) - 100cm)
#define LIMIT_UL 180

#define GAP 140
#define GAP_START 20
#define GAP_END 160
#define NUM_ULTRA 3
#define RL 0 // Right and left
#define UB 1 // Uppder and Bottom
#define B 2 //Bottom

/*========================================================
   ultra sensor and servo motor
  ========================================================*/
// index 0은 좌/우 측정, 1은 상/하 측정에 사용.
const int limit[] = {60, 100};

const int ultraMotorPin[] = {A2, A3};
const int echoPin[] = {3, 6, 10};
const int trigPin[] = {5, 9, 11};
float distance[2] = {0}; //장애물과의 거리: index 0은 좌/우 측정, 1은 상/하

// 회전 가능 각도 (최대/최소)
int minAngle = 100;
int maxAngle = 180;
int toStickFromBottom = 0; // 지팡이와 바닥까지의 거리

Servo servo[2];
Servo handle;

int handleAngle = CENTER; // 초기 지팡이 핸들 각도: 기본 센터,
int isBlocked[GAP]; // 장애물이 존재하는 지 측정하는 변수.
// limit 범위 내에 장애물이 측정될 경우 1, 아니면 0을 설정한다.
// 센서가 회전하는 각도를 GAP만큼의 개수로 균일하게 나누고,
// 균일하게 나눠진 각도만큼의 범위에 장애물 존재여부를 관리한다.

const int handleMotorPin = A0;

/* ========================================================
   timer
  ===========================================================*/
unsigned long preTime = 0;
unsigned long  currentTime = 0;
unsigned int duration = 3000;

/* ===========================================================================
    Section
     start : 0이 연속적으로 있는 구간의 시작 각도 , length : end-start
     end : 끝 각도 , direction : end+start/2(중간값)를 통해 판단한 방향
  ===========================================================================*/
typedef struct {
  int start;
  int end;
  int length;
  int direction;
} Section;

#define SECTION_NUM 6
Section sections[SECTION_NUM];

/*===================================================
  interrupt button
  =====================================================*/
int buttonPin = 2;
int clickState = 1;
/*===================================================
  setup
  =====================================================*/
bool mBreak = true;
void setup() {
  Serial.begin(9600);
  servo[RL].attach(ultraMotorPin[RL]);
  delay(15);
  servo[UB].attach(ultraMotorPin[UB]);
  delay(15);
  //handle.attach(handleMotorPin);
  //delay(15);
  //handle.write(handleAngle);
  //delay(15);
  pinMode(buttonPin, INPUT);
  clickState = 1;
  for (int i = 0 ; i < NUM_ULTRA ; i++) {
    pinMode(trigPin[i], OUTPUT);
    pinMode(echoPin[i], INPUT);
    delay(3);
  }
  preTime = millis();
}


void loop() {
  myTimer();
}

void myTimer() {
  currentTime = millis();
  if (currentTime - preTime >= duration) { // 3초마다 모터 움직이도록 조정.
    //지팡이와 바닥까지의 거리를 측정함 (위 아래 움직일 때의 시작각도와 끝각도를 구하기 위해).
    toStickFromBottom = sensingUltra(B);
    Serial.println(toStickFromBottom);
    //위아래 측정시 영역안에 장애물이 한개라도 있을 경우 true 아니면 false
    bool mIsBlocked = moveUltraMotorUpAndDown();
    if (mIsBlocked) {
      // checkRightLeft();
      checkRightLeft_o1();
    } else {
      Serial.println("중앙");
      //changeHandleAngle(CENTER);
    }
    delayMicroseconds(10);
    preTime = currentTime;
  } else {

  }
}

/**
 * 좌/우 방향 판단 (optimized version).
 * 현재는 아무것도 안하고 serial print로 출력함.
 */
void checkRightLeft_o1() {
  int direction = moveUltraMotorRightAndLeft_o1();
  if (direction != 0)
    if (direction == LEFT) {
      Serial.println("Direction: 왼쪽");
    } else if (direction == RIGHT) {
      Serial.println("Direction: 오른쪽");
    } else if (direction == CENTER) {
      Serial.println("Direction: 중앙");
    } else {
      Serial.println("Direction: 방향X");
    }
  else
    Serial.println("막힘.....");
}

/**
 * 좌/우 방향 판단
 */
void checkRightLeft() {
  moveUltraMotorRightAndLeft();
  int direction = findDirection();
  if (direction != 0)
    changeHandleAngle(direction);
  else
    Serial.println("막힘.....");
}

/**
 * 모터가 위 아래로 1도 간격으로 움직이는 동안에, 초음파 센서로 장애물과의 거리 측정.
 */
bool moveUltraMotorUpAndDown()
{
  int pos = startAng(180, 52, 50);
  float pe = endAng(180, 52, 50);
  int sum = 0;
  int result  = 0;
  if (pos <= maxAngle || pos >= 0)
  {
    for (int ang = pos; ang < pe; ang++) // for문을 돌며 모터 각도를 설정.
    {
      servo[UB].write(ang);
      delay(5);
      result = sensingUltra(UB); // 해당 거리에 물체가 있는가?
      if (result == 1 &&  ang != pos) { // Question: ang != pos를 한 이유는?
        return true;
      }
    }
    Serial.print("\n");
    for (int ang = pe; ang >= pos; ang--) // Question: 돌아가면서 체크는 안하는가?
    {
      servo[UB].write(ang);
      delay(5);
    }
  }
  return false;
}

/**
 * 시스템 구동시, 모터 초기 각도 계산.
 */
float startAng(float r, float l, float a)
{
  float x = a / l;
  float rad_x = asin(x);
  float ang_x = rad_x / 3.141592654 * 180;

  float y = a / r;
  float rad_y = asin(y);
  float ang_y = rad_y / 3.141592654 * 180;

  return ang_x - ang_y;
}

/**
 * 시스템 구동시, 모터 종료 각도 계산
 */
float endAng(float r, float l, float a)
{
  float z = (180 - a) / r;
  float rad_z = asin(z);
  float ang_z = rad_z / 3.141592654 * 180;

  return ang_z + 100;
}

/**
 * 좌우로 움직이며 초음파 센서로 장애물 여부를 판단 (optimized version).
 * 여기서 optimized는 time optimization이다.
 * 기존 함수는 한 바퀴 모두 돈 뒤에 안전 방향을 결정하였다.
 * 하지만, 이 버전의 함수에서는
 * 모터가 회전하며 센서가 탐지하는 동안,
 * SEQUENCE_LIMIT 만큼의 빈칸이 있을 경우, 한 바퀴 도는 것을 멈추고 바로
 * 방향 안내한다.
 */
int moveUltraMotorRightAndLeft_o1() {
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
    if ((curVal = sensingUltra(RL)) == preVal && preVal == 0) {
      if (count == 0) start = angle;  // 시작각도 구하기.
      count ++;
    } else if (count > 0) {
      eCount ++;
      emptyRate = eCount / count * 100;
      Serial.println(String(eCount)+"/"+String(count)+"*100 ="+String(emptyRate));
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
      angle = GAP_END - GAP_START;
      break;
    }
    // @}

    Serial.print(curVal);
    preVal = curVal;
  }
  Serial.println();

  if (angle == GAP_END && count < SEQUENCE_LIMIT) Serial.println("빈 공간 못찾음:" + String(count));
  for (int i = 0 ; i <  GAP_END - GAP_START  ; i++ , angle--) {
    servo[RL].write(angle);
    delay(5);
  }

  return mDirection;
}

/**
 * 좌우로 움직이며 초음파 센서로 장애물 여부를 판단.
 */
void moveUltraMotorRightAndLeft() {
  int angle = GAP_START;
  int end = GAP_END;
  for (int i = 0; i < GAP_END - GAP_START ; i++, angle ++ ) {
    servo[RL].write(angle);
    delay(5);
    isBlocked[i] = sensingUltra(RL);
  }
  Serial.println();
  for (int i = 0 ; i <  GAP_END - GAP_START  ; i++ , angle--) {
    servo[RL].write(angle);
    delay(5);
  }
}

/**
 * 전역 변수인 isBlock 배열을 확인하여 0이 30개 이상 연속된 구간(장애물이 없는 구간)이 있는지 찾는 함수.
 * 장애물이 없는 구간이 있을 경우 Section 구조체 배열에 저장한 후 가장 각도가 (끝각도-시작각도)
 * 큰 값의 방향으로 핸들을 움직임.
 * 사방이 막혔다고 판단되면 return -1 ( 진동 ).
 */
int findDirection() {
  int s, e, count = 0;
  int pre = 1;
  int mDirection = 0;
  int isAllZero = true;
  for (int i = 0 ; i < GAP_END - GAP_START ; i ++) {
    /*
      이전의 isBlock 값(pre)과 현재의 isBlock 값을 비교 (isBlock이 0이면 해당 각도의 limit 거리 내에 장애물이 없음. 1이면 있음)
      다를 경우는 2가지인데
       0->1 : 0에서 1일 때 개수를 확인하여 30이상이면 Section 구조체를 구조체 배열에 저장하고 개수 변수(count)를 초기화한다.
       1->0 :1에서 0이면 시작각도 값(start)를 현재의 각도 값으로 저장한다.
       1 -> 1 : 1에서 1일때 장애물이 있으므로 isAllZero를 false로 저장하여 이후 모든 구간에 장애물이 없는지 판단할때 사용함
       0 -> 0 : 0에서 0일때 0인 구간이 연속되고 있으므로 끝 각도를 현재의 각도로 설정한다.
    */
    if (pre != isBlocked[i]) {
      if (isBlocked[i] == 0) { // 1->0
        s = i;
      } else if (isBlocked[i] == 1) { // 0 -> 1
        if (count >= SEQUENCE_LIMIT) {
          isAllZero = false;
          mDirection = calculateDirection(s, e);
          return mDirection;
        }
        count = 0;
      }
    } else {
      if (isBlocked[i] == 0) { //0-> 0
        e = i;
        count ++;
      } else
        // 1 -> 1
        isAllZero = false;
    }
    pre = isBlocked[i];
  }
  if (isAllZero) {
    return RIGHT;
  }
  if (count >= SEQUENCE_LIMIT) {
    mDirection = calculateDirection(s, e);
  }
  return mDirection;
}

/**
 * 구간의 시작 각도와 끝각도를 가지고 방향 결정.
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
 * 현재 핸들의 방향과 변경할 핸들의 방향이 다르면 핸들 방향 변경.
 */
void changeHandleAngle(int pos) {
  if (handleAngle != pos) {
    handleAngle = pos;
    handle.write(handleAngle);
    delay(20);
  }
}

/**
 * 초음파 값을 측정하기.
 */
int sensingUltra(int sensorType) {
  // 초음파를 보낸다. 다 보내면 echo가 HIGH 상태로 대기하게 된다.
  digitalWrite(trigPin[sensorType], LOW);
  digitalWrite(echoPin[sensorType], LOW);

  delayMicroseconds(2);
  digitalWrite(trigPin[sensorType], HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin[sensorType], LOW);

  // echoPin 이 HIGH를 유지한 시간을 저장 한다.
  unsigned long  mDuration = pulseIn(echoPin[sensorType], HIGH);
  delayMicroseconds(100);
  // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
  float mDistance = mDuration / 29.0 / 2.0;
  //delayMicroseconds(100); // QUESTION: 왜 멈출까?
  if (sensorType == B) { // 지면까지의 거리
    if (mDistance > 1.0)
      return mDistance;
    else
      return -1;
  } else { // 좌/우, 상/하
    //이상한 값이거나 limit보다 장애물과의 거리가 짧으면 1을 리턴한다.
    if (mDistance > 2.0 && mDistance < limit[sensorType]) {
      return 1;
    } else {
      return 0;
    }
  }
}

