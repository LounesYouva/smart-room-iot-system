import time
import serial
import requests
from datetime import datetime, timezone
from helper import *



# =========================
# SERIAL
# =========================
def open_serial():
    try:
        ser = serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=SERIAL_TIMEOUT)
        time.sleep(2)
        ser.reset_input_buffer()
        ser.reset_output_buffer()
        print(f"[SERIAL] Connecté sur {SERIAL_PORT} à {BAUD_RATE} bauds")
        return ser
    except Exception as e:
        print(f"[SERIAL] Erreur ouverture port série : {e}")
        raise SystemExit(1)


# =========================
# HTTP / BACKEND
# =========================
def get_pending_commands_by_type(command_type: CommandType):
    url = f"{BASE_URL}/status/PENDING/type/{command_type.value}"

    try:
        response = requests.get(url, timeout=5)
        response.raise_for_status()
        data = response.json()

        if not isinstance(data, list):
            print(f"[HTTP] Réponse inattendue pour {command_type.value}: {data}")
            return []

        return data

    except requests.RequestException as e:
        print(f"[HTTP] Erreur récupération commandes {command_type.value} : {e}")
        return []
    except Exception as e:
        print(f"[HTTP] Erreur inattendue récupération {command_type.value} : {e}")
        return []


def update_command_status(command_id: int, status: CommandStatus):
    url = f"{BASE_URL}/{command_id}/status"

    try:
        response = requests.put(url, params={"value": status.value}, timeout=5)
        response.raise_for_status()
        print(f"[HTTP] Commande {command_id} -> {status.value}")
        return True
    except requests.RequestException as e:
        print(f"[HTTP] Erreur update statut commande {command_id} : {e}")
        return False


def send_sensor_data_to_backend(sensor_payload: dict):
    """
    Hypothèse d'API backend :
    POST /api/sensors/dht11
    Body:
    {
      "commandId": 105,
      "deviceId": "DHT11",
      "temperature": 24,
      "humidity": 61,
      "status": "EXECUTED"
    }
    """
    url = f"{SENSOR_BASE_URL}readings"

    try:
        response = requests.post(url, json=sensor_payload, timeout=5)
        response.raise_for_status()
        print(f"[HTTP] Données capteur envoyées : {sensor_payload}")
        return True
    except requests.RequestException as e:
        print(f"[HTTP] Erreur envoi données capteur : {e}")
        return False


# =========================
# TEMPS / EXPIRATION
# =========================
def parse_iso_datetime(value: str):
    if not value:
        return None

    try:
        if value.endswith("Z"):
            value = value.replace("Z", "+00:00")

        dt = datetime.fromisoformat(value)

        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)

        return dt
    except Exception:
        return None


def is_expired(command: dict):
    expires_at = command.get("expiresAt")
    if not expires_at:
        return False

    dt = parse_iso_datetime(expires_at)
    if dt is None:
        return False

    return datetime.now(timezone.utc) > dt


def compute_ttl_seconds(command: dict) -> int:
    expires_at = command.get("expiresAt")
    if not expires_at:
        return TTL_NO_EXPIRATION

    dt = parse_iso_datetime(expires_at)
    if dt is None:
        return TTL_NO_EXPIRATION

    now = datetime.now(timezone.utc)
    delta = int((dt - now).total_seconds())

    if delta <= 0:
        return 0

    return min(delta, 255)


# =========================
# HELPERS
# =========================
def normalize_str(value):
    if value is None:
        return ""
    return str(value).strip().upper()


def format_frame_hex(frame: bytes) -> str:
    return " ".join(f"{b:02X}" for b in frame)


def map_device_id(command: dict) -> int:
    raw = command.get("deviceId")

    if isinstance(raw, int):
        return raw & 0xFF

    key = normalize_str(raw)
    return DEVICE_MAP.get(key, 0xFF)


def map_command_type(command: dict) -> int:
    raw = normalize_str(command.get("type"))
    return COMMAND_TYPE_MAP.get(raw, CMD_TYPE_ACTION)


def map_command_name(command: dict) -> int:
    raw = normalize_str(command.get("command"))
    return COMMAND_NAME_MAP.get(raw, CMD_NO_OP)


def map_command_value(command: dict, command_name: int) -> int:
    raw_value = command.get("value")

    if raw_value is not None:
        try:
            return int(raw_value) & 0xFF
        except Exception:
            return CMD_VALUE_NONE

    if command_name == CMD_TURN_ON:
        return CMD_VALUE_ON
    if command_name == CMD_TURN_OFF:
        return CMD_VALUE_OFF
    if command_name == CMD_ENABLE:
        return CMD_VALUE_ENABLE
    if command_name == CMD_DISABLE:
        return CMD_VALUE_DISABLE

    return CMD_VALUE_NONE


def is_global_command(command: dict) -> bool:
    device = normalize_str(command.get("deviceId"))
    cmd = normalize_str(command.get("command"))

    return device == "SYSTEM" or cmd in ("START", "STOP", "GLOBAL_ON", "GLOBAL_OFF")


# =========================
# CONSTRUCTION TRAMES
# =========================
def build_protocol_frame(command: dict) -> bytes:
    cmd_id = int(command.get("id", 0)) & 0xFF
    ttl = compute_ttl_seconds(command)

    if is_global_command(command):
        frame = bytearray(FRAME_SIZE)
        frame[IDX_FRAME_TYPE] = FRAME_TYPE_GLOBAL_CONTROL
        frame[IDX_COMMAND_ID] = cmd_id
        frame[IDX_DEVICE_ID] = 0x00
        frame[IDX_COMMAND_NAME] = 0x00
        frame[IDX_COMMAND_TYPE] = 0x00
        frame[IDX_COMMAND_VALUE] = 0x00
        frame[IDX_COMMAND_STATUS] = 0x00

        cmd = normalize_str(command.get("command"))
        if cmd in ("START", "GLOBAL_ON"):
            frame[IDX_TTL] = GLOBAL_POWER_ON
        elif cmd in ("STOP", "GLOBAL_OFF"):
            frame[IDX_TTL] = GLOBAL_POWER_OFF
        else:
            frame[IDX_TTL] = ttl

        return bytes(frame)

    device_id = map_device_id(command)
    command_name = map_command_name(command)
    command_type = map_command_type(command)
    command_value = map_command_value(command, command_name)

    frame = bytearray(FRAME_SIZE)
    frame[IDX_FRAME_TYPE] = FRAME_TYPE_HARDWARE_CONTROL
    frame[IDX_COMMAND_ID] = cmd_id
    frame[IDX_DEVICE_ID] = device_id
    frame[IDX_COMMAND_NAME] = command_name
    frame[IDX_COMMAND_TYPE] = command_type
    frame[IDX_COMMAND_VALUE] = command_value
    frame[IDX_COMMAND_STATUS] = STATUS_PENDING
    frame[IDX_TTL] = ttl

    return bytes(frame)


# =========================
# SERIAL I/O
# =========================
def send_frame_to_arduino(ser, frame: bytes) -> bool:
    try:
        print(f"[TX] {format_frame_hex(frame)}")
        ser.write(frame)
        ser.flush()
        return True
    except Exception as e:
        print(f"[SERIAL] Erreur envoi Arduino : {e}")
        return False


def read_exactly(ser, size: int, timeout: float) -> bytes:
    data = b""
    start = time.time()

    while len(data) < size:
        if time.time() - start > timeout:
            break

        chunk = ser.read(size - len(data))
        if chunk:
            data += chunk

    return data


# =========================
# PARSING FRAMES ENTRANTES
# =========================
def parse_ack_frame(frame: bytes):
    return {
        "kind": "ack",
        "frameType": frame[IDX_FRAME_TYPE],
        "commandId": frame[IDX_COMMAND_ID],
        "deviceId": frame[IDX_DEVICE_ID],
        "commandName": frame[IDX_COMMAND_NAME],
        "commandType": frame[IDX_COMMAND_TYPE],
        "errorCode": frame[IDX_COMMAND_VALUE],
        "status": frame[IDX_COMMAND_STATUS],
        "reserved": frame[IDX_TTL],
    }


def parse_sensor_data_frame(frame: bytes):
    """
    Format proposé côté Arduino :
    [FrameType][DeviceID][Temp][Humidity][CommandID][Status][Reserved][Reserved]
    """
    return {
        "kind": "sensor_data",
        "frameType": frame[0],
        "deviceId": frame[1],
        "temperature": frame[2],
        "humidity": frame[3],
        "commandId": frame[4],
        "status": frame[5],
        "reserved1": frame[6],
        "reserved2": frame[7],
    }


def parse_incoming_frame(frame: bytes):
    if frame is None or len(frame) != FRAME_SIZE:
        return None

    frame_type = frame[0]

    if frame_type == FRAME_TYPE_ACK:
        return parse_ack_frame(frame)

    if frame_type == FRAME_TYPE_SENSOR_DATA:
        return parse_sensor_data_frame(frame)

    return {
        "kind": "unknown",
        "raw": frame,
    }


def ack_status_to_backend_status(ack: dict) -> CommandStatus:
    status = ack["status"]

    if status == STATUS_EXECUTED:
        return CommandStatus.EXECUTED

    if status == STATUS_EXPIRED:
        return CommandStatus.EXPIRED

    return CommandStatus.FAILED


# =========================
# GESTION SENSOR DATA
# =========================
def handle_sensor_data_frame(frame_info: dict):
    if frame_info["deviceId"] != DEVICE_DHT11:
        print(f"[SENSOR] Device capteur non géré : {frame_info['deviceId']}")
        return

    payload = {
        "commandId": frame_info["commandId"],
        "deviceId": "DHT11",
        "temperature": int(frame_info["temperature"]),
        "humidity": int(frame_info["humidity"]),
        "status": "EXECUTED" if frame_info["status"] == STATUS_EXECUTED else "FAILED"
    }

    print(f"[SENSOR] DHT11 -> T={payload['temperature']}°C H={payload['humidity']}% CMD={payload['commandId']}")
    send_sensor_data_to_backend(payload)


# =========================
# ATTENTE REPONSE COMMANDE
# =========================
def wait_for_ack_and_optional_data(ser, expected_command_id: int, expect_sensor_data: bool = False):
    """
    Attend d'abord un ACK correspondant à la commande.
    Si expect_sensor_data=True, on accepte ensuite une trame SENSOR_DATA associée.
    """
    ack = None
    sensor_data = None
    deadline = time.time() + SERIAL_TIMEOUT + 2.0

    while time.time() < deadline:
        raw = read_exactly(ser, FRAME_SIZE, timeout=1.0)
        if not raw:
            continue

        print(f"[RX] {format_frame_hex(raw)}")
        info = parse_incoming_frame(raw)

        if info is None:
            print("[RX] Trame invalide")
            continue

        if info["kind"] == "ack":
            if info["commandId"] != expected_command_id:
                print(f"[RX] ACK ignoré, ID inattendu : {info['commandId']}")
                continue

            ack = info

            if not expect_sensor_data:
                return ack, sensor_data

            # Pour DHT11 on continue un peu pour récupérer la data
            continue

        if info["kind"] == "sensor_data":
            if info["commandId"] != expected_command_id:
                print(f"[RX] SENSOR_DATA ignorée, CMD inattendue : {info['commandId']}")
                continue

            sensor_data = info

            if ack is not None:
                return ack, sensor_data

        else:
            print(f"[RX] Type inconnu : {info}")

    return ack, sensor_data


# =========================
# TRAITEMENT COMMANDE
# =========================
def process_command(ser, command: dict):
    cmd_id = int(command.get("id"))
    expected_id = cmd_id & 0xFF
    device_id = command.get("deviceId")
    cmd_type = command.get("type")
    cmd_text = command.get("command")

    print(f"[PROCESS] ID={cmd_id} DEVICE={device_id} TYPE={cmd_type} CMD={cmd_text}")

    if is_expired(command):
        print(f"[PROCESS] Commande {cmd_id} expirée, non envoyée")
        update_command_status(cmd_id, CommandStatus.EXPIRED)
        return

    if not update_command_status(cmd_id, CommandStatus.SENT):
        print(f"[PROCESS] Impossible de passer la commande {cmd_id} en SENT")
        return

    frame = build_protocol_frame(command)

    if not send_frame_to_arduino(ser, frame):
        update_command_status(cmd_id, CommandStatus.FAILED)
        return

    normalized_device = normalize_str(device_id)
    normalized_cmd = normalize_str(cmd_text)
    expect_sensor_data = (normalized_device == "DHT11" and normalized_cmd == "READ_VALUE")

    ack, sensor_data = wait_for_ack_and_optional_data(
        ser,
        expected_command_id=expected_id,
        expect_sensor_data=expect_sensor_data
    )

    if ack is None:
        print(f"[PROCESS] Pas d'ACK valide pour commande {cmd_id}")
        update_command_status(cmd_id, CommandStatus.FAILED)
        return

    backend_status = ack_status_to_backend_status(ack)

    if backend_status == CommandStatus.EXECUTED:
        update_command_status(cmd_id, CommandStatus.EXECUTED)
    elif backend_status == CommandStatus.EXPIRED:
        print(f"[PROCESS] Commande {cmd_id} expirée côté Arduino")
        update_command_status(cmd_id, CommandStatus.EXPIRED)
    else:
        error_label = ERROR_LABELS.get(ack["errorCode"], f"UNKNOWN_ERROR_{ack['errorCode']}")
        print(f"[PROCESS] Arduino a renvoyé une erreur : {error_label}")
        update_command_status(cmd_id, CommandStatus.FAILED)

    if sensor_data is not None:
        handle_sensor_data_frame(sensor_data)
    elif expect_sensor_data and backend_status == CommandStatus.EXECUTED:
        print(f"[PROCESS] ACK reçu pour DHT11 mais aucune SENSOR_DATA reçue pour commande {cmd_id}")


# =========================
# TRAITEMENT PAR TYPE
# =========================
def process_states(ser):
    states = get_pending_commands_by_type(CommandType.STATE)
    if states:
        print(f"[STATE] {len(states)} commande(s) STATE à traiter")

    for cmd in states:
        process_command(ser, cmd)


def process_actions(ser):
    actions = get_pending_commands_by_type(CommandType.ACTION)
    if actions:
        print(f"[ACTION] {len(actions)} commande(s) ACTION à traiter")

    for cmd in actions:
        process_command(ser, cmd)


# =========================
# MAIN
# =========================
def main():
    ser = open_serial()

    while True:
        try:
            process_states(ser)
            process_actions(ser)

        except KeyboardInterrupt:
            print("\n[MAIN] Arrêt manuel")
            break

        except Exception as e:
            print(f"[MAIN] Erreur boucle principale : {e}")

        time.sleep(POLL_INTERVAL)

    try:
        ser.close()
        print("[SERIAL] Port fermé")
    except Exception:
        pass


if __name__ == "__main__":
    main()