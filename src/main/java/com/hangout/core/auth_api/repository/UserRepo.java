package com.hangout.core.auth_api.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hangout.core.auth_api.entity.User;

public interface UserRepo extends JpaRepository<User, BigInteger> {
	Optional<User> findByUserName(String username);

	Optional<User> findByEmail(String email);

	@Modifying
	@Query(value = "update user_creds set enabled = true where email = :email", nativeQuery = true)
	void activateAccount(@Param("email") String email);

	@Modifying
	@Query(value = "delete from user_creds where user_name = :userName", nativeQuery = true)
	void deleteByUserName(@Param("userName") String username);
}
