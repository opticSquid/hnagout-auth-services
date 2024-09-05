package com.hangout.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.entity.User;

public interface UserRepo extends JpaRepository<User, Long> {
	User findByUserName(String username);

	@Query(value = "delete from user_creds where user_name=:userName", nativeQuery = true)
	@Modifying
	void deleteByUserName(@Param("userName") String username);
}
