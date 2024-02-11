package com.hangout.core.entity;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "_user")
public class User implements UserDetails {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3058452269786940486L;
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String userId;
	@Email
	@Column(unique = true)
	private String email;
	@Column(length = 256)
	private String name;
	@Column(length = 255)
	private String password;
	@Enumerated(EnumType.STRING)
	private Role role;
	@Enumerated(EnumType.STRING)
	private Gender gender;
	@Min(value = 1L, message = "age can not be less than 1")
	private Integer age;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Returns the collection of anything that extends GrantedAuthority class
		// In this case the Set of SimpleGrantedAuthority
		// because this class extends GrantedAuthority class
		return Set.of(new SimpleGrantedAuthority(role.name()));
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
