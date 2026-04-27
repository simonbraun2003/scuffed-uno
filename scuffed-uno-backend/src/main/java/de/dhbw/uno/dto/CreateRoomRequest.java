package de.dhbw.uno.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for creating a new UNO game room")
public class CreateRoomRequest {
    
    @Schema(description = "Player name who is creating the room", example = "JohnDoe", required = true)
    private String playerName;
    
    public CreateRoomRequest() {}
    
    public CreateRoomRequest(String playerName) {
        this.playerName = playerName;
    }
    
    public String getPlayerName() { 
        return playerName; 
    }
    
    public void setPlayerName(String playerName) { 
        this.playerName = playerName; 
    }
} 