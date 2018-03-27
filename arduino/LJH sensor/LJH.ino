#include <Servo.h>

 #define UP 50
 #define LEFT 130
 
Servo myservo;
int increase = 1;
int min_angle=100;
int max_angle = 180;
int center=90;

float pos=min_angle;
 
int TrigPin = 12;
int EchoPin = 13;
 
void setup(){
  Serial.begin(9600);
  myservo.attach(6);
  pinMode(TrigPin, OUTPUT);
  pinMode(EchoPin, INPUT);
}

 /*
  * checkAng()은 시작를 구한다음 끝나는 각도까지 회전하게 하는 것
  */
void loop(){
  checkAng();
  

  
}
 
unsigned long distance(){
  unsigned long d;
   
  digitalWrite(TrigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(TrigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(TrigPin, LOW);
   
  d = pulseIn(EchoPin, HIGH)*0.034/2;
   
  return d;
}

/*
 * r : 측정 거리 ( 180cm로 가정함)
 * l : 초음파센서를 통해 얻은 바닥까지와의 거리
 * a : 지팡이 끝에서 초음파 센서 단 거리까지의 거리
 * arcsin을 이용하여 초음파센서를 통해 얻은 바닥까지의 거리에 따른 시작각도를 구함.
 */
float startAng(float r, float l, float a)
{
  float x=a/l;
  float rad_x=asin(x);
  float ang_x=rad_x/3.141592654*180;

  
  float y=a/r;
  float rad_y=asin(y);
  float ang_y=rad_y/3.141592654*180;


  return ang_x-ang_y;
}

/*
 * r : 측정 거리 ( 180cm로 가정함)
 * l : 초음파센서를 통해 얻은 바닥까지와의 거리
 * a : 지팡이 끝에서 초음파 센서 단 거리까지의 거리
 * 사람의 평균키가 180도로 가정하고 계산함
 * 끝나는 각도를 구함
 */
float endAng(float r, float l, float a)
{
  float z=(180-a)/r;
  float rad_z=asin(z);
  float ang_z=rad_z/3.141592654*180;

  return ang_z+100;
}

/*
 * 위에서 얻은 시작각도와 끝나는 각도로 회전하는 함수
 */
void checkAng()
{
  int i;
  pos=startAng(180,52,50);
 float pe=endAng(180,52,50);

 if(pos <= max_angle || pos>=0)
 {
  for(i=pos; i<endAng(180,52,50); i++)
  {
      myservo.write(i);
      //Serial.print(pos);
      // Serial.print(",");
       Serial.print(distance());
       Serial.print(".");
       delay(10);
  }
  for(i=endAng(180,52,50); i>=pos; i--)
    {
      myservo.write(i);
     // Serial.print(pos);
     // Serial.print(",");
       Serial.print(distance());
       Serial.print(".");
        delay(10);
    }
 }


  
}

