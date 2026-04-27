package de.dhbw.uno.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for joining an existing UNO game room")
public class JoinRoomRequest {
    
    @Schema(description = "Room ID to join", example = "ABC12345", required = true)
    private String roomId;
    
    @Schema(description = "Player name who is joining the room", example = "JaneDoe", required = true)
    private String playerName;
    
    public JoinRoomRequest() {}
    
    public JoinRoomRequest(String roomId, String playerName) {
        this.roomId = roomId;
        this.playerName = playerName;
    }
    
    public String getRoomId() { 
        return roomId; 
    }
    
    public void setRoomId(String roomId) { 
        this.roomId = roomId; 
    }
    
    public String getPlayerName() { 
        return playerName; 
    }
    
    public void setPlayerName(String playerName) { 
        this.playerName = playerName; 
    }
} 