package com.atchensong.pojo;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DeviceMessage {

    private Long deviceId;

    private String msg;

}
