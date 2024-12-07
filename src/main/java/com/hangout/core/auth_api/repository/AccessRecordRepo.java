package com.hangout.core.auth_api.repository;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.auth_api.entity.AccessRecord;
import com.hangout.core.auth_api.entity.Action;

public interface AccessRecordRepo extends JpaRepository<AccessRecord, BigInteger> {
    @Query(value = "select * from access_records where user_id = :id and ip_address = :ip order by last_seen desc limit 1", nativeQuery = true)
    Optional<AccessRecord> getLatestAccess(@Param("id") BigInteger userId, @Param("ip") String ipAddr);

    @Query(value = "select last_seen from accesss_records where user_id = :id order by last_seen desc limit 1", nativeQuery = true)
    Optional<LocalDateTime> getLastSeen(@Param("id") BigInteger userId);

    // ! if I don't keep the sction separate it is not mapping back to correct enum
    // ! value
    @Query(value = "select action from access_records where user_id = :id and ip_address = :ip and action = 0 or action = 3 order by last_seen desc limit 1", nativeQuery = true)
    Optional<Action> getLastEntryAttemptAction(@Param("id") BigInteger userId, @Param("ip") String ipAddr);

    @Query(value = "select refresh_token_expiry_time  from access_records where user_id = :id and ip_address = :ip and action = 0 or action = 3 order by last_seen desc limit 1", nativeQuery = true)
    Optional<Instant> getLastEntryAttemptRefTokenExpiry(@Param("id") BigInteger userId, @Param("ip") String ipAddr);
}
