package com.hangout.core.hangoutauthservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Entity
@Getter
@Table(name = "_user")
@NoArgsConstructor
public class User implements UserDetails {
    // Auto generated
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;
    // Sensitive details
    private String name;
    @Min(value = 1L, message = "age can not be less than 1")
    private Integer age;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    // credential details
    @Email
    @Column(unique = true)
    private String email;
    private String password;
    // Authorization Details
    @Enumerated(EnumType.STRING)
    private Role role;
    @Setter
    @Enumerated(EnumType.STRING)
    private Authorization authorization;
    // Access Control
    @Setter
    private Boolean isAccountNonExpired;
    @Setter
    private Boolean isAccountNonLocked;
    @Setter
    private Boolean isCredentialsNonExpired;
    /**
     * this is used as a isVerified field
     * this will be turned to true after user verification
     **/
    @Setter
    private Boolean isEnabled;

    public User(String name, Integer age, Gender gender, String email, String password, Role role, Authorization authorization, Boolean isAccountNonExpired, Boolean isAccountNonLocked, Boolean isCredentialsNonExpired, Boolean isEnabled) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.password = password;
        this.role = role;
        this.authorization = authorization;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Returns the collection of anything that extends GrantedAuthority class
        // In this case the Set of SimpleGrantedAuthority
        // because this class extends GrantedAuthority class
        return Set.of(new SimpleGrantedAuthority(authorization.name()));
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
        return isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

}
