#include <iostream>
#include <wiringPi.h>
#include <fstream>
#include <cmath>
#include <string>

#define TRIG 0	//Wiring Pi
#define ECHO 2	//Wiring Pi
using namespace std;
/* In the directory: of SonarJson.c:
1)  [ sudo ../../pi-blaster/pi-blaster ] if servo
2)  g++  SonarJson.c -l wiringPi -o  SonarJson
*/
void setup() {
	wiringPiSetup();
	pinMode(TRIG, OUTPUT);
	pinMode(ECHO, INPUT);
	//TRIG pin must start LOW
	digitalWrite(TRIG, LOW);
	delay(30);
}
int getCM() {
	//Send trig pulse
	digitalWrite(TRIG, HIGH);
	delayMicroseconds(20);
	digitalWrite(TRIG, LOW);

	//Wait for echo start
	while(digitalRead(ECHO) == LOW);

	//Wait for echo end
	long startTime = micros();
	while(digitalRead(ECHO) == HIGH);
	long travelTime = micros() - startTime;

	//Get distance in cm
	int distance = travelTime / 58;
    return distance;
}

/*
SAM generates : {"p":"f_t","t":"d","d":{"cm":153,"dir":"forward"}}
OUR output is:  {"p":"f_t","t":"d","d":{"cm":153,"dir":"forward"}}

*/
int main(void) {
	int cm ;
 	setup();
	while(1) {
		cm = getCM();
		cout <<  "{\"p\":\"f_t\",\"t\":\"d\",\"d\":{\"cm\":" << cm << ",\"dir\":\"forward\"}}" <<  endl;
		delay(300);
	}
	return 0;
}