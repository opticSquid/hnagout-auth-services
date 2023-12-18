package com.hangout.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hangout.core.entity.User;

public interface UserRepo extends JpaRepository<User, String> {
	Optional<User> findByEmail(String email);

	List<UserNameProjection> findByUserIdIn(List<String> userIds);
}
