package com.hangout.core.auth_api.entity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
	private BigInteger userId;
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
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<Device> devices;
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<AccessRecord> accessRecords;
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

	public void addNewDevice(Device device) {
		if (this.devices == null) {
			this.devices = new ArrayList<>();
		}
		this.devices.add(device);
	}

	public void addAccessRecord(AccessRecord accessRecord) {
		if (this.accessRecords == null) {
			this.accessRecords = new ArrayList<>();
		}
		this.accessRecords.add(accessRecord);
	}
}
