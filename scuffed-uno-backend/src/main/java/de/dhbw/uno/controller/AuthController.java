package de.dhbw.uno.controller;

import de.dhbw.uno.model.User;
import de.dhbw.uno.service.UserService;
import de.dhbw.uno.config.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import de.dhbw.uno.dto.UserDto;
import de.dhbw.uno.dto.LoginResponseDto;
import org.modelmapper.ModelMapper;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.*;

/**
 * REST-Controller für alle API-Auth Endpunkte: Login, Register
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication Controller", description = "Register, Login and reset password")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService, ModelMapper modelMapper) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.modelMapper = modelMapper;
    }

    /**
     * API-Endpunkt zum Registrierung (Sign Up); Prüft zudem Korrektheit der Anfrage
     * @param username
     * @param email
     * @param password
     * @return
     */
    @Operation(summary = "Sign up")
    @PostMapping("/register")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "500", description = "Invalid input or username taken",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<?> register(@RequestParam String username,
                                      @RequestParam String email,
                                      @RequestParam String password) {

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty");
        }

        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body("Invalid email");
        }

        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters");
        }

        Optional<User> existingUserByUsername = userService.getUserByUsername(username);
        if (existingUserByUsername.isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        Optional<User> existingUserByEmail = userService.getUserByEmail(email);
        if (existingUserByEmail.isPresent()) {
            return ResponseEntity.badRequest().body("Email is already registered");
        }

        User createdUser = userService.createUser(username, email, password);
        UserDto userDto = modelMapper.map(createdUser, UserDto.class);
        return ResponseEntity.ok(userDto);
    }

    /**
     * API-Endpunkt zum Login (Sign in); Prüft zudem Korrektheit der Anfrage
     * @param username
     * @param password
     * @return
     */
    @Operation(summary = "Sign in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid username or password",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestParam String username,
                                                  @RequestParam String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        User user = userService.getUserByUsername(username).get();
        String jwt = jwtService.generateToken(user.getUsername());

        UserDto userDto = modelMapper.map(user, UserDto.class);
        LoginResponseDto response = new LoginResponseDto(jwt, userDto);

        return ResponseEntity.ok(response);
    }




}


