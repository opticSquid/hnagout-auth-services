package com.hangout.core.auth_service.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.auth_service.entity.User;

public interface UserRepo extends JpaRepository<User, BigInteger> {
	User findByUserName(String username);

	@Query(value = "delete from user_creds where user_name=:userName", nativeQuery = true)
	@Modifying
	void deleteByUserName(@Param("userName") String username);
}
