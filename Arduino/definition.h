
// ======================================================
// PROTOCOLE IOT - DEFINES OFFICIELS
// ======================================================

// =========================
// Taille de trame
// =========================
#define FRAME_SIZE 8

// =========================
// Index des bytes dans la trame
// [Byte7][Byte6][Byte5][Byte4][Byte3][Byte2][Byte1][Byte0]
// =========================
#define IDX_FRAME_TYPE      0
#define IDX_COMMAND_ID      1
#define IDX_DEVICE_ID       2
#define IDX_COMMAND_NAME    3
#define IDX_COMMAND_TYPE    4
#define IDX_COMMAND_VALUE   5
#define IDX_COMMAND_STATUS  6
#define IDX_TTL             7

// ======================================================
// FRAME TYPES
// ======================================================
#define FRAME_TYPE_GLOBAL_CONTROL   0x00
#define FRAME_TYPE_HARDWARE_CONTROL 0x01
#define FRAME_TYPE_ACK              0x02
#define FRAME_TYPE_SENSOR_DATA 0x03

// ======================================================
// GLOBAL CONTROL VALUES (quand FRAME_TYPE = 0x00)
// ======================================================
#define GLOBAL_POWER_ON   0x01
#define GLOBAL_POWER_OFF  0x02

// ======================================================
// DEVICE IDS
// ======================================================
#define DEVICE_SYSTEM       0x00
#define DEVICE_LIGHT        0x01
#define DEVICE_HEATING      0x02
#define DEVICE_VENTILATION  0x03
#define DEVICE_RFID         0x04
#define DEVICE_DHT11        0x05
#define DEVICE_BUZZER       0x06
#define DEVICE_LCD          0x07

// ======================================================
// COMMAND NAMES
// ======================================================
#define CMD_NO_OP       0x00
#define CMD_TURN_ON     0x01
#define CMD_TURN_OFF    0x02
#define CMD_TOGGLE      0x03
#define CMD_SET_VALUE   0x04
#define CMD_READ_VALUE  0x05
#define CMD_RESET       0x06
#define CMD_ENABLE      0x07
#define CMD_DISABLE     0x08
#define CMD_HEARTBEAT   0x09

// ======================================================
// COMMAND TYPES
// ======================================================
#define CMD_TYPE_ACTION  0x01
#define CMD_TYPE_CONFIG  0x02
#define CMD_TYPE_QUERY   0x03
#define CMD_TYPE_SYSTEM  0x04

// ======================================================
// COMMAND VALUES
// =========================
#define CMD_VALUE_NONE     0x00
#define CMD_VALUE_ON       0x01
#define CMD_VALUE_OFF      0x02
#define CMD_VALUE_FALSE    0x02
#define CMD_VALUE_TRUE     0x01
#define CMD_VALUE_ENABLE   0x01
#define CMD_VALUE_DISABLE  0x02
#define CMD_VALUE_MAX      0xFF

// ======================================================
// COMMAND STATUS
// Backend -> Arduino
// ======================================================
#define STATUS_PENDING   0x00

// Arduino -> Backend (ACK)
// ======================================================
#define STATUS_EXECUTED  0x01
#define STATUS_FAILED    0x02
#define STATUS_EXPIRED   0x03
#define STATUS_REJECTED  0x04

// ======================================================
// ERROR CODES (ACK)
// ======================================================
#define ERROR_NONE                0x00
#define ERROR_COMMAND_UNKNOWN     0x01
#define ERROR_DEVICE_UNREACHABLE  0x02
#define ERROR_TIMEOUT             0x03
#define ERROR_INVALID_VALUE       0x04
#define ERROR_INVALID_DEVICE      0x05
#define ERROR_INVALID_FRAME       0x06
#define ERROR_HARDWARE_FAILURE    0x07
#define ERROR_BUSY                0x08

// ======================================================
// TTL
// ======================================================
#define TTL_NO_EXPIRATION  0x00

// ======================================================
// VALEURS UTILES / RESERVEES
// ======================================================
#define RESERVED_BYTE      0x00
#define DEFAULT_ACK_BYTE0  0x00