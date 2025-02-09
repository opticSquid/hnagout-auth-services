package com.hangout.core.auth_api.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.auth_api.entity.Device;

public interface DeviceRepo extends JpaRepository<Device, UUID> {
    @Query(value = "SELECT * FROM devices WHERE os = :os AND screen_width = :screenWidth AND screen_height = :screenHeight AND user_agent = :userAgent AND continent = :continent AND country = :country AND user_id = :userId", nativeQuery = true)
    List<Device> findAllMatchingDevices(
            @Param("os") String os,
            @Param("screenWidth") Integer screenWidth,
            @Param("screenHeight") Integer screenHeight,
            @Param("userAgent") String userAgent,
            @Param("continent") String continent,
            @Param("country") String country,
            @Param("userId") BigInteger userId);

    @Query(value = "select * from devices where device_id = :deviceId and user_id = :userId", nativeQuery = true)
    Optional<Device> validateDeviceOwnership(@Param("deviceId") UUID deviceId, @Param("userId") BigInteger userId);
}
