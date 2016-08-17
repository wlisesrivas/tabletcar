
#include <SoftwareSerial.h>

// creates a "virtual" serial port/UART
// connect BT module TX to D10
// connect BT module RX to D11
// connect BT Vcc to 5V, GND to GND

SoftwareSerial BT(10, 11); 

// ---------------------------------------------------------
// Salidas
// ---------------------------------------------------------
// Power button pin
int BTN_POWER    = 1;

// Vol Up button pin
int BTN_VOL_UP   = 2;

// Vol Up button pin
int BTN_VOL_DOWN = 3;

// SWICHT
int ON  = 0;
int OFF = 1;

// Read ohms, @Link: http://www.circuitbasics.com/arduino-ohm-meter/
// The program sets up analog pin A0 to read the voltage between the known resistor and the unknown resistor
int analogPin = 0;
int raw = 0;
int Vin = 5; // 5v
// The voltage drop across your unknown resistor:
float Vout = 0;
// The known resistor (1 kOhm = 1000 Ohms in this example).
float R1 = 985;
// The resistance value your unknown resistor value in Ohms.
float R2 = 0;
float buffer = 0;
int tol = 100; // 100 ohms tolerance.

void setup()
{
  pinMode(BTN_POWER, OUTPUT);
  pinMode(BTN_VOL_UP, OUTPUT);
  pinMode(BTN_VOL_DOWN, OUTPUT);
  
  // Turn off at start
  digitalWrite(BTN_POWER, OFF);
  digitalWrite(BTN_VOL_UP, OFF);
  digitalWrite(BTN_VOL_DOWN, OFF);
  
  // Set the data rate for the SoftwareSerial port
  BT.begin(9600);
  delay(50);
  BT.println("Codeasy SRL. 2016, Welcome to wRadio - Bluetooth Interface");
  
  Serial.begin(9600);
}

void loop()
{
  // Read ohms
  raw = analogRead(analogPin);
  if (raw)
  {
    buffer = raw * Vin;
    Vout = (buffer) / 1024.0;
    buffer = (Vin / Vout) - 1;
    R2 = R1 * buffer;
    if(R2 < 10000) 
    {
      Serial.print("R2: ");
      Serial.println(R2);
      handleInput(R2);
      delay(10);
    }
    
  }
}

void handleInput(float ohm) {
   // Volumen Up
   if(ohm
}

void send() {
  // Protocol WBS (Wlises Button Status)
  // WPP1 => Power on
  // WPP0 => Power off
  // WVU1 => Volumen Up on
  // WVU0 => Volumen Up off
  // WVD1 => Volumen Down on
  // WVD0 => Volumen Down off
  // WNT1 => Next Track on
  // WNT0 => Next Track off
  // WPT1 => Prev Track on
  // WPT0 => Prev Track off

  String cmd = "W";
  
  
}





