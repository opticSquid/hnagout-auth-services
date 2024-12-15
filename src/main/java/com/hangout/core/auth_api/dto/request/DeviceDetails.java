package com.hangout.core.auth_api.dto.request;

public record DeviceDetails(String ip, String os, Integer screenWidth, Integer screenHeight, String userAgent) {

}
