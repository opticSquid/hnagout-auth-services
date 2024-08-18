package com.hangout.core.entity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@Table(name = "user_creds")
@NoArgsConstructor
public class User implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long userId;
	@NonNull
	@Column(unique = true)
	private String userName;
	@NonNull
	@Column(unique = true)
	@Email
	private String email;
	@NonNull
	@Length(min = 8)
	private String password;
	@JsonIgnore
	private Roles role;
	@JsonIgnore
	private Boolean enabled;

	public User(@NonNull String userName, @NonNull String email, @NonNull String password) {
		this.userName = userName;
		this.email = email;
		this.password = password;
		this.role = Roles.USER;
		this.enabled = false;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Set.of(new SimpleGrantedAuthority(role.name()));
	}

	@Override
	public String getUsername() {
		return this.userName;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
}
