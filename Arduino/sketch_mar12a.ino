#include <SPI.h>
#include <MFRC522.h>
#include <DHT.h>
#include "C:\Users\etulyon1\OneDrive - ASCOTRONICS\Documents\Arduino\sketch_mar12a\helper.h"
#include "C:\Users\etulyon1\OneDrive - ASCOTRONICS\Documents\Arduino\sketch_mar12a\definition.h"


volatile bool user_start = false;

// Buffer commande reçue
byte rxFrame[FRAME_SIZE];

// Buffer ACK à envoyer
byte txFrame[FRAME_SIZE];

byte buffer[8];

// =========================
// Helpers
// =========================
bool isAuthorizedCard(MFRC522::Uid *uid) {
  if (uid->size != 4) return false;

  for (byte i = 0; i < 4; i++) {
    if (uid->uidByte[i] != authorizedUID[i]) {
      return false;
    }
  }
  return true;
}

void copyUID(MFRC522::Uid *uid, byte *dest) {
  for (byte i = 0; i < 4; i++) {
    dest[i] = uid->uidByte[i];
  }
}







// =========================
// Lecture DHT
// =========================
void handleDHT() {
  unsigned long currentMillis = millis();

  if (currentMillis - previousDHTMillis >= dhtInterval) {
    previousDHTMillis = currentMillis;

    float h = dht.readHumidity();
    float t = dht.readTemperature();

    if (!isnan(h) && !isnan(t)) {
      // ici tu peux choisir :
      currentTemperature = t;
      currentHumidity = h;


      currentTemperature = t;
      currentHumidity = h;

    } else {;
    }
  }

}

// =========================
// Lecture RFID
// =========================
void handleRFID() {
  unsigned long currentMillis = millis();

  if (currentMillis - lastRFIDReadMillis < rfidCooldown) {
    return;
  }

  if (rfid.PICC_IsNewCardPresent() && rfid.PICC_ReadCardSerial()) {
    bool authorized = isAuthorizedCard(&rfid.uid);
    cardDetected = true;
    cardAuthorized = authorized;
    if (rfid.uid.size >= 4) {
      copyUID(&rfid.uid, lastUID);
    }

    if (authorized) {
      occupancyCount++;
      digitalWrite(RED_LED_PIN, LOW);
      digitalWrite(GREEN_LED_PIN, HIGH);
      greenLedActive = true;
      greenLedStart = currentMillis;
    } else {
      digitalWrite(GREEN_LED_PIN, LOW);
      digitalWrite(RED_LED_PIN, HIGH);
      redLedActive = true;
      redLedStart = currentMillis;
    }

    rfid.PICC_HaltA();
    rfid.PCD_StopCrypto1();

    lastRFIDReadMillis = currentMillis;
  }
}

// =========================
// Gestion extinction LED RFID
// =========================
void handleRFIDLeds() {
  unsigned long currentMillis = millis();

  if (greenLedActive && (currentMillis - greenLedStart >= greenLedDuration)) {
    digitalWrite(GREEN_LED_PIN, LOW);
    greenLedActive = false;
  }

  if (redLedActive && (currentMillis - redLedStart >= redLedDuration)) {
    digitalWrite(RED_LED_PIN, LOW);
    redLedActive = false;
  }
}

void updateOutputs(bool heat, bool vent, bool light) { 
    digitalWrite(HEATING_LED_PIN, heat ? HIGH : LOW); 
   digitalWrite(VENTILATION_PIN, vent ? HIGH : LOW);
   digitalWrite(LIGHT_LED_PIN, light ? HIGH : LOW); 
   }
void handleControl() {
  unsigned long currentMillis = millis();

  if (currentMillis - previousControlMillis >= controlInterval) {
    previousControlMillis = currentMillis;
    updateOutputs(heatingOn, ventilationOn, lightOn);
  }
}

void processCommand() {
  byte frameType = rxFrame[IDX_FRAME_TYPE];
  byte commandId = rxFrame[IDX_COMMAND_ID];
  byte deviceId = rxFrame[IDX_DEVICE_ID];
  byte commandName = rxFrame[IDX_COMMAND_NAME];
  byte commandType = rxFrame[IDX_COMMAND_TYPE];
  byte commandValue = rxFrame[IDX_COMMAND_VALUE];
  byte commandStatus = rxFrame[IDX_COMMAND_STATUS];
  byte ttl = rxFrame[IDX_TTL];

  byte errorCode = ERROR_NONE;
  byte finalStatus = STATUS_EXECUTED;

  bool sendDhtFrame = false;

  if (frameType == FRAME_TYPE_GLOBAL_CONTROL) {
    if (ttl == GLOBAL_POWER_ON) {
      start();
    } else if (ttl == GLOBAL_POWER_OFF) {
      stop();
    } else {
      errorCode = ERROR_INVALID_VALUE;
      finalStatus = STATUS_FAILED;
    }
  }
  else if (frameType == FRAME_TYPE_HARDWARE_CONTROL) {
    if (!user_start) {
      errorCode = ERROR_DEVICE_UNREACHABLE;
      finalStatus = STATUS_FAILED;
    } else {
      switch (deviceId) {
        case DEVICE_LIGHT:
          if (commandName == CMD_TURN_ON) lightOn = true;
          else if (commandName == CMD_TURN_OFF) lightOn = false;
          else if (commandName == CMD_TOGGLE) lightOn = !lightOn;
          else {
            errorCode = ERROR_COMMAND_UNKNOWN;
            finalStatus = STATUS_FAILED;
          }
          break;

        case DEVICE_HEATING:
          if (commandName == CMD_TURN_ON) heatingOn = true;
          else if (commandName == CMD_TURN_OFF) heatingOn = false;
          else if (commandName == CMD_TOGGLE) heatingOn = !heatingOn;
          else {
            errorCode = ERROR_COMMAND_UNKNOWN;
            finalStatus = STATUS_FAILED;
          }
          break;

        case DEVICE_VENTILATION:
          if (commandName == CMD_TURN_ON) ventilationOn = true;
          else if (commandName == CMD_TURN_OFF) ventilationOn = false;
          else if (commandName == CMD_TOGGLE) ventilationOn = !ventilationOn;
          else {
            errorCode = ERROR_COMMAND_UNKNOWN;
            finalStatus = STATUS_FAILED;
          }
          break;

        case DEVICE_DHT11:
          if (commandName == CMD_READ_VALUE) {
            float h = dht.readHumidity();
            float t = dht.readTemperature();

            if (isnan(h) || isnan(t)) {
              errorCode = ERROR_HARDWARE_FAILURE;
              finalStatus = STATUS_FAILED;
            } else {
              currentTemperature = t;
              currentHumidity = h;
              sendDhtFrame = true;
            }
          } else {
            errorCode = ERROR_COMMAND_UNKNOWN;
            finalStatus = STATUS_FAILED;
          }
          break;

        default:
          errorCode = ERROR_INVALID_DEVICE;
          finalStatus = STATUS_FAILED;
          break;
      }
    }
  }
  else {
    errorCode = ERROR_INVALID_FRAME;
    finalStatus = STATUS_FAILED;
  }

  txFrame[IDX_FRAME_TYPE]     = FRAME_TYPE_ACK;
  txFrame[IDX_COMMAND_ID]     = commandId;
  txFrame[IDX_DEVICE_ID]      = deviceId;
  txFrame[IDX_COMMAND_NAME]   = commandName;
  txFrame[IDX_COMMAND_TYPE]   = commandType;
  txFrame[IDX_COMMAND_VALUE]  = errorCode;
  txFrame[IDX_COMMAND_STATUS] = finalStatus;
  txFrame[IDX_TTL]            = RESERVED_BYTE;

  Serial.write(txFrame, FRAME_SIZE);

  if (sendDhtFrame) {
    sendDHT11Data(commandId);
  }
}
bool readBytesFromBackend() {
  if (Serial.available() >= FRAME_SIZE) {
    size_t n = Serial.readBytes(rxFrame, FRAME_SIZE);
    if (n == FRAME_SIZE) {
      return true;
    }
  }
  return false;
}
void sendDHT11Data(byte commandId) {
  byte sensorFrame[FRAME_SIZE];

  float h = dht.readHumidity();
  float t = dht.readTemperature();

  sensorFrame[0] = FRAME_TYPE_SENSOR_DATA;
  sensorFrame[1] = DEVICE_DHT11;

  if (isnan(h) || isnan(t)) {
    sensorFrame[2] = 0x00;
    sensorFrame[3] = 0x00;
    sensorFrame[4] = commandId;
    sensorFrame[5] = STATUS_FAILED;
    sensorFrame[6] = RESERVED_BYTE;
    sensorFrame[7] = RESERVED_BYTE;
  } else {
    currentTemperature = t;
    currentHumidity = h;

    sensorFrame[2] = (byte)t;
    sensorFrame[3] = (byte)h;
    sensorFrame[4] = commandId;
    sensorFrame[5] = STATUS_EXECUTED;
    sensorFrame[6] = RESERVED_BYTE;
    sensorFrame[7] = RESERVED_BYTE;
  }

  Serial.write(sensorFrame, FRAME_SIZE);
}
void start(){
    digitalWrite(STANDBY,LOW);
    digitalWrite(POWER_ON, HIGH);
    user_start = true;
}

void stop(){
    digitalWrite(STANDBY,HIGH);
    digitalWrite(POWER_ON, LOW);
    user_start = false;
}
// =========================
// Setup
// =========================
void setup() {
  Serial.begin(115200);

  pinMode(RED_LED_PIN, OUTPUT);
  pinMode(STANDBY, OUTPUT);
  pinMode(POWER_ON, OUTPUT);
  pinMode(GREEN_LED_PIN, OUTPUT);
  pinMode(VENTILATION_PIN, OUTPUT);
  pinMode(HEATING_LED_PIN, OUTPUT);
  pinMode(LIGHT_LED_PIN, OUTPUT);

  digitalWrite(RED_LED_PIN, LOW);
  digitalWrite(GREEN_LED_PIN, LOW);
  digitalWrite(VENTILATION_PIN, LOW);
  digitalWrite(HEATING_LED_PIN, LOW);
  digitalWrite(LIGHT_LED_PIN, LOW);

  SPI.begin();
  rfid.PCD_Init();
  dht.begin();

  Serial.println("====================================");
  Serial.println("Salle intelligente prete");
  Serial.println("Version SANS FreeRTOS");
  Serial.println("Commandes serie locales:");
  Serial.println("  r = reset occupation");
  Serial.println("  - = decrement occupation");
  Serial.println("Commande distante Python:");
  Serial.println("  CMD:HEATING=1;VENTILATION=0;LIGHT=1");
  Serial.println("====================================");
}

// =========================
// Loop
// =========================
void loop() {
  if (readBytesFromBackend()) {
    processCommand();
  }

  if (user_start) {
    start();
    handleRFID();
    handleRFIDLeds();
    handleDHT();
    handleControl();
 
    
  
  }
}


