package com.onion.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class WriteDeviceDto {
    private String deviceName;
    private String token;
}
