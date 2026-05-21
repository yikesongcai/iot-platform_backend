import paho.mqtt.client as mqtt
import json
import time
import sys

def on_connect(client, userdata, flags, rc):
    print(f"Python: Connected with code {rc}")

def send_command(device_id, action):
    client = mqtt.Client()
    client.on_connect = on_connect
    client.connect("47.92.210.172", 1883, 60)

    payload = {
        "deviceId": device_id,
        "action": action,
        "timestamp": int(time.time())
    }
    client.publish("device/control", json.dumps(payload))
    client.disconnect()

if __name__ == "__main__":
    if len(sys.argv) == 3:
        send_command(sys.argv[1], sys.argv[2])
    else:
        print("Usage: python mqtt_client.py <deviceId> <action>")
