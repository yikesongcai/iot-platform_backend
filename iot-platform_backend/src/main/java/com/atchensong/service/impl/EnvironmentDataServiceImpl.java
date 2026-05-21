package com.atchensong.service.impl;

import com.atchensong.mapper.EnvironmentDataMapper;
import com.atchensong.pojo.EnvironmentDataDTO;
import com.atchensong.service.EnvironmentDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnvironmentDataServiceImpl implements EnvironmentDataService {
    private final EnvironmentDataMapper messageMapper;

    public EnvironmentDataDTO getLatestEnvironmentData() {

        return messageMapper.selectLatestEnvironmentData();
    }
}
