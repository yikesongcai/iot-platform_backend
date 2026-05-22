-- ============================================
-- IoT Greenhouse 数据库性能优化脚本
-- 为长时间运行场景添加必要索引
-- ============================================

USE `iot_final_training`;

-- 1. device_messages 表：核心查询优化
-- 查询模式：WHERE device_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp
ALTER TABLE `device_messages` ADD INDEX `idx_device_timestamp` (`device_id`, `timestamp`);
-- 清理任务：DELETE FROM device_messages WHERE create_time < ?
ALTER TABLE `device_messages` ADD INDEX `idx_create_time` (`create_time`);
-- 按消息类型过滤查询
ALTER TABLE `device_messages` ADD INDEX `idx_message_type` (`message_type`);

-- 2. warning 表：清理任务优化
ALTER TABLE `warning` ADD INDEX `idx_warning_create_time` (`create_time`);
-- 按设备查询告警
ALTER TABLE `warning` ADD INDEX `idx_warning_device_id` (`device_id`);

-- 3. alarm_message 表：清理和查询优化
ALTER TABLE `alarm_message` ADD INDEX `idx_alarm_time` (`alarm_time`);
ALTER TABLE `alarm_message` ADD INDEX `idx_alarm_device_id` (`device_id`);

-- 4. system_log 表：清理任务优化
ALTER TABLE `system_log` ADD INDEX `idx_system_log_create_time` (`create_time`);

-- 5. control 表：按设备查询优化
ALTER TABLE `control` ADD INDEX `idx_control_device_id` (`device_id`);
ALTER TABLE `control` ADD INDEX `idx_control_timestamp` (`timestamp`);
