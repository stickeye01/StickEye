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
#define UL 1 // Uppder and Lower
#define B 2 //Bottom

/*========================================================
   ultra sensor and servo motor
  ========================================================*/
//index 0은 오른쪽 왼쪽 측정, 1은 위아래
const int limit[] = {60, 100}; 

const int ultraMotorPin[] = {A2, A3};
const int echoPin[] = {3, 6, 10};
const int trigPin[] = {5, 9, 11};
float distance[2] = {0}; //장애물과의 거리 index 0은 오른쪽 왼쪽 측정, 1은 위아래

int minAngle = 100;
int maxAngle = 180;
int toStickFromBottom = 0;

int ultraMotorAngle[] = {20, 0};

Servo servo[2];
Servo handle;

int handleAngle = CENTER;
int isBlocked[GAP]; //index에 해당되는 각도에서 limit 범위 내에 장애물이 측정되면 1 아니면 0

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
  servo[UL].attach(ultraMotorPin[UL]);
  delay(15);
  handle.attach(handleMotorPin);
  delay(15);
  handle.write(handleAngle);
  delay(15);
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
  if (currentTime - preTime >= duration) {
    //지팡이와 바닥까지의 거리를 측정함(위 아래 움직일 때의 시작각도와 끝각도를 구하기 위해).
    toStickFromBottom = sensingUltra(B);
    Serial.println(toStickFromBottom);
    //위아래 측정시 영역안에 장애물이 한개라도 있을 경우 true 아니면 false
    bool mIsBlocked = moveUltraMotorUpAndDown();
    if (mIsBlocked) {
      checkRightLeft();
    } else {
      changeHandleAngle(CENTER);
    }
    delayMicroseconds(10);
    preTime = currentTime;
  } else {

  }

}
void checkRightLeft() {
  moveUltraMotorRightAndLeft();
  int direction = findDirection();
  if (direction != 0)
    changeHandleAngle(direction);
  else
    Serial.println("막힘.....");
}

/*
   모터가 위 아래로 1도 간격으로 움직이며 초음파 센서로 장애물과의 거리 측정
*/
bool moveUltraMotorUpAndDown()
{
  int pos = startAng(180, 52, 50);
  float pe = endAng(180, 52, 50);
  int sum = 0;
  int result  = 0;
  if (pos <= maxAngle || pos >= 0)
  {
    for (int i = pos; i < pe; i++)
    {
      servo[UL].write(i);
      result = sensingUltra(UL);
      if (result == 1 &&  i != pos) {
        return true;
      }
      delay(5);
    }
    Serial.print("\n");
    for (int i = pe; i >= pos; i--)
    {
      servo[UL].write(i);
      delay(5);
    }
  }
  return false;
}
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

float endAng(float r, float l, float a)
{
  float z = (180 - a) / r;
  float rad_z = asin(z);
  float ang_z = rad_z / 3.141592654 * 180;

  return ang_z + 100;
}

/*
   좌우로 움직이며 초음파 센서로 장애물 여부를 판단
*/
void moveUltraMotorRightAndLeft() {
  int angle = GAP_START;
  int end = GAP_END;
  for (int i = 0; i < GAP_END - GAP_START ; i++, angle ++ ) {
    servo[RL].write(angle);
    delay(5);
    isBlocked[i] = sensingUltra(RL);
    delay(5);
  }
  Serial.println();
  for (int i = 0 ; i <  GAP_END - GAP_START  ; i++ , angle--) {
    servo[RL].write(angle);
    delay(5);
  }
}

/*전역 변수인 isBlock 배열을 확인하여 0이 30개 이상 연속된 구간(장애물이 없는 구간)이 있는지 찾는 함수.
  장애물이 없는 구간이 있을 경우 Section 구조체 배열에 저장한 후 가장 각도가 (끝각도-시작각도) 큰 값의 방향으로 핸들을 움직임
  사방이 막혔다고 판단되면 return -1 ( 진동 )*/
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
          mDirection = caculateDirection(s, e);
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

  /*
      구간 확인하기 위한 출력 구문
    if (sectionIndex != 0) {
    Serial.println();
    Serial.print("구간 : length : ");
    Serial.print(sections[0].length);
    delay(1);
    Serial.print(" ");
    Serial.print(sections[0].start);
    Serial.print(" - ");
    Serial.println(sections[0].end);
    delay(1);
    Serial.print("direction: ");
    Serial.println(sections[0].direction);
    delay(1);
    }
  */
  if (count >= SEQUENCE_LIMIT) {
    mDirection = caculateDirection(s, e);
  }
  return mDirection;
}

//구간의 시작 각도와 끝각도를 가지고 방향 결정
int caculateDirection(int s, int e) {
  int mid = s + e / 2;
  delay(1);
  if (mid < 90) {
    return RIGHT;
  } else if (mid == 90) {
    return CENTER;
  } else {
    return LEFT;
  }
}

//현재 핸들의 방향과 변경할 핸들의 방향이 다르면 핸들 방향 변경
void changeHandleAngle(int pos) {
  if (handleAngle != pos) {
    handleAngle = pos;
    handle.write(handleAngle);
    delay(20);
  }
}


int sensingUltra(int i) {
  // 초음파를 보낸다. 다 보내면 echo가 HIGH 상태로 대기하게 된다.
  digitalWrite(trigPin[i], LOW);
  digitalWrite(echoPin[i], LOW);

  delayMicroseconds(2);
  digitalWrite(trigPin[i], HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin[i], LOW);

  // echoPin 이 HIGH를 유지한 시간을 저장 한다.
  unsigned long  mDuration = pulseIn(echoPin[i], HIGH);
  delayMicroseconds(100);
  // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
  float mDistance = mDuration / 29.0 / 2.0;
  delayMicroseconds(100);
  if (i == B) {
    if (mDistance > 1.0)
      return mDistance;
    else
      return -1;
  } else {
    //이상한 값이거나 limit보다 장애물과의 거리가 짧으면 1을 리턴한다.
    if (mDistance > 2.0 && mDistance < limit[i]) {
      Serial.print("1");
      //Serial.print(mDistance);
      Serial.print(" ");
      delayMicroseconds(5);
      return 1;
    } else {
      Serial.print("0");
      return 0;
    }
  }
}

