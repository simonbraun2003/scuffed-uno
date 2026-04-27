package de.dhbw.uno.service;

import de.dhbw.uno.model.User;
import de.dhbw.uno.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User("johndoe", "john@example.com", "hashedPassword");
    }

    @Test
    void createUser_shouldHashPasswordAndSaveUser() {
        // Arrange
        String rawPassword = "password123";
        String hashedPassword = "hashed123";
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userService.createUser("johndoe", "john@example.com", rawPassword);

        // Assert
        assertEquals("johndoe", createdUser.getUsername());
        assertEquals("john@example.com", createdUser.getEmail());
        assertEquals(hashedPassword, createdUser.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById_shouldReturnUserIfExists() {
        when(userRepository.findById("123")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById("123");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void getUserByUsername_shouldReturnUserIfExists() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByUsername("johndoe");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void getUserByEmail_shouldReturnUserIfExists() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

        User result = (User) userService.loadUserByUsername("johndoe");

        assertEquals(user, result);
    }

    @Test
    void loadUserByUsername_shouldThrowIfUserNotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nouser");
        });
    }
}
