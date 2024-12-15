package com.hangout.core.auth_api.repository;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.auth_api.entity.Device;

public interface DeviceRepo extends JpaRepository<Device, UUID> {
    @Query(value = "select * from devices where screen_width = :screenWidth and screen_height = :screenHeight and os = :os and user_agent = :userAgent and country = :country and user_id = :userId", nativeQuery = true)
    Optional<Device> findDevice(@Param("screenWidth") Integer screenWidth, @Param("screenHeight") Integer screenHeight,
            @Param("os") String os, @Param("userAgent") String userAgent, @Param("country") String country,
            @Param("userId") BigInteger userId);

    @Query(value = "select * from devices where device_id = :deviceId and user_id = :userId", nativeQuery = true)
    Optional<Device> validateDeviceOwnership(@Param("deviceId") UUID deviceId, @Param("userId") BigInteger userId);
}
