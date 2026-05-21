package com.atchensong;

import com.atchensong.common.DateFormatValidator;
import com.atchensong.common.RandomUtil;
import com.atchensong.pojo.DeviceData;
import com.atchensong.service.DataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IotFinalTrainingApplicationTests {

    @Autowired
    DataService dataService;


    @Test
    void contextLoads() {

        LambdaQueryWrapper<DeviceData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(DeviceData::getCreateTime, "2024-06-19", "2024-06-20");
        dataService.list(queryWrapper).forEach(System.out::println);

    }

    @Test
    void testDateFormate() {
        boolean b1 = DateFormatValidator.isValidDateFormat("2024-06-1");
        boolean b2 = DateFormatValidator.isValidDateFormat("2024-06-20");
        if (!(b1 && b2)) {
            System.out.println("日期格式有误");
            return;
        }

        System.out.println(b1);
        System.out.println(b2);
    }

    @Test
    void testRandomPK() {
        String randomKey = RandomUtil.getRandomKey("三路网关", "网关设备");
        System.out.println(randomKey.toLowerCase());
    }

}
