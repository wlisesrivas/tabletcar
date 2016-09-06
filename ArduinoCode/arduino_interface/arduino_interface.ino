// ---------------------------------------------------------
// Salidas
// ---------------------------------------------------------

// Vol Up button pin
#define BTN_VOL_UP 10

// Vol Up button pin
#define BTN_VOL_DOWN 11

// Power button pin
#define BTN_POWER 12

// SWICHT
#define ON  0
#define OFF 1

// Read ohms, @Link: http://www.circuitbasics.com/arduino-ohm-meter/
// The program sets up analog pin A0 to read the voltage between the known resistor and the unknown resistor
#define APIN 0

#define Vin 5 // 5v

int raw = 0;

// The voltage drop across your unknown resistor:
float Vout = 0;

// The known resistor (1 kOhm = 1000 Ohms in this example).
#define R1 2100.0

// The resistance value your unknown resistor value in Ohms.
float R2 = 0;

float buffer = 0;

// ------------------------------------------------------------------
// Ohms values.
// Round a number to the nearest hundred. e.g. 
// 990 = 1000, 940 = 900, 40 = 0, 55 = 100 etc ...
// ------------------------------------------------------------------

#define ROUND_OHM_NUM  100 
#define OHM_MODE       0 // is fixed

#define OHM_VOL_UP     900
#define OHM_VOL_DOWN   3000
#define OHM_NEXT_TRACK 0 // 1 ohm
#define OHM_PREV_TRACK 300

void setup()
{
  pinMode(BTN_POWER, OUTPUT);
  pinMode(BTN_VOL_UP, OUTPUT);
  pinMode(BTN_VOL_DOWN, OUTPUT);
  Serial.begin(9600);
  Serial.print("Ready!");
  // Turn off at start
  clearAll();
}

void clearAll() {
  digitalWrite(BTN_POWER, LOW);
  digitalWrite(BTN_VOL_UP, OFF);
  digitalWrite(BTN_VOL_DOWN, OFF);
}

int roundOhm(int val) {
  return round(val / ROUND_OHM_NUM) * ROUND_OHM_NUM;
}

void loop()
{
  // Read ohms
  raw = analogRead(APIN);
  if (raw)
  {
    buffer = raw * Vin;
    Vout = (buffer) / 1024.0;
    buffer = (Vin / Vout) - 1;
    R2 = R1 * buffer;
    if(R2 < 10000) 
    {
      int rohm = roundOhm(R2);
      Serial.print("Ohm: ");
      Serial.println(rohm);
      switch(rohm) {
        case OHM_VOL_UP:
            digitalWrite(BTN_VOL_UP, ON);
            Serial.println("Volume Up");
            break;
        case OHM_VOL_DOWN:
            digitalWrite(BTN_VOL_DOWN, ON);
            Serial.println("Volume Down");
            break;
        case OHM_NEXT_TRACK:
          Serial.println("Next Track");
          break;
        case OHM_PREV_TRACK:
          Serial.println("Prev Track");
            break; // TODO
      }
    } else clearAll();
    delay(125);
  }
  delay(10);
}

