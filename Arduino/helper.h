// =========================
// RFID
// =========================
#define SS_PIN 53
#define RST_PIN 6
#define MOTOR 32 
#define LED  33
#define STANDBY 42
#define POWER_ON  44
MFRC522 rfid(SS_PIN, RST_PIN);

// UID autorisé
byte authorizedUID[4] = { 0xA3, 0x13, 0x5D, 0x14 };

// =========================
// DHT11
// =========================
#define DHTPIN 7
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

// =========================
// LEDs / Actionneurs
// =========================
#define RED_LED_PIN 4      // accès refusé
#define GREEN_LED_PIN 3    // accès autorisé
#define VENTILATION_PIN 2   // ventilation
#define HEATING_LED_PIN 5  // chauffage
#define LIGHT_LED_PIN 8    // lumière

// =========================
// Variables globales
// =========================
float currentTemperature = 0.0;
float currentHumidity = 0.0;

bool cardDetected = false;
bool cardAuthorized = false;
byte lastUID[4] = { 0, 0, 0, 0 };

int occupancyCount = 0;

bool heatingOn = false;
bool ventilationOn = false;
bool lightOn = false;

// =========================
// Gestion timing avec millis()
// =========================
unsigned long previousDHTMillis = 0;
unsigned long previousMonitorMillis = 0;
unsigned long previousControlMillis = 0;

const unsigned long dhtInterval = 15000;
const unsigned long monitorInterval = 2000;
const unsigned long controlInterval = 200;

// gestion clignotement LED RFID
bool greenLedActive = false;
bool redLedActive = false;
unsigned long greenLedStart = 0;
unsigned long redLedStart = 0;
const unsigned long greenLedDuration = 800;
const unsigned long redLedDuration = 1000;

// anti-relecture carte trop rapide
unsigned long lastRFIDReadMillis = 0;
const unsigned long rfidCooldown = 800;

// buffer série
String inputLine = "";

// =========================
// Prototypes
// =========================
bool isAuthorizedCard(MFRC522::Uid *uid);
void copyUID(MFRC522::Uid *uid, byte *dest);
void printUID(byte *uid);
void updateOutputs(bool heat, bool vent, bool light);
int extractCommandValue(String cmd, const String &key);
void processCommandLine(String line);

void handleDHT();
void handleRFID();
void handleControl();
void handleMonitor();
void handleSerialCommand();
void handleRFIDLeds();
bool readBytesFromBackend();
void start();
void stop();
void processCommand();
void updateOutputs(bool heat, bool vent, bool light);