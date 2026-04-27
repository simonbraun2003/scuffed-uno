package de.dhbw.uno.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Response payload for UNO game room operations")
public class GameRoomResponse {
    
    @Schema(description = "Indicates if the operation was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Response message", example = "Room created successfully")
    private String message;
    
    @Schema(description = "Room ID", example = "ABC12345")
    private String roomId;
    
    @Schema(description = "Number of players currently in the room", example = "2")
    private Integer playerCount;
    
    @Schema(description = "List of player names in the room")
    private List<String> players;
    
    @Schema(description = "Whether the game has started", example = "false")
    private Boolean gameStarted;
    
    @Schema(description = "Current player's turn", example = "JohnDoe")
    private String currentPlayer;
    
    public GameRoomResponse() {}
    
    public GameRoomResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Getters and Setters
    public boolean isSuccess() { 
        return success; 
    }
    
    public void setSuccess(boolean success) { 
        this.success = success; 
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public void setMessage(String message) { 
        this.message = message; 
    }
    
    public String getRoomId() { 
        return roomId; 
    }
    
    public void setRoomId(String roomId) { 
        this.roomId = roomId; 
    }
    
    public Integer getPlayerCount() { 
        return playerCount; 
    }
    
    public void setPlayerCount(Integer playerCount) { 
        this.playerCount = playerCount; 
    }
    
    public List<String> getPlayers() { 
        return players; 
    }
    
    public void setPlayers(List<String> players) { 
        this.players = players; 
    }
    
    public Boolean getGameStarted() { 
        return gameStarted; 
    }
    
    public void setGameStarted(Boolean gameStarted) { 
        this.gameStarted = gameStarted; 
    }
    
    public String getCurrentPlayer() { 
        return currentPlayer; 
    }
    
    public void setCurrentPlayer(String currentPlayer) { 
        this.currentPlayer = currentPlayer; 
    }
} 