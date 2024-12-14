package com.hangout.core.auth_api.repository;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.auth_api.entity.AccessRecord;

public interface AccessRecordRepo extends JpaRepository<AccessRecord, BigInteger> {
    @Query(value = "select * from access_records where user_id = :id and device_id = :deviceId order by access_token_expiry_time desc limit 1", nativeQuery = true)
    Optional<AccessRecord> getLatestAccess(@Param("id") BigInteger userId, @Param("deviceId") UUID deviceId);

    @Query(value = "select * from access_records where user_id = :id and device_id = :deviceId and action = 0 or action = 3 order by access_token_expiry_time desc limit 1", nativeQuery = true)
    Optional<AccessRecord> getLastEntryRecord(@Param("id") BigInteger userId, @Param("deviceId") UUID deviceId);
}
