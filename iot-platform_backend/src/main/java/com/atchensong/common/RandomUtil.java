package com.atchensong.common;

import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RandomUtil {
    public static String getRandomKey(String ProductName,String category) {
        String seed=ProductName+category+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return DigestUtils.md5Hex(seed).substring(8,24).toUpperCase();
    }
    public static String getRandomSecret(String ProductName,String category){
        String t=LocalDateTime.now().format(DateTimeFormatter.ofPattern("mmMMddyyyyHHss"));
        String seed=category+ t+ProductName;
        return DigestUtils.md5Hex(seed);
    }
}
