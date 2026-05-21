package com.atchensong.service;


import com.atchensong.pojo.EnvironmentDataDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface EnvironmentDataService {
    public EnvironmentDataDTO getLatestEnvironmentData();

}
