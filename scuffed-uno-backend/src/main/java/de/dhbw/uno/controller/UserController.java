package de.dhbw.uno.controller;

import de.dhbw.uno.model.User;
import de.dhbw.uno.dto.UserDto;
import de.dhbw.uno.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import org.modelmapper.ModelMapper;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST-Controller, der User-spezifische Endpunkt(e) bereitstellt: Get User,
 */
@RestController
@Tag(name = "User Controller")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * API-Endpunkt, der die User-Details zurückgibt
     * @param id
     * @return
     */
    @Operation(summary = "Get user details", description = "Fetches user details by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> getUser(
            @Parameter(description = "Id of the user to fetch", required = true)
            @PathVariable String id) {

        Optional<User> user = userService.getUserById(id);

        if (user.isPresent()) {
            UserDto dto = modelMapper.map(user.get(), UserDto.class);
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
