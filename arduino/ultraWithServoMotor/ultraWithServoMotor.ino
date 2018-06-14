/*
   arduino mini's PMW pins (d3,d5,d6,d9,d10,d11)
*/
#include <Servo.h>
#include <Wire.h>
#include <Kalman.h> //Source: https://github.com/TKJElectronics/
//#define RESTRICT_PITCH // Comment out to restrict roll to ±90deg instead - please read: http://www.freescale.com/files/sensors/doc/app_note/AN3461.pdf

//Kalman kalmanX; // Create the Kalman instances
//Kalman kalmanY;
/* IMU Data */
//double accX, accY, accZ;
//double gyroX, gyroY, gyroZ;
//int16_t tempRaw;

//double gyroXangle, gyroYangle; // Angle calculate using the gyro only
//double compAngleX, compAngleY; // Calculated angle using a complementary
 
/*  
 *  kalAngleX값은 roll(좌우의 기울기). 초음파 센서의 기울어짐으로 인한 이상값 측정을 보정하기 위해 사용. 초음파 센서가 항상 정면의 위아래를 측정하도록 유도.
 *  kalAngleY값은 pitch(위아래 기울기). 지팡이의 기울기(tilt)
*/
//double kalAngleX, kalAngleY; // Calculated angle using a Kalman filter (X : roll(좌우), Y : pitch(위아래))

//uint32_t timer;
//uint8_t i2cData[14]; // Buffer for I2C data

/*
#include "I2Cdev.h"
#include "MPU6050_6Axis_MotionApps20.h"
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
    #include "Wire.h"
#endif
#define OUTPUT_READABLE_YAWPITCHROLL
*/

#define RIGHT 70
#define LEFT 110
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
#define C 2 //Bottom

#define HYPO_BOTTOM 19 //바닥 측정을 위해 달린 초음파 센서의 지팡이 상의 빗변 길이
#define HYPO_TOP 45 //위에 달린 초음파 센서의 지팡이 상의 빗변 길이
/*========================================================
  vibration 
  ========================================================*/
const int vibrationPin = A1;


/*========================================================
   ultra sensor and servo motor
  ========================================================*/
// index 0은 좌/우 측정, 1은 상/하 측정에 사용.
const int limit[] = {60, 80};

const int servoMotorPin[]=  {A2, A3, A1};
const int echoPin[] = {11,9,3};
const int trigPin[] = {10,6,5};

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
unsigned int duration = 3000;

/*===================================================
  Gyro Sensor
  SCL : A5
  SDA : A4
  INT : D2
  x : roll
  =====================================================*/
/*인터럽트핀 
#define INTERRUPT_PIN 2 

uint8_t mpuIntStatus;   // holds actual interrupt status byte from MPU
uint8_t devStatus;      // return status after each device operation (0 = success, !0 = error)
uint16_t packetSize;    // expected DMP packet size (default is 42 bytes)
uint16_t fifoCount;     // count of all bytes currently in FIFO
uint8_t fifoBuffer[64]; // FIFO storage buffer

bool dmpReady = false;
Quaternion q;           // [w, x, y, z]    
float ypr[3];           // [yaw, pitch, roll]   yaw/pitch/roll container and gravity vector
VectorFloat gravity;    // [x, y, z]            gravity vector
MPU6050 mpu;
*/
/*
volatile bool mpuInterrupt = false; 
//인터럽트 발생 시 호출되는 함수
void dmpDataReady() {
    mpuInterrupt = true;
}
*/

/*===================================================
  setup
  =====================================================*/

void setup() {
  Serial.begin(9600);
  // @{
  // set servo motors for ultrasonography
  //
  for(int i = 0 ; i<3 ; i++){
    servo[i].attach(servoMotorPin[i]); //A2,A3,A1
    delay(15);
  }
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

/*
 * 자이로 센서 초기화

void initGyro(){
   #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
        Wire.begin();
        TWBR = 24; // 400kHz I2C clock (200kHz if CPU is 8MHz)
    #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
        Fastwire::setup(400, true);
    #endif
    
    //mpu6050 센서 초기화
    mpu.initialize();
    
    //인터럽트핀 입력핀으로 설정
    pinMode(INTERRUPT_PIN, INPUT);

    //DMP 연결 확인    
    Serial.println(F("Testing device connections..."));
    Serial.println(mpu.testConnection() ? F("MPU6050 connection successful") : F("MPU6050 connection failed"));

    //dmp 초기화 
    //dmp : mpu6050 내부에 있는 Digital Mortion Processor
    devStatus = mpu.dmpInitialize();
    
    mpu.setXGyroOffset(220);
    mpu.setYGyroOffset(76);
    mpu.setZGyroOffset(-85);
    mpu.setZAccelOffset(1788);
    //상태 0 : 초기화 성공
     if (devStatus == 0) {
        // dmp 활성화
        Serial.println(F("Enabling DMP..."));
        mpu.setDMPEnabled(true);

        //인터럽트 핀과 함수 연결
        //인터럽트 핀이 Low에서 High로 될 때 dmpDateReady 함수 호출됨.
        Serial.println(F("Enabling interrupt detection (Arduino external interrupt 2)..."));
        attachInterrupt(digitalPinToInterrupt(INTERRUPT_PIN), dmpDataReady, RISING);
        mpuIntStatus = mpu.getIntStatus();

        //초기화 완료
        dmpReady = true;
        
        // fifo 패킷 사이즈 얻어오기
        packetSize = mpu.dmpGetFIFOPacketSize();

    } else {
        // ERROR!
        // 1 = initial memory load failed
        // 2 = DMP configuration updates failed
        // (if it's going to break, usually the code will be 1)
        Serial.print(F("DMP Initialization failed (code "));
        Serial.print(devStatus);
        Serial.println(F(")"));
    }   
}
 */

/*===========================================================================
 * Loop
============================================================================== */
void loop() {
  startObstacDetect();
}

void startObstacDetect() {
  /* @{
   * startObstacDetect:
   *  순차적으로 장애물을 감지한다.
   *  0) 3초 단위로 작업 수행
   *  1) 내리막길 감지-: isCliff();
   *  2) 오르막길 감지-: isUpHill();
   *  3) 단순 장애물 감지: 상하 -> 좌우
   *  @}
    */
  currentTime = millis();
  if (currentTime - preTime >= duration) { // 3초마다 모터 움직이도록 조정
    /*
     * tilt,start Angle, end Angle 구하기
     * tilt: 자이로 센서로 구한 지팡이의 기울기(각도º).
    */
    rotateServoMotorForwards();
    double roll = ceil(getRoll());
    float tilt = sin(radians(roll));    
    printStr("각도 : ", roll);
    printStr("각도 : ", tilt);
    
    int startAng = getStartAng(180,tilt);
    int endAng = getEndAng(180,tilt);
   
    //checkSlope(tilt,ceil(startAng));
    bool mIsBlocked = moveUltraMotorUpAndDown_1(startAng, endAng,tilt);

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


/**
 * 시스템 구동시, 모터 초기 각도 계산.
 * tilt : 자이로센서로 구한 지팡이 기울기(각도º)
 * HYPO_TOP : 위아래 장애물을 감지하는 센서의 지팡이 상의 위치
 * height : 위아래 장애물 감지 센서의 바닥과의 직각 거리 (높이),  l*sin(cane)
)
*/
 
float getStartAng(int r, float tilt)
{
  float height = HYPO_TOP * sin(radians(tilt));
  float x = height / HYPO_TOP;
  float rad_x = acos(x);
  float ang_x = rad_x / 3.141592654 * 180;
  printStr("startAng",ang_x); 
  return ang_x;
}

/*
 * 시스템 구동시, 모터 종료 각도 계산
 * tilt : 자이로센서로 구한 지팡이 기울기(각도º)
 * HYPO_TOP : 위아래 장애물을 감지하는 센서의 지팡이 상의 위치
 * height : 위아래 장애물 감지 센서의 바닥과의 직각 거리 (높이),  l*sin(caneTilt)
 */
float getEndAng(int r, float tilt)
{
  float height = HYPO_TOP*sin(radians(tilt));
  float z = (180 - height) / r;
  float rad_z = asin(z);
  float ang_z = rad_z / 3.141592654 * 180;
  
  if(isnan(ang_z)){
    ang_z= 50;
  }
  printStr("endAng",ang_z+100);
  return ceil(ang_z) + 100;
}


void rotateServoMotorForwards(){
     updateGyroValue();
     double pitch = ceil(getPitch());
     if(abs(pitch) >= 2){
        int currentServoAng = servo[C].read();
        if(currentServoAng - pitch > 0 and currentServoAng - pitch <180){
               currentServoAng = currentServoAng - pitch;
               servo[C].write(currentServoAng);
               delay(20);
               Serial.println("current angle = "+String(currentServoAng));
               updateGyroValue();
          }
     }
}

/* 바닥에 경사 확인
 * startAng까지 앞 쪽에 낭떠러지가 있는지 확인한다.
*/
void checkSlope(int tilt,int startAng){
  boolean isCounting= false;
  int count = 0;
  float preHeight;
  float hypo;
  int degree;
  float height;

  for(int ang = 0 ; ang <= tilt ; ang++ ){
    /*낭떠러지로 추정되는 구간이 나타났을 때*/
    //서보모터 아래에서 위로 움직임.
    servo[UB].write(ang);
    delay(5);
    
    degree = 90 - tilt + ang;
    hypo = sensingUltra(UB); 
    height = hypo *cos(radians(degree));
    delay(1);

    /*전값과의 차이가 15 이상일때 낭떠러지 구간을 측정
     * count 중 일 때는 preHeight을 갱신하지 않는다.*/
    
    if( (height - preHeight) > 15 && ang > 2){
      /*최초로 counting*/
      if(!isCounting) {
          count = 1;
          isCounting = true;
          Serial.println("갱신");
      }else{
         count++;
         if(count > 4){
            /*구간이 4이상이 될 경우 확실한 낭떠러지로 확신하여 알리고 나감*/
            Serial.println("낭떠러지 구간!");
            isCounting = false;
            //break;
         }
      }
    }else{
      if(isCounting){
            /* 오차 제거를 위함. 예를 들어 34 145 39와 같이 값이 한번 잘못 나온 경우를 제외 */
        if(count <= 3){
           isCounting = false;
           count = 0;
           Serial.println("리셋");
        }
      }else{
         preHeight  = height;
      }
    }
      
    Serial.print(degree);
    Serial.print(" 도 일때 hypo = ");
    Serial.print(hypo);
    Serial.print(" radians = ");
    Serial.print(cos(radians(degree)));
    Serial.print(" hieght = ");
    Serial.println(ceil(height));
    
   }
}

/*******************************************************************
*******************************************************************/

/**
 * 서보 모터 움직이며 장애물 측정
 */
 

bool moveUltraMotorUpAndDown_1(int startAngle, int endAngle, int tilt){
  int sum = 0;
  int result  = 0;
  int degree;
  float hypo;
  float height;
  if (startAngle <= maxAngle || startAngle >= 0)
  {
    for (int ang = 0 ; ang < endAngle; ang++) // for문을 돌며 모터 각도를 설정.
    {      
      if(ang % 3 == 0){//3번에 한번씩 정면 보도록 중앙 서보모터 움직임
        rotateServoMotorForwards();
      }
      servo[UB].write(ang);
      delay(5);

      //======================================================================
      // 0도에서 시작각도 전까지 앞에 낭떠리지 검사
       if(ang < startAngle){ 
          degree = 90 - tilt + ang;
          hypo = sensingUltra(UB); 
          height = hypo *cos(radians(degree));
          delay(1);
          Serial.println("hypo="+String(hypo));
         // Serial.println("height="+String(height));
       
      //======================================================================
      //시작각도 ~ 끝 각도까지 전방 장애물 검사
       }else if(ang >= startAngle){ 
          result = isBlocked(UB); // 해당 거리에 물체가 있는가?
        // 시작 각도에서 초음파 센서의 맨 처음 측정값이 0이 나올 경우를 스킵하기 위한 조건문
        // 시작 각도에서는 무조건 pass
        if (result == 1 &&  ang != startAngle) {   
          return true;
        }
      }
    }
    Serial.println();
    
    for (int ang = endAngle; ang >= startAngle; ang--) 
    {
      servo[UB].write(ang);
      delay(5);
    }
  }
  return false;
}

 
bool moveUltraMotorUpAndDown(int startAngle, int endAngle){
  int sum = 0;
  int result  = 0;
  if (startAngle <= maxAngle || startAngle >= 0)
  {
    for (int ang = startAngle; ang < endAngle; ang++) // for문을 돌며 모터 각도를 설정.
    {      
      servo[UB].write(ang);
      delay(5);
      result = isBlocked(UB); // 해당 거리에 물체가 있는가?
      
      /*
       * 시작 각도에서 초음파 센서의 맨 처음 측정값이 0이 나올 경우를 스킵하기 위한 조건문
       * 시작 각도에서는 무조건 pass
       */
      if (result == 1 &&  ang != startAngle) {   
        return true;
      }
    }
    Serial.println();
    for (int ang = endAngle; ang >= startAngle; ang--) 
    {
      servo[UB].write(ang);
      delay(5);
    }
  }
  return false;
}
/**
 * 좌/우 방향 판단 (optimized version).
 * 현재는 아무것도 안하고 serial print로 출력함.
 * 막혔을 시 진동모터 진동
 */
void checkRightLeft() {
  int direction = moveUltraMotorRightAndLeft();
  if (direction != 0)
    if (direction == LEFT) {
      Serial.println("Direction: 왼쪽");
      changeHandleAngle(LEFT);
    } else if (direction == RIGHT) {
      changeHandleAngle(RIGHT);
      Serial.println("Direction: 오른쪽");
    } else if (direction == CENTER) {
      changeHandleAngle(RIGHT);
      Serial.println("Direction: 중앙");
    } else {
      changeHandleAngle(CENTER);
      Serial.println("Direction: 방향 X");
    }
  else{
    Serial.println("막힘.....");
    /*
    analogWrite(vibrationPin,150);
    delay(10);
    analogWrite(vibrationPin,150);
    delay(100);
    analogWrite(vibrationPin,150);
    delay(10);
    analogWrite(vibrationPin,150);
    delay(100);
    */
  }
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
      angle = GAP_END - GAP_START;
      break;
    }
    // @}

    //Serial.print(curVal);
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
 * 초음파 센서의 측정값을 구함
 */
float isBlocked(int sensorType){
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
  float distance = mDuration / 29.0 / 2.0;
  
  if (distance > 2.0 && distance < limit[sensorType]) {
      //Serial.println(1);
      return 1;
    } else {
      //Serial.println(0);
      return 0;
    }
}

/**
 * 초음파 센서 구동 후 측정 거리 반환
 */
float sensingUltra(int sensorType){
    // 초음파를 보낸다. 다 보내면 echo가 HIGH 상태로 대기하게 된다.
  digitalWrite(trigPin[sensorType], LOW);
  digitalWrite(echoPin[sensorType], LOW);
  delay(10);
  digitalWrite(trigPin[sensorType], HIGH);
  delay(10);
  digitalWrite(trigPin[sensorType], LOW);

  // echoPin 이 HIGH를 유지한 시간을 저장 한다.
  unsigned long  mDuration = pulseIn(echoPin[sensorType], HIGH);
  //Serial.println("Duration: "+String(mDuration));
  delay(100);
  // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
  float distance = mDuration / 29.0 / 2.0;
  
  return distance;
}


void printStr(String head, float value){
   Serial.print(head);
   Serial.print(" : ");
   Serial.println(value);
}


