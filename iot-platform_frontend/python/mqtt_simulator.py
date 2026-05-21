"""
物联网设备MQTT模拟器 - 模拟温度传感器通过MQTT协议上报数据

连接到嵌入式MQTT Broker(Spring Boot应用内置的Vert.x MQTT Server)
协议说明:
  - 连接地址: localhost:1884
  - ClientId格式: {productKey}_{deviceName}_{model}
  - 认证: 用户名=deviceName, 密码=设备密码
  - Topic格式: /{scope}/{productKey}/{deviceName}/{action}
  - property上报: topic中包含"post"
  - warning上报: topic中包含"warning"
"""

import paho.mqtt.client as mqtt
import json
import time
import math
import random
import argparse

# ============ 配置参数 ============
MQTT_HOST = "localhost"
MQTT_PORT = 1884

# 设备信息（需与数据库devices表一致）
DEVICE_CONFIGS = [
    {"productKey": "pk-temp-001", "deviceName": "temp_sensor_01", "model": "m1", "password": "sensor123"},
    {"productKey": "pk-hum-001",  "deviceName": "hum_sensor_01",  "model": "m1", "password": "sensor123"},
    {"productKey": "pk-co2-001",  "deviceName": "co2_sensor_01",  "model": "m1", "password": "sensor123"},
    {"productKey": "pk-light-001","deviceName": "light_sensor_01","model": "m1", "password": "sensor123"},
]


def create_client(device, interval_s):
    """创建单个设备模拟器的MQTT客户端"""
    client_id = f"{device['productKey']}_{device['deviceName']}_{device['model']}"
    client = mqtt.Client(client_id=client_id, protocol=mqtt.MQTTv311)
    client.username_pw_set(device['deviceName'], device['password'])

    def on_connect(c, userdata, flags, rc):
        if rc == 0:
            print(f"[{device['deviceName']}] MQTT连接成功 (clientId={client_id})")
        else:
            print(f"[{device['deviceName']}] MQTT连接失败, rc={rc}")

    def on_disconnect(c, userdata, rc):
        print(f"[{device['deviceName']}] MQTT断开连接, rc={rc}")
        # 自动重连
        while True:
            try:
                c.reconnect()
                print(f"[{device['deviceName']}] 重连成功")
                break
            except:
                time.sleep(5)

    client.on_connect = on_connect
    client.on_disconnect = on_disconnect

    try:
        client.connect(MQTT_HOST, MQTT_PORT, keepalive=60)
    except Exception as e:
        print(f"[{device['deviceName']}] 连接失败: {e}")
        return None

    return client


def gen_temperature(hour):
    """生成温度数据: 26.5°C均值 ±9°C昼夜波动 + 噪声"""
    temp = 26.5 + 9 * math.sin((hour - 8) * math.pi / 12) + random.uniform(-0.3, 0.3)
    return round(temp, 1)


def gen_humidity(hour):
    """生成湿度数据: 与温度反相"""
    hum = 70 - 20 * math.sin((hour - 8) * math.pi / 12) + random.uniform(-1, 1)
    return round(max(40, min(95, hum)), 1)


def gen_co2(hour):
    """生成CO2数据: 白天低 夜间高"""
    if 6 <= hour <= 18:
        co2 = 420 + random.uniform(0, 50)
    else:
        co2 = 450 + 80 * math.sin((hour - 20) * math.pi / 10) + random.uniform(0, 30)
    return round(co2)


def gen_light(hour):
    """生成光照数据: 6-18点有光，峰值60000lux"""
    if 6 <= hour <= 18:
        light = 60000 * math.sin((hour - 6) * math.pi / 12)
        light = max(0, light)
        if random.random() < 0.2:
            light *= random.uniform(0.3, 0.7)
    else:
        light = random.uniform(0, 5)
    return round(light)


def run_simulator(device_index=None, interval=30):
    """运行设备模拟器"""
    configs = DEVICE_CONFIGS if device_index is None else [DEVICE_CONFIGS[device_index]]

    clients = {}
    for dev in configs:
        client = create_client(dev, interval)
        if client:
            client.loop_start()
            clients[dev['deviceName']] = (client, dev)

    if not clients:
        print("没有设备连接成功，退出")
        return

    print(f"\n{'='*50}")
    print(f"模拟器已启动: {len(clients)} 个设备, 每 {interval}s 上报一次")
    print(f"按 Ctrl+C 停止\n{'='*50}\n")

    base_time = time.time()

    try:
        while True:
            elapsed = time.time() - base_time
            # 60倍时间加速，模拟一天24小时的周期
            simulated_hour = ((elapsed * 60) / 60) % 24

            for name, (client, dev) in clients.items():
                pk = dev['productKey']
                dn = dev['deviceName']

                if "temp" in dn:
                    val = gen_temperature(simulated_hour)
                    msg = f"温度:{val}℃"
                elif "hum" in dn:
                    val = gen_humidity(simulated_hour)
                    msg = f"湿度:{val}%"
                elif "co2" in dn:
                    val = gen_co2(simulated_hour)
                    msg = f"CO2:{val}ppm"
                elif "light" in dn:
                    val = gen_light(simulated_hour)
                    msg = f"光照:{val}lux"
                else:
                    continue

                topic = f"/sensor/{pk}/{dn}/post"
                try:
                    client.publish(topic, msg, qos=1)
                    print(f"[{name}] topic={topic} → {msg}")
                except Exception as e:
                    print(f"[{name}] publish failed: {e}")

            time.sleep(interval)

    except KeyboardInterrupt:
        print("\n模拟器已停止")
        for name, (client, _) in clients.items():
            client.loop_stop()
            client.disconnect()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="IoT设备MQTT模拟器")
    parser.add_argument("--device", type=int, choices=range(4), help="指定设备索引(0-3)")
    parser.add_argument("--interval", type=int, default=30, help="上报间隔(秒), 默认30")
    args = parser.parse_args()

    print("IoT设备MQTT模拟器 v2.0")
    print(f"MQTT Broker: {MQTT_HOST}:{MQTT_PORT}")
    print(f"可用设备:")
    for i, d in enumerate(DEVICE_CONFIGS):
        print(f"  [{i}] {d['deviceName']} (pk={d['productKey']})")
    print()

    run_simulator(args.device, args.interval)
