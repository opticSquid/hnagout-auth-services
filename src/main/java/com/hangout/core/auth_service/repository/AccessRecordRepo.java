package com.hangout.core.auth_service.repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.auth_service.entity.AccessRecord;

public interface AccessRecordRepo extends JpaRepository<AccessRecord, BigInteger> {
    @Query(value = "select * from access_record where user_id = :id and ip_address = :ip order by last_seen desc limit 1", nativeQuery = true)
    Optional<AccessRecord> getLatestAccess(@Param("id") BigInteger userId, @Param("ip") String ipAddr);

    @Query(value = "select last_seen from accesss_record where user_id = :id order by last_seen desc limit 1", nativeQuery = true)
    Optional<LocalDateTime> getLastSeen(@Param("id") BigInteger userId);
}
