package com.atchensong.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据清理任务：定期清理过期数据，防止数据膨胀影响性能。
 * device_messages/warning/system_log 保留30天，alarm_message 保留90天。
 */
@Slf4j
@Component
@EnableScheduling
public class DataCleanupTask {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 3 * * * ?") // 每天凌晨3:00执行
    public void cleanOldData() {
        log.info("[数据清理]: 开始执行数据清理...");
        long start = System.currentTimeMillis();

        int dm = cleanTable("device_messages", 30);
        int w = cleanTable("warning", 30);
        int sl = cleanTable("system_log", 30);
        int am = cleanAlarmMessages(90);

        long elapsed = System.currentTimeMillis() - start;
        log.info("[数据清理]: 完成，清理 device_messages:{}条, warning:{}条, system_log:{}条, alarm_message:{}条, 耗时:{}ms",
                dm, w, sl, am, elapsed);
    }

    private int cleanTable(String table, int days) {
        try {
            int rows = jdbcTemplate.update(
                    "DELETE FROM " + table + " WHERE create_time < ?",
                    LocalDateTime.now().minusDays(days));
            return rows;
        } catch (Exception e) {
            log.warn("[数据清理]: 清理表{}失败: {}", table, e.getMessage());
            return 0;
        }
    }

    private int cleanAlarmMessages(int days) {
        try {
            int rows = jdbcTemplate.update(
                    "DELETE FROM alarm_message WHERE alarm_time < ?",
                    LocalDateTime.now().minusDays(days));
            return rows;
        } catch (Exception e) {
            log.warn("[数据清理]: 清理alarm_message失败: {}", e.getMessage());
            return 0;
        }
    }
}
