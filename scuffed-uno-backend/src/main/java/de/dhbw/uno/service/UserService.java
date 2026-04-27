package de.dhbw.uno.service;
import de.dhbw.uno.model.User;

import de.dhbw.uno.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    // Zugriff auf die Benutzer-Datenbank
    private final UserRepository userRepository;
    // Verschlüsselt Passwörter vor dem Speichern
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Erstellt einen neuen Benutzer mit gehashtem Passwort
    public User createUser(String username, String email, String plainPassword) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        User user = new User(username, email, hashedPassword);
        return userRepository.save(user);
    }

    // Sucht Benutzer per ID
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    // Sucht Benutzer per Benutzername
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Sucht Benutzer per E-Mail
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Von Spring Security für den Login verwendet
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // Gibt die Anzahl aller Benutzer zurück
    public long getUserCount() {
        return userRepository.count();
    }
}
