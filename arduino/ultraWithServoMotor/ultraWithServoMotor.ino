/*
 * arduino mini's PMW pins (d3,d5,d6,d9,d10,d11)
 */
#include <Servo.h>
#define RIGHT 50
#define LEFT 130
#define CENTER 90

#define GAP 180
#define SEQUENCE_LIMIT 30.0 //(degree)
#define LIMIT 60

#define NUM_ULTRA 2
#define R 0
#define L 1

/* =
 * ultra and servo motor 
 */
 
const int ultraMotorPin[] = {10,11};
const int echoPin[] ={3,6};
const int trigPin[] ={5,9};
float distance[2]={0}; //R,L

int ultraMotorAngle[] ={0,0};

Servo servo[2];
Servo handle;

int handleAngle = CENTER;

int isBlocked[GAP];

const int handleMotorPin = A0;
 
/* ========================================================
 * timer
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
}Section;

#define SECTION_NUM 6
Section sections[SECTION_NUM];

/* ---------------------------------------------------
 -----------------------------------------------------*/
 
void setup() {
  mBreak = false;
  Serial.begin(9600);
  servo[R].attach(ultraMotorPin[R]);
  delay(15);
  servo[L].attach(ultraMotorPin[L]);
  delay(15);
  handle.attach(handleMotorPin);
  delay(15);
  for(int i = 0 ; i<NUM_ULTRA ; i++){
      pinMode(trigPin[i], OUTPUT);
      pinMode(echoPin[i], INPUT);
      delay(3);
  }
  preTime = millis();
}

void loop() {
  if(mBreak){
    moveUltraMotor();
    Serial.println();
    int size = getSection();
    controlHandle(size);
    mBreak = false;
  }
  myTimer();
}

void myTimer(){
  currentTime = millis();
  if (currentTime - preTime >= duration) {
    moveUltraMotor();
     delayMicroseconds(10);
    int size = getSection();
     delayMicroseconds(10);
     controlHandle(size);
     delayMicroseconds(10);
     preTime = currentTime; 
  }else {

  }  
}
 /*
  * if Sequence's number of 0 is over 30, Store it in the sections array
 */ 
int getSection(){
  int s,e,count = 0;
  int pre = 1;
  int sectionIndex =0 ;

  for(int i = 0 ; i<GAP ; i ++){
     /*
      * pre right
      * 0  -> 1 : Store sequence's value in the sections array  (condition: sequence's length is more than 30)
      * 1  -> 0 : Store index of right array in the start variable and the end variable
      * 0  -> 0 : Increase count and end value
      * count is sequence length
      */
     if(pre != isBlocked[i]){
      if(isBlocked[i] == 0){ // 1->0
        s = i;
      }else if(isBlocked[i] == 1){ // 0 -> 1
          if(count > SEQUENCE_LIMIT){
              sections[sectionIndex].start = s;
              sections[sectionIndex].end = e;
              sections[sectionIndex].length = e-s;
              sections[sectionIndex].direction = getDirection(s,e);
              sectionIndex++;
          }
           count = 0;
      }
     }else{
       if(isBlocked[i] == 0){
          e = i;
          count ++;
       }
     }
     pre = isBlocked[i];
  }
  
  if(sectionIndex != 0){
         Serial.println();
         Serial.print("구간 : ");
         Serial.print(sections[0].length);
         delay(1);
         Serial.print(" ");
         Serial.print(sections[0].start);
         delay(1);
         Serial.print(" ");
         Serial.println(sections[0].end);
         delay(1);
  }

  return sectionIndex;
}

int getDirection(int s,int e){
  int mid = s+e/2;
  delay(1);
  if(mid <= 75){
     return LEFT;
  }else if(mid >75 && mid < 105){
    return CENTER;
  }else {
    return RIGHT;
  }
}

void moveUltraMotor(){
    for(int i = 0 ; i<GAP ; i++ ){
      ultraMotorAngle[R] +=1; 
      servo[R].write(ultraMotorAngle[R]);
      delay(5);
      isBlocked[i] = sensingUltra(R);
  }
  backUltraMotor();
}

void backUltraMotor(){
  for(int i = 0 ; i<GAP  ; i++ ){
    ultraMotorAngle[R] -=1;
    servo[R].write(ultraMotorAngle[R]);
    delay(5);
  }
}

void controlHandle(int size){
  int s,e;
  int max = 0;
  if(size == 0){
    //vibration
    Serial.println("막힘");
  }else if(size == 1){
    changeServoMotorAngle(sections[0].direction);
  }else{
    /*
     * search direction
     * priority : section's length
    */
    for(int i = 1 ;  i< size ; i++){
      if(sections[max].length < sections[i].length)
         max = i;
    }
  }
  changeServoMotorAngle(sections[max].direction);
}

void changeServoMotorAngle(int pos){
  if(handleAngle != pos){
    handleAngle = pos;
    handle.write(handleAngle);
    delay(15);
  }
}

int sensingUltra(int i){
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
    delayMicroseconds(10);
    /*
    Serial.print(mDistance);
    Serial.print(" ");
    delayMicroseconds(200);
    */
    if(mDistance < LIMIT){
       delayMicroseconds(10);
       return 1;
    }else{
       delayMicroseconds(10);
      return 0;
    }
    Serial.print(" ");
}

/*
void changeServoMotorAngle(char c){
  switch(c){
    case 'r':
    handleAngle = RIGHT;
    break;
    case 'c':
    handleAngle = CENTER;
    break;
     case 'l':
    handleAngle = LEFT;
    break;
  } 
    servo.write(handleAngle);
    delay(100);
}
*/

