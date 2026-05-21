# IoT 温室智能监控平台

基于 Spring Boot + Vue 3 的 IoT 温室环境监控系统，集成 MQTT 设备通信、Vert.x 事件总线、ECharts 数据可视化和远程设备控制于一体。

---

## 目录

- [功能介绍](#功能介绍)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [项目结构](#项目结构)
- [快速开始](#快速开始)

---

## 功能介绍

### 🌡️ 实时环境监控

- 仪表盘概览：总设备数、在线率、上报消息数、告警数、CPU 使用率
- 环境数据卡片：实时展示温度、湿度、CO2、光照四项核心指标
- 设备类型分布饼图（ECharts），动态计算各传感器占比
- 最新环境数据一键刷新

### 📡 设备管理

- 设备注册：自动生成唯一 ProductKey，支持设备密钥认证
- 设备列表：按名称 / 在线状态 / ProductKey / 标题多条件检索
- 设备编辑与删除，支持分页查询
- 远程控制下发：通过 MQTT 向指定设备发送控制指令（如开关、参数调节）
- 控制历史记录持久化存储

### 📊 历史数据查询

- 按设备 + 时间范围查询传感器历史数据
- 支持按消息类型（温度 / 湿度 / CO2 / 光照）过滤
- 聚合模式：按小时平均，便于长周期趋势分析
- 原始数据模式：保留完整精度，适合细粒度排查

### 🔔 告警管理

- 告警列表：展示设备名称、告警名称、告警级别、告警时间
- 告警详情：查看告警参数（当前值 vs 阈值）
- 告警级别分级：紧急 / 严重
- 模拟设备自动触发告警（温度过高、湿度过低、CO2 超标）

### 👤 用户管理

- 用户登录 / 登出
- 用户列表分页查询
- 新增 / 编辑 / 删除用户
- 用户状态管理（启用 / 禁用）
- 角色划分：admin / operator

### 📋 系统日志

- 操作日志分页查询，按日志类型筛选
- 记录操作时间、类型、操作人、内容

### 🔄 自动化任务

- **设备模拟器**：定时生成温度、湿度、CO2、光照模拟数据（60x 时间加速，可配置间隔）
- **数据清理**：每日凌晨 3 点自动清理过期数据（device_messages / warning / system_log 保留 30 天，alarm_message 保留 90 天）

---

## 技术栈

| 层级       | 技术                                                      |
| ---------- | --------------------------------------------------------- |
| 前端       | Vue 3 + Vue Router 4 + Pinia 3 + Element Plus + ECharts 5 |
| 后端       | Spring Boot 3.3 + Java 17 + MyBatis-Plus 3.5.6            |
| 消息通信   | Vert.x 4.2.5 Event Bus（内部事件）+ MQTT Server（设备通信） |
| 数据库     | MySQL 8.0 + Druid 连接池                                   |
| 数据清理   | Spring @Scheduled 定时任务                                 |
| 构建工具   | Maven（后端）+ Vue CLI 5（前端）                           |
| 部署       | Docker + Docker Compose + Nginx                            |

---

## 系统架构

```
┌───────────────────────────────────────────────────────────┐
│                    前端 (Vue 3)                            │
│  Login → Dashboard → DeviceManagement → AlarmList         │
│  SystemLog → UserManagement → AlarmDetail                 │
│  (ECharts 仪表盘 / 环境数据卡片 / 设备类型分布图)          │
└────────────────────────┬──────────────────────────────────┘
                         │ HTTP REST / Nginx 反向代理
┌────────────────────────▼──────────────────────────────────┐
│                 后端 (Spring Boot 3.3)                     │
│                                                           │
│  Controllers                                              │
│  ├── PanelController   (/panel, /latest, /message-trend)  │
│  ├── DeviceController  (/device/*)                        │
│  ├── DataController    (/data/history)                    │
│  ├── AlarmController   (/alarm/*)                         │
│  ├── WarningController (/warning/*)                       │
│  ├── UserController    (/user/*)                          │
│  └── SystemLogController (/system-log/*)                  │
│                                                           │
│  MQTT Verticle (端口 1884)                                 │
│  ├── 设备认证（ProductKey + Password）                     │
│  ├── 主题路由（post → 属性上报 / warning → 告警）          │
│  └── 远程控制指令下发                                      │
│                                                           │
│  Vert.x Event Bus                                         │
│  ├── VertxMqProducer ──── publish ──── topics             │
│  │   ├── device.property.post (属性上报)                   │
│  │   └── device.warning (告警事件)                         │
│  └── VertxMqConsumer ─── consume ─── handlers             │
│      ├── MessageConsumeHandler → device_messages 表       │
│      └── WarningConsumeHandler → warning 表               │
│                                                           │
│  定时任务                                                  │
│  ├── DeviceSimulator (可配置间隔，默认 30s)                │
│  └── DataCleanupTask (每日凌晨 3:00)                      │
└───────────┬──────────────────────────────────────────────┘
            │
    ┌───────▼──────┐
    │   MySQL 8.0   │
    │ (core-mysql)  │
    └──────────────┘
```

### 数据流

```
DeviceSimulator                 MQTT 真实设备
      │                              │
      ▼                              ▼
Vert.x Event Bus ───── publish ──── topics
      │
      ▼
MessageConsumeHandler ──── persist ──── device_messages
WarningConsumeHandler  ──── persist ──── warning
      │
      ▼
REST Controllers ──── MyBatis-Plus ──── JSON (R<T>)
      │
      ▼
Nginx /api/* proxy ──── Vue 3 前端 ──── ECharts 可视化
```

---

## 项目结构

```
iot-greenhouse/
├── init.sql                        # 数据库初始化（建表 + 示例数据）
├── optimize.sql                    # 性能优化索引
├── docker-compose.yml              # Docker 编排
├── nginx.conf                      # Nginx 反向代理配置
├── iot-platform_backend/           # 后端 Spring Boot
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/atchensong/
│       ├── controller/             # REST 接口
│       ├── service/ + impl/        # 业务逻辑
│       ├── mapper/                 # MyBatis 数据访问
│       ├── pojo/                   # 实体 / DTO / VO
│       ├── common/                 # 公共类（R 响应体、异常处理、AOP 日志）
│       ├── config/                 # 配置（Vert.x、MyBatis-Plus、Swagger）
│       ├── component/              # 组件（MqttVerticle、DeviceSimulator、DataCleanupTask）
│       ├── mq/ + mq/vertx/         # 消息队列抽象与 Vert.x 实现
│       └── handler/                # 消息消费处理器
└── iot-platform_frontend/          # 前端 Vue 3
    ├── Dockerfile
    ├── vue.config.js
    └── src/
        ├── components/             # 页面组件
        ├── views/                  # 告警详情 / 首页视图
        ├── router/                 # 路由配置
        ├── stores/                 # Pinia 状态管理
        ├── api/                    # API 模块
        ├── service/request/        # Axios 请求封装
        ├── utils/                  # 工具函数（图表配置、日期格式化）
        └── assets/                 # 静态资源（CSS、图片）
```

---

## 快速开始

### 前提条件

- Docker 已安装
- `core-mysql` MySQL 8.0 容器已运行（`docker network ls | grep core-network`）
- 数据库 `iot_final_training` 已通过 `init.sql` 初始化

### 一键启动

```bash
cd iot-greenhouse

# 1. 初始化数据库（首次）
docker exec -i core-mysql mysql -uroot -p123456 < init.sql

# 2. 添加性能索引
docker exec -i core-mysql mysql -uroot -p123456 < optimize.sql

# 3. 构建并启动
docker compose up -d --build

# 4. 访问
# 前端：http://localhost:8085
# 测试账号：admin / 123456
```

### 开发模式

```bash
# 后端（需要本地 MySQL）
cd iot-platform_backend
./mvnw spring-boot:run

# 前端（dev server 自动代理 /api → localhost:8084）
cd iot-platform_frontend
npm install
npm run serve
```

### 模拟器配置

在 `application.yml` 或 `application-docker.yml` 中调整：

```yaml
simulator:
  enabled: true        # 是否启用模拟数据
  interval-ms: 30000   # 上报间隔（毫秒），演示时可改为 10000
```
