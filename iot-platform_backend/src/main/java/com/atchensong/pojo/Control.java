package com.atchensong.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("control")
public class Control {
    private Long id;
    private Long deviceId;
    private String actionName;
    private String content;
    private LocalDateTime timestamp;
}
