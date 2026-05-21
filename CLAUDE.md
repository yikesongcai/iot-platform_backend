# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

An IoT greenhouse monitoring platform with a Spring Boot 3.3 backend and Vue 3 frontend. The system collects sensor data (temperature, humidity, CO2, light) from greenhouse devices via MQTT, displays dashboards with ECharts visualizations, handles alarm/warning events, and supports device management and remote control.

## Architecture

### Backend (`iot-platform_backend/`)
- **Framework**: Spring Boot 3.3 + MyBatis-Plus 3.5.6 + Vert.x 4.2.5
- **Database**: MySQL 8.0 (Druid connection pool); schema defined in root `init.sql`
- **Internal messaging**: Vert.x Event Bus — `VertxMqProducer`/`VertxMqConsumer` publish and consume typed messages on topics defined in `Constants.java`. `DeviceSimulator` publishes simulated sensor data; `MessageConsumeHandler` persists it to the `device_messages` table. `WarningConsumeHandler` handles warning events.
- **MQTT server**: `MqttVerticle` runs an embedded MQTT server on port 1884. Devices connect with credentials; messages are routed by topic convention `{productKey}_{deviceName}_*`. Topic paths determine message type: topics containing "post" trigger property reporting, topics containing "warning" trigger alarm recording.
- **Controllers → Services → Mappers**: Standard MyBatis-Plus layered pattern. `DataServiceImpl` extends `ServiceImpl<DataMapper, DeviceData>` and gains CRUD automatically.
- **Package**: `com.atchensong` — `controller/`, `service/` + `service/impl/`, `mapper/`, `pojo/`, `common/`, `config/`, `component/`, `mq/` + `mq/vertx/`, `handler/`, `aop/`
- **Key config**: `application.yml` — DB credentials, Druid settings, server port 8084, simulator toggle/interval. `vertxConfig.java` wires producer/consumer beans. `MessageConsumerConfiguration.java` and `WarningConsumerConfiguration.java` register the consume handlers.
- **Scheduled tasks**: `DataCleanupTask` prunes old data daily at 3am. `DeviceSimulator` generates fake sensor readings on a configurable interval (default 30s, 60x time acceleration for demo purposes) and publishes to both the property report and warning topics.

### Frontend (`iot-platform_frontend/`)
- **Framework**: Vue 3 + Vue Router 4 + Pinia 3
- **UI libraries**: Element Plus + Ant Design Vue (mixed usage)
- **Charts**: ECharts 5
- **Build tool**: Vue CLI 5 (not Vite, despite `vite` being a dependency; config is `vue.config.js`)
- **Styling**: Less + plain CSS in `src/assets/css/`
- **API layer**: `src/service/request/index.js` wraps axios with a configurable base URL (`src/service/request/config.js` — defaults to `http://localhost:8084`). Some API modules (e.g., `src/api/alarm.js`) use axios directly with hardcoded URLs.
- **State management**: Pinia stores in `src/stores/` (currently `alarm.js`)
- **Router**: Hash-based routing in `src/router/index.js` — login, dashboard, alarm list/detail, user management, system log, device management
- **Components**: `DashBoard.vue` (main dashboard with ECharts), `DeviceList.vue` / `DeviceItem.vue` / `DeviceManagement .vue` (note trailing space in filename), `Login.vue`, `NavBar.vue`, `UserManagement.vue`, `SystemLog.vue`, `UserDialog.vue`, `EnvironmentDataCard.vue`

### Data Flow
1. `DeviceSimulator` (or real MQTT devices) → Vert.x Event Bus topics
2. `MessageConsumeHandler` / `WarningConsumeHandler` persist to `device_messages` / `warning` tables
3. Frontend fetches data via REST controllers → MyBatis-Plus queries → JSON responses (wrapped in `R<T>`)
4. Dashboard uses ECharts for real-time visualization; `DeviceManagement` supports device CRUD and remote command sending via `MqttVerticle.sendMessageToDevice()`

### Database Tables (from `init.sql`)
- `device` — registered IoT devices with productKey, online status
- `device_messages` — time-series sensor readings (temperature, humidity, CO2, light) with value, unit, status
- `alarm_message` — alarm events with JSON params
- `warning` — warning records
- `control` — device command/control history
- `system_log` — system operation logs
- `index_panel` — dashboard panel configuration
- `user` — user accounts

## Commands

### Backend
```bash
cd iot-platform_backend
./mvnw spring-boot:run        # Start backend (requires MySQL with init.sql loaded)
./mvnw test                    # Run tests
./mvnw clean package           # Build JAR
```

### Frontend
```bash
cd iot-platform_frontend
npm install                    # Install dependencies
npm run serve                  # Dev server with hot-reload
npm run build                  # Production build
npm run lint                   # Lint and fix
```

### Prerequisites
- MySQL 8.0 running on localhost:3306 with database `iot_final-training` created (run `init.sql`)
- Java 17+ and Maven (or use the bundled `mvnw`)
- Node 16+
