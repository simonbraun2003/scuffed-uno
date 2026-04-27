package de.dhbw.uno.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Entity Klasse für den Benutzer / Spieler, welche ebenso in der DB zu finden ist; Spring Data spezifisch
 */
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class User implements UserDetails {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Setter
    @Getter
    @Column(nullable = false, unique = true)
    private String username;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String hashed_password;

    public User() {}


    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.hashed_password = password;

    }

    public void setPassword(String hashed_password) {
        this.hashed_password = hashed_password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.hashed_password;
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

    @Override
    public String getUsername() {
        return this.username;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }


}
