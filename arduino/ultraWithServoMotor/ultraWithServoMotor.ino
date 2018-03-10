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
const int limit[] = {60, 100};
const int ultraMotorPin[] = {A2, A3};
const int echoPin[] = {3, 6, 10};
const int trigPin[] = {5, 9, 11};
float distance[2] = {0}; //R,L

int minAngle = 100;
int maxAngle = 180;
int toStickFromBottom = 0;

int ultraMotorAngle[] = {20, 0};

Servo servo[2];
Servo handle;

int handleAngle = CENTER;
int isBlocked[GAP];

const int handleMotorPin = A0;

/* ========================================================
   timer
  ===========================================================*/
unsigned long preTime = 0;
unsigned long  currentTime = 0;
unsigned int duration = 3000;

bool mBreak = true;

typedef struct {
  int start;
  int end;
  int length;
  int direction;
} Section;

#define SECTION_NUM 6
Section sections[SECTION_NUM];

/* ---------------------------------------------------
 *  interrupt button
  -----------------------------------------------------*/
int click_state;
const int buttonPin = 9;


void setup() {
  Serial.begin(9600);
  servo[RL].attach(ultraMotorPin[RL]);
  delay(15);
  servo[UL].attach(ultraMotorPin[UL]);
  delay(15);
  handle.attach(handleMotorPin);
  delay(15);
  handle.write(handleAngle);
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
    toStickFromBottom = sensingUltra(B);
    if(toStickFromBottom!= -1){
      Serial.println(toStickFromBottom);
      moveUltraMotorUpAndDown();
      delayMicroseconds(10);
    }

    preTime = currentTime;
  } else {

  }
}
/*
   if Sequence's number of 0 is over 30, Store it in the sections array
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

float endAng(float r, float l, float a)
{
  float z = (180 - a) / r;
  float rad_z = asin(z);
  float ang_z = rad_z / 3.141592654 * 180;

  return ang_z + 100;
}

void moveUltraMotorUpAndDown()
{
  int pos = startAng(180, 52, toStickFromBottom);
  float pe = endAng(180, 52, toStickFromBottom);
  int sum = 0;
  if (pos <= maxAngle || pos >= 0)
  {
    for (int i = pos; i < endAng(180, 52, toStickFromBottom); i++)
    {
      servo[UL].write(i);
      sum += sensingUltra(UL);
      delay(10);
    }
    Serial.print("\n");
    for (int i = endAng(180, 52, toStickFromBottom); i >= pos; i--)
    {
      servo[UL].write(i);
      delay(5);
    }
  }
  if (sum > 1)
    findDirection();
  else
    changeHadleAngle(CENTER);
}

void findDirection() {
  moveUltraMotorRightAndLeft();
  int size = getSection();
  Serial.print("section 수");
  Serial.println(size);
  int direction = chooseSection(size);
  if (direction != -1)
    changeHadleAngle(direction);
  else
    Serial.println("막힘.....");
}

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

int getSection() {
  int s, e, count = 0;
  int pre = 1;
  int sectionIndex = 0 ;
  int isAllZero = true;
  for (int i = 0 ; i < GAP_END - GAP_START ; i ++) {
    /*
       pre right
       0  -> 1 : Store sequence's value in the sections array  (condition: sequence's length is more than 30)
       1  -> 0 : Store index of right array in the start variable and the end variable
       0  -> 0 : Increase count and end value
       count is sequence length
    */
    if (pre != isBlocked[i]) {
      if (isBlocked[i] == 0) { // 1->0
        s = i;
      } else if (isBlocked[i] == 1) { // 0 -> 1
        if (count > SEQUENCE_LIMIT) {
          sections[sectionIndex].start = s;
          sections[sectionIndex].end = e;
          sections[sectionIndex].length = e - s;
          sections[sectionIndex].direction = caculateDirection(s, e);
          sectionIndex++;
          isAllZero = false;
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
  //when there is no obstacle,isAllZero is true
  if (isAllZero) {
    sections[sectionIndex].start = 0;
    sections[sectionIndex].direction = CENTER;
    sections[sectionIndex].end = 180;
    sections[sectionIndex].length = e - s;
    sectionIndex++;
  }
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
  return sectionIndex;
}

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

int chooseSection(int size) {
  int s, e;
  int max = 0;
  if (size == 0) {
    return -1;
  } else if (size == 1) {
    return sections[0].direction;
  } else {
    /*
       search direction
       priority : section's length
    */
    for (int i = 1 ;  i < size ; i++) {
      if (sections[max].length < sections[i].length)
        max = i;
    }
    return sections[max].direction;
  }
}

void changeHadleAngle(int pos) {
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
    if (mDistance > 5.0)
      return mDistance;
    else
      return -1;
  } else {
    //Ignore odd value
    if (mDistance > 2.0 && mDistance < limit[i]) {
      Serial.print("1");
      Serial.print(mDistance);
      Serial.print(" ");
      delayMicroseconds(10);
      return 1;
    } else {
      Serial.print("0");
      return 0;
    }
  }
}
