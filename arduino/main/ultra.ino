
/**
   초음파 센서의 측정값을 구함
*/
float isBlocked(int sensorType) {
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
// Serial.println("dist "+ String(distance));
  if (distance > 2.0 && distance < limit[sensorType]) {
    //Serial.println(1);
    return 1;
  } else {
    //Serial.println(0);
    return 0;
  }
}
int testIsBlocked(int sensorType, float limit) {
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
// Serial.println("dist "+ String(distance));
  if (distance > 2.0 && distance < limit ) {
    //.println(String(sensorType)+"초음파 센서 거리 "+String(distance)+" < " +String(limit));
    return 1;
  } else {
    //Serial.println(0);
    return 0;
  }
}

/**
   초음파 센서 구동 후 측정 거리 반환
*/
float sensingUltra(int sensorType) {
  // 초음파를 보낸다. 다 보내면 echo가 HIGH 상태로 대기하게 된다.
  digitalWrite(trigPin[sensorType], LOW);
  digitalWrite(echoPin[sensorType], LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin[sensorType], HIGH);
  delayMicroseconds(2);
  digitalWrite(trigPin[sensorType], LOW);

  // echoPin 이 HIGH를 유지한 시간을 저장 한다.
  unsigned long  mDuration = pulseIn(echoPin[sensorType], HIGH);
  //Serial.println("Duration: "+String(mDuration));
  delayMicroseconds(100);
  // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
  float distance = mDuration / 29.0 / 2.0;
  Serial.println(String(sensorType)+" : "+ String(distance) +"...........");
  return distance;
}
