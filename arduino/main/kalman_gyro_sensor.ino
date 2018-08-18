/* Copyright (C) 2012 Kristian Lauszus, TKJ Electronics. All rights reserved.

 This software may be distributed and modified under the terms of the GNU
 General Public License version 2 (GPL2) as published by the Free Software
 Foundation and appearing in the file GPL2.TXT included in the packaging of
 this file. Please note that GPL2 Section 2[b] requires that all works based
 on this software must also be made publicly available under the terms of
 the GPL2 ("Copyleft").

 Contact information
 -------------------

 Kristian Lauszus, TKJ Electronics
 Web      :  http://www.tkjelectronics.com
 e-mail   :  kristianl@tkjelectronics.com
 */
/*
#include <Wire.h>
#include <Kalman.h> // Source: https://github.com/TKJElectronics/KalmanFilter
#include <Servo.h>

Servo servo[3];
*/
#define RESTRICT_PITCH // Comment out to restrict roll to ±90deg instead - please read: http://www.freescale.com/files/sensors/doc/app_note/AN3461.pdf


Kalman kalmanX; // Create the Kalman instances
Kalman kalmanY;

/* IMU Data */
double accX, accY, accZ;
double gyroX, gyroY, gyroZ;
int16_t tempRaw;

double gyroXangle, gyroYangle; // Angle calculate using the gyro only
double compAngleX, compAngleY; // Calculated angle using a complementary
 
/*  
 *  kalAngleX값은 roll(좌우의 기울기). 초음파 센서의 기울어짐으로 인한 이상값 측정을 보정하기 위해 사용. 초음파 센서가 항상 정면의 위아래를 측정하도록 유도.
 *  kalAngleY값은 pitch(위아래 기울기). 지팡이의 기울기(tilt)
*/
double kalAngleX, kalAngleY; // Calculated angle using a Kalman filter (X : roll(좌우), Y : pitch(위아래))


uint32_t timer;
uint8_t i2cData[14]; // Buffer for I2C data

// TODO: Make calibration routine

/*=========================================================
 *  initialize gyro sensor and kalman filter
===========================================================*/

void  initGyro(){
    Wire.begin();
#if ARDUINO >= 157
  Wire.setClock(400000UL); // Set I2C frequency to 400kHz
#else
  TWBR = ((F_CPU / 400000UL) - 16) / 2; // Set I2C frequency to 400kHz
#endif

  i2cData[0] = 7; // Set the sample rate to 1000Hz - 8kHz/(7+1) = 1000Hz
  i2cData[1] = 0x00; // Disable FSYNC and set 260 Hz Acc filtering, 256 Hz Gyro filtering, 8 KHz sampling
  i2cData[2] = 0x00; // Set Gyro Full Scale Range to ±250deg/s
  i2cData[3] = 0x00; // Set Accelerometer Full Scale Range to ±2g
  while (i2cWrite(0x19, i2cData, 4, false)); // Write to all four registers at once
  while (i2cWrite(0x6B, 0x01, true)); // PLL with X axis gyroscope reference and disable sleep mode

  while (i2cRead(0x75, i2cData, 1));
  if (i2cData[0] != 0x68) { // Read "WHO_AM_I" register
    Serial.print(F("Error reading sensor"));
    while (1);
  }

  delay(100); // Wait for sensor to stabilize

  /* Set kalman and gyro starting angle */
  while (i2cRead(0x3B, i2cData, 6));
  accX = (int16_t)((i2cData[0] << 8) | i2cData[1]);
  accY = (int16_t)((i2cData[2] << 8) | i2cData[3]);
  accZ = (int16_t)((i2cData[4] << 8) | i2cData[5]);
  // Source: http://www.freescale.com/files/sensors/doc/app_note/AN3461.pdf eq. 25 and eq. 26
  // atan2 outputs the value of -π to π (radians) - see http://en.wikipedia.org/wiki/Atan2
  // It is then converted from radians to degrees
#ifdef RESTRICT_PITCH // Eq. 25 and 26
  double roll  = atan2(accY, accZ) * RAD_TO_DEG;
  double pitch = atan(-accX / sqrt(accY * accY + accZ * accZ)) * RAD_TO_DEG;
#else // Eq. 28 and 29
  double roll  = atan(accY / sqrt(accX * accX + accZ * accZ)) * RAD_TO_DEG;
  double pitch = atan2(-accX, accZ) * RAD_TO_DEG;
#endif

  kalmanX.setAngle(roll); // Set starting angle
  kalmanY.setAngle(pitch);
  gyroXangle = roll;
  gyroYangle = pitch;
  compAngleX = roll;
  compAngleY = pitch;

  timer = micros();
}
/*
void myTimer(){
  currentTime = millis();
  if (currentTime - preTime >= duration) { 
    updateGyroValue();
    updateGyroValue();
    updateGyroValue();
    double roll = ceil(kalAngleX);
    float tilt = roll;    
    int startAng = getStartAng(180,tilt);
    int endAng = getEndAng(180,tilt);
    
    rotateServoMotorForwards();
    bool mIsBlocked = moveUltraMotorUpAndDown(0, floor(tilt));
    
  }else{
    updateGyroValue();
  }
}
*/
/* 
 *  초음파 센서의 본체가 앞쪽을 향하도록 자이로 센서의 측정하여 중앙 서보 모터를 조정함. 
 *  즉 사용자가 어떻게 지팡이를 잡아도 초음파 센서는 앞쪽을 향하게 되어 있음.

void rotateServoMotorForwards(){
     updateGyroValue();
     double pitch = ceil(kalAngleY);
     if(abs(pitch) >= 2){
        int currentServoAng = servo[C].read();
        if(currentServoAng - pitch > 0 and currentServoAng - pitch <180){
               currentServoAng = currentServoAng - pitch;
               servo[C].write(currentServoAng);
               delay(20);
               Serial.println("current angle = "+String(currentServoAng));
               updateGyroValue();
               Serial.println("pitch= "+String(kalAngleX));
          }
     }
}
*/

/* 
 *  자이로 센서 값 업데이트
*/

void updateGyroValue() {
  /* Update all the values */
  while (i2cRead(0x3B, i2cData, 14));
  accX = (int16_t)((i2cData[0] << 8) | i2cData[1]);
  accY = (int16_t)((i2cData[2] << 8) | i2cData[3]);
  accZ = (int16_t)((i2cData[4] << 8) | i2cData[5]);
  tempRaw = (int16_t)((i2cData[6] << 8) | i2cData[7]);
  gyroX = (int16_t)((i2cData[8] << 8) | i2cData[9]);
  gyroY = (int16_t)((i2cData[10] << 8) | i2cData[11]);
  gyroZ = (int16_t)((i2cData[12] << 8) | i2cData[13]);;

  double dt = (double)(micros() - timer) / 1000000; // Calculate delta time
  timer = micros();

  // Source: http://www.freescale.com/files/sensors/doc/app_note/AN3461.pdf eq. 25 and eq. 26
  // atan2 outputs the value of -π to π (radians) - see http://en.wikipedia.org/wiki/Atan2
  // It is then converted from radians to degrees
#ifdef RESTRICT_PITCH // Eq. 25 and 26
  double roll  = atan2(accY, accZ) * RAD_TO_DEG;
  double pitch = atan(-accX / sqrt(accY * accY + accZ * accZ)) * RAD_TO_DEG;
#else // Eq. 28 and 29
  double roll  = atan(accY / sqrt(accX * accX + accZ * accZ)) * RAD_TO_DEG;
  double pitch = atan2(-accX, accZ) * RAD_TO_DEG;
#endif

  double gyroXrate = gyroX / 131.0; // Convert to deg/s
  double gyroYrate = gyroY / 131.0; // Convert to deg/s

#ifdef RESTRICT_PITCH
  // This fixes the transition problem when the accelerometer angle jumps between -180 and 180 degrees
  if ((roll < -90 && kalAngleX > 90) || (roll > 90 && kalAngleX < -90)) {
    kalmanX.setAngle(roll);
    compAngleX = roll;
    kalAngleX = roll;
    gyroXangle = roll;
  } else
    kalAngleX = kalmanX.getAngle(roll, gyroXrate, dt); // Calculate the angle using a Kalman filter

  if (abs(kalAngleX) > 90)
    gyroYrate = -gyroYrate; // Invert rate, so it fits the restriced accelerometer reading
  kalAngleY = kalmanY.getAngle(pitch, gyroYrate, dt);
#else
  // This fixes the transition problem when the accelerometer angle jumps between -180 and 180 degrees
  if ((pitch < -90 && kalAngleY > 90) || (pitch > 90 && kalAngleY < -90)) {
    kalmanY.setAngle(pitch);
    compAngleY = pitch;
    kalAngleY = pitch;
    gyroYangle = pitch;
  } else
    kalAngleY = kalmanY.getAngle(pitch, gyroYrate, dt); // Calculate the angle using a Kalman filter

  if (abs(kalAngleY) > 90)
    gyroXrate = -gyroXrate; // Invert rate, so it fits the restriced accelerometer reading
  kalAngleX = kalmanX.getAngle(roll, gyroXrate, dt); // Calculate the angle using a Kalman filter
#endif

  gyroXangle += gyroXrate * dt; // Calculate gyro angle without any filter
  gyroYangle += gyroYrate * dt;
  //gyroXangle += kalmanX.getRate() * dt; // Calculate gyro angle using the unbiased rate
  //gyroYangle += kalmanY.getRate() * dt;

  compAngleX = 0.93 * (compAngleX + gyroXrate * dt) + 0.07 * roll; // Calculate the angle using a Complimentary filter
  compAngleY = 0.93 * (compAngleY + gyroYrate * dt) + 0.07 * pitch;

  // Reset the gyro angle when it has drifted too much
  if (gyroXangle < -180 || gyroXangle > 180)
    gyroXangle = kalAngleX;
  if (gyroYangle < -180 || gyroYangle > 180)
    gyroYangle = kalAngleY;

  /* Print Data */
#if 0 // Set to 1 to activate
  Serial.print(accX); Serial.print("\t");
  Serial.print(accY); Serial.print("\t");
  Serial.print(accZ); Serial.print("\t");

  Serial.print(gyroX); Serial.print("\t");
  Serial.print(gyroY); Serial.print("\t");
  Serial.print(gyroZ); Serial.print("\t");

  Serial.print("\t");
#endif
  delay(2);
}

double getRoll(){
  return kalAngleX;
}

double getPitch(){
  return kalAngleY;
}

/*
bool moveUltraMotorUpAndDown_1(int startAngle, int endAngle){
  int sum = 0;
  int result  = 0;
  if (startAngle <= maxAngle || startAngle >= 0)
  {
    for (int ang = startAngle; ang < endAngle; ang++) // for문을 돌며 모터 각도를 설정.
    {      
      servo[UB].write(ang);
      delay(5);
      result = isBlocked(UB); // 해당 거리에 물체가 있는가?
      if(ang % 3 == 0){//5번에 한번씩 업데이트
        updateGyroValue();
      }
      
       // 시작 각도에서 초음파 센서의 맨 처음 측정값이 0이 나올 경우를 스킵하기 위한 조건문
       // 시작 각도에서는 무조건 pass
       
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
*/
/**
 * 시스템 구동시, 모터 초기 각도 계산.
 * tilt : 자이로센서로 구한 지팡이 기울기(각도º)
 * HYPO_TOP : 위아래 장애물을 감지하는 센서의 지팡이 상의 위치
 * height : 위아래 장애물 감지 센서의 바닥과의 직각 거리 (높이),  l*sin(cane)
)
 */

/**
 * 초음파 센서의 측정값을 구함
 */
/*
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
  delayMicroseconds(3000);
  // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
  float distance = mDuration / 29.0 / 2.0;
  Serial.println(distance);
  if (distance > 2.0 && distance < limit[sensorType]) {
      //Serial.println(1);
      return 1;
    } else {
      //Serial.println(0);
      return 0;
    }
}

void printStr(String head, float value){
   Serial.print(head);
   Serial.print(" : ");
   Serial.println(value);
}
*/
