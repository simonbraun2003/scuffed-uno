package de.dhbw.uno.controller;

import de.dhbw.uno.service.GameRoomService;
import de.dhbw.uno.service.GameStateService;
import de.dhbw.uno.dto.CreateRoomRequest;
import de.dhbw.uno.dto.JoinRoomRequest;
import de.dhbw.uno.dto.GameRoomResponse;
import de.dhbw.uno.dto.CardDataDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * REST-Controller für alle Endpunkte in Bezug auf die eigentlichen Spielräume: Raum erstellen/joinen, Statusabfrage, Karte spielen, usw.
 */
@RestController
@RequestMapping("/api/game")
@Tag(name = "UNO Game Room Management", description = "REST API for managing UNO multiplayer game rooms")
public class GameWebSocketController {

    private final GameRoomService gameRoomService;
    private final GameStateService gameStateService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GameWebSocketController(GameRoomService gameRoomService, GameStateService gameStateService, SimpMessagingTemplate messagingTemplate) {
        this.gameRoomService = gameRoomService;
        this.gameStateService = gameStateService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * API-Endpunkt, um einen Raum zu erstellen
     * @param request
     * @return
     */
    @Operation(
        summary = "Create a new UNO game room",
        description = "Creates a new multiplayer UNO game room and adds the creator as the first player",
        tags = {"Room Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Room created successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request - Player name is required",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class)))
    })
    @PostMapping("/create-room")
    public ResponseEntity<GameRoomResponse> createRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Player information for room creation",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateRoomRequest.class))
            )
            @RequestBody CreateRoomRequest request) {
        try {
            String playerName = request.getPlayerName();
            
            if (playerName == null || playerName.trim().isEmpty()) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Player name is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String gameId = gameRoomService.createGameRoom(playerName);
            gameRoomService.addPlayerToRoom(gameId, playerName);
            
            // Get the created room to populate response with complete information
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(gameId);
            
            GameRoomResponse response = new GameRoomResponse(true, "Room created successfully");
            response.setRoomId(gameId);
            response.setPlayerCount(gameRoom.getPlayers().size());
            response.setPlayers(gameRoom.getPlayers());
            response.setGameStarted(gameRoom.isGameStarted());
            response.setCurrentPlayer(gameRoom.getCurrentPlayer());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            GameRoomResponse errorResponse = new GameRoomResponse(false, "Failed to create room: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API-Endpunkt, um einem Raum zu joinen
     * @param request
     * @return
     */
    @Operation(
        summary = "Join an existing UNO game room",
        description = "Allows a player to join an existing UNO game room using the room ID",
        tags = {"Room Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully joined room",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request - Room ID or player name missing/invalid",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Room not found",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class)))
    })
    @PostMapping("/join-room")
    public ResponseEntity<GameRoomResponse> joinRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Room and player information for joining",
                required = true,
                content = @Content(schema = @Schema(implementation = JoinRoomRequest.class))
            )
            @RequestBody JoinRoomRequest request) {
        try {
            String roomId = request.getRoomId();
            String playerName = request.getPlayerName();
            
            if (roomId == null || roomId.trim().isEmpty()) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Room ID is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (playerName == null || playerName.trim().isEmpty()) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Player name is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(roomId);
            
            if (gameRoom == null) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Room not found");
                return ResponseEntity.notFound().build();
            }
            
            if (gameRoom.getPlayers().size() >= 4) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Room is full (maximum 4 players)");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (gameRoom.getPlayers().contains(playerName)) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Player already in room");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            gameRoomService.addPlayerToRoom(roomId, playerName);
            
            // Notify other players via WebSocket
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "PLAYER_JOINED");
            notification.put("player", playerName);
            notification.put("playerCount", gameRoom.getPlayers().size());
            notification.put("message", playerName + " joined the room");
            
            messagingTemplate.convertAndSend("/topic/room/" + roomId, notification);
            
            GameRoomResponse response = new GameRoomResponse(true, "Successfully joined room");
            response.setRoomId(roomId);
            response.setPlayerCount(gameRoom.getPlayers().size());
            response.setPlayers(gameRoom.getPlayers());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            GameRoomResponse errorResponse = new GameRoomResponse(false, "Failed to join room: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API-Endpunkt, um den Status eines Raumes abzufragen
     * @param roomId
     * @return
     */
    @Operation(
        summary = "Get UNO game room status",
        description = "Retrieves current status and information about a specific UNO game room",
        tags = {"Room Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Room status retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Room not found",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class)))
    })
    @GetMapping("/room/{roomId}/status")
    public ResponseEntity<GameRoomResponse> getRoomStatus(
            @Parameter(description = "Room ID to check status for", example = "ABC12345", required = true)
            @PathVariable String roomId) {
        try {
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(roomId);
            
            if (gameRoom == null) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Room not found");
                return ResponseEntity.notFound().build();
            }
            
            GameRoomResponse response = new GameRoomResponse(true, "Room status retrieved successfully");
            response.setRoomId(roomId);
            response.setPlayerCount(gameRoom.getPlayers().size());
            response.setPlayers(gameRoom.getPlayers());
            response.setGameStarted(gameRoom.isGameStarted());
            response.setCurrentPlayer(gameRoom.getCurrentPlayer());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            GameRoomResponse errorResponse = new GameRoomResponse(false, "Failed to get room status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API-Endpunkt, um im laufenden Spiel eines Raumes eine Karte zu spielen; z.B. Rote 6
     * @param cardMessage
     */
    @MessageMapping("/playCard") // e.g., /app/playCard
    public void playCard(Map<String, String> cardMessage) {
        String gameId = cardMessage.get("gameId");
        String card = cardMessage.get("card");
        String color = cardMessage.get("color");
        String playerName = cardMessage.get("player");
        String chosenColor = cardMessage.get("chosenColor");
        System.out.println("GameID: " + gameId + ", Card: " + card + ", Color: " + color + ", playerName: " + playerName + ", chosenColor: " + chosenColor);
        try {
            GameStateService.MultiplayerGameState gameState = gameStateService.getGame(gameId);
            System.out.println("[playCard] gameState instance: " + System.identityHashCode(gameState));
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(gameId);
            System.out.println("[playCard] gameRoom instance: " + System.identityHashCode(gameRoom));
            
            if (gameState != null && gameState.isGameStarted()) {
                System.out.println("Play-Card: GameState != null und game is started");
                // Parse card value and color from card string (e.g., "red_7")
                String[] cardParts = card.split("_");
                String cardColor = cardParts.length > 1 ? cardParts[0] : color;
                String cardValue = cardParts.length > 1 ? cardParts[1] : card;
                
                // Create CardDataDTO for backend validation
                CardDataDTO cardToPlay = new CardDataDTO(cardValue, cardColor);
                
                // Validate if player can play this card
                if (!gameState.getCurrentPlayerName().equals(playerName)) {
                    System.out.println("Not your turn: " + playerName);
                    throw new IllegalStateException("Not your turn");
                }
                
                if (!gameState.canPlayCard(cardToPlay, playerName)) {
                    System.out.println("Cant play card / invalid card play: " + playerName + ", card: " + cardToPlay);
                    throw new IllegalStateException("Invalid card play");
                }
                
                // Play the card through backend logic
                gameState.playCard(playerName, cardToPlay, chosenColor);
                System.out.println("Played card");
                // Debug: Print full game state after card is played
                System.out.println("--- Game State After Card Played ---");
                System.out.println("Current player: " + gameState.getCurrentPlayerName());
                System.out.println("Current color: " + gameState.getCurrentColor());
                System.out.println("Top card: " + gameState.getTopCard());
                System.out.println("Direction: " + (gameState.isClockwise() ? "clockwise" : "counter-clockwise"));
                System.out.println("Draw stack: " + gameState.getDrawStack());
                for (String p : gameState.getPlayerOrder()) {
                    System.out.println("Player: " + p + ", Hand: " + gameState.getPlayerHand(p));
                }
                System.out.println("-------------------------------");
                
                // Broadcast successful card play to all players
                Map<String, Object> response = new HashMap<>();
                response.put("type", "CARD_PLAYED");
                response.put("player", playerName);
                response.put("card", card);
                response.put("gameId", gameId);
                response.put("currentPlayer", gameState.getCurrentPlayerName());
                response.put("topCard", gameState.getTopCard().toString());
                response.put("currentColor", gameState.getCurrentColor());
                response.put("clockwise", gameState.isClockwise());
                response.put("drawStack", gameState.getDrawStack());
                response.put("direction", gameState.isClockwise() ? 1 : -1);
                
                // Check for winner
                if (gameState.isGameOver()) {
                    System.out.println("Game Over, Winner is selected");
                    response.put("winner", gameState.getWinner());
                    response.put("type", "GAME_OVER");
                }
                
                // Send updated game state to each player with their specific hand
                for (String player : gameState.getPlayerOrder()) {
                    Map<String, Object> playerResponse = new HashMap<>(response);
                    playerResponse.put("playerHand", gameState.getPlayerHand(player));
                    playerResponse.put("players", gameState.getPlayerOrder());
                    playerResponse.put("playerIndex", gameState.getPlayerOrder().indexOf(player));
                    // Calculate hand sizes for all players
                    Map<String, Integer> handSizes = new HashMap<>();
                    for (String p : gameState.getPlayerOrder()) {
                        handSizes.put(p, gameState.getPlayerHand(p).size());
                    }
                    playerResponse.put("handSizes", handSizes);
                    System.out.println("broadcast to player: " + playerResponse);
                    messagingTemplate.convertAndSendToUser(player, "/queue/gameState", playerResponse);
                }
                System.out.println("Room response");
                // Broadcast to room
                messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
                
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "INVALID_MOVE");
            errorResponse.put("message", "Failed to play card: " + e.getMessage());
            System.out.println("ERR Play card: " + errorResponse);
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
        }
    }

    /**
     * API-Endpunkt, um im laufenden Spiel eines Raumes eine Karte zu ziehen; z.B. Rote 6
     * @param drawMessage
     */
    @MessageMapping("/drawCard") // e.g., /app/drawCard
    public void drawCard(Map<String, String> drawMessage) {
        String gameId = drawMessage.get("gameId");
        String playerName = drawMessage.get("player");
        String drawCount = drawMessage.get("drawCount");
        System.out.println("drawCard: " + gameId + ", " + playerName + ", " + drawCount);
        try {
            GameStateService.MultiplayerGameState gameState = gameStateService.getGame(gameId);
            System.out.println("[drawCard] gameState instance: " + System.identityHashCode(gameState));
            if (gameState != null && gameState.isGameStarted()) {
                // Validate it's player's turn
                if (!gameState.getCurrentPlayerName().equals(playerName)) {
                    System.out.println("Not your turn: " + playerName);
                    throw new IllegalStateException("Not your turn");
                }
                int cardsToTraw = drawCount != null ? Integer.parseInt(drawCount) : 1;
                if (gameState.getDrawStack() > 0) {
                    // Forced draw: always allow, do not check for playable cards
                    System.out.println("Draw stack: " + gameState.getDrawStack());
                    gameState.handleForcedDraw(playerName);
                    cardsToTraw = gameState.getDrawStack();
                } else {
                    // Normal draw: check for playable cards
                    List<CardDataDTO> playerHand = gameState.getPlayerHand(playerName);
                    System.out.println("Checking playable cards for player: " + playerName);
                    System.out.println("Current color: " + gameState.getCurrentColor());
                    System.out.println("Top card: " + gameState.getTopCard());
                    for (CardDataDTO card : playerHand) {
                        System.out.println("Card in hand: " + card + ", canPlayCard: " + gameState.canPlayCard(card, playerName));
                    }
                    boolean hasPlayable = false;
                    for (CardDataDTO card : playerHand) {
                        if (gameState.canPlayCard(card, playerName)) {
                            hasPlayable = true;
                            System.out.println("has playable");
                            break;
                        }
                    }
                    if (hasPlayable) {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("type", "ERROR");
                        errorResponse.put("message", "You have a playable card and cannot draw.");
                        System.out.println("ERR Draw card: " + errorResponse);
                        messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
                        return;
                    }
                    // Normal draw
                    gameState.drawCards(playerName, cardsToTraw);
                    gameState.moveToNextPlayer();
                    System.out.println("Card drawed");
                }
                // Broadcast draw action to all players
                Map<String, Object> response = new HashMap<>();
                response.put("type", "CARDS_DRAWN");
                response.put("player", playerName);
                response.put("drawCount", cardsToTraw);
                response.put("gameId", gameId);
                response.put("currentPlayer", gameState.getCurrentPlayerName());
                response.put("drawStack", gameState.getDrawStack());
                // Send updated game state to each player
                for (String player : gameState.getPlayerOrder()) {
                    Map<String, Object> playerResponse = new HashMap<>(response);
                    playerResponse.put("playerHand", gameState.getPlayerHand(player));
                    playerResponse.put("players", gameState.getPlayerOrder());
                    playerResponse.put("playerIndex", gameState.getPlayerOrder().indexOf(player));
                    // Calculate hand sizes for all players
                    Map<String, Integer> handSizes = new HashMap<>();
                    for (String p : gameState.getPlayerOrder()) {
                        handSizes.put(p, gameState.getPlayerHand(p).size());
                    }
                    playerResponse.put("handSizes", handSizes);
                    System.out.println("Handsizes: " + handSizes);
                    messagingTemplate.convertAndSendToUser(player, "/queue/gameState", playerResponse);
                }
                // Broadcast to room
                System.out.println("Room mssg");
                messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            System.out.println("ERR draw card final");
            errorResponse.put("type", "ERROR");
            errorResponse.put("message", "Failed to draw card: " + e.getMessage());
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
        }
    }

    /**
     * API-Endpunkt, um ein Web-Socket zu testen
     * @param message
     * @return
     */
    @MessageMapping("/test") // e.g., /app/test  
    @SendTo("/topic/test")
    public String testMessage(String message) {
        return "Echo: " + message;
    }

    /**
     * API-Endpunkt, um den Spieler als bereit in einem Raum zu definieren
     * @param readyMessage
     */
    @MessageMapping("/playerReady")
    public void playerReady(Map<String, String> readyMessage) {
        String gameId = readyMessage.get("gameId");
        String playerName = readyMessage.get("player");
        GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(gameId);
        if (gameRoom != null) {
            gameRoom.setPlayerReady(playerName, true);
            // Send updated readyStates to the client who sent PLAYER_READY
            Map<String, Object> response = new HashMap<>();
            response.put("type", "READY_UPDATE");
            response.put("readyStates", gameRoom.getPlayerReadyStates());
            messagingTemplate.convertAndSendToUser(playerName, "/queue/gameState", response);
        }
    }

    /**
     * API-Endpunkt, um in einem Raum das Spiel zu starten
     * @param startMessage
     */
    @MessageMapping("/startGame") // e.g., /app/startGame
    @SendToUser("/queue/gameState")
    public void startGame(Map<String, String> startMessage) {
        System.out.println("Received Start message");
        String gameId = startMessage.get("gameId");
        String playerName = startMessage.get("player");
        
        try {
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(gameId);
            if (gameRoom != null && gameRoom.getPlayers().size() >= 2) {
                // Only the creator can start the game
                if (!playerName.equals(gameRoom.getCreatorId())) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("type", "ERROR");
                    errorResponse.put("message", "Only the room creator can start the game.");
                    System.out.println("ERR ROOM CREATOR");
                    System.out.println("ERROR: " + gameRoom.getCreatorId() + "; PlayerName: " + playerName);
                    messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
                    return;
                }
                if (!gameRoom.allPlayersReady()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("type", "ERROR");
                    errorResponse.put("message", "Not all players are ready.");
                    System.out.println("ERR PLAYER READY");
                    // Add list of not ready players
                    List<String> notReadyPlayers = new ArrayList<>();
                    for (Map.Entry<String, Boolean> entry : gameRoom.getPlayerReadyStates().entrySet()) {
                        if (!entry.getValue()) {
                            notReadyPlayers.add(entry.getKey());
                        }
                    }
                    errorResponse.put("notReadyPlayers", notReadyPlayers);
                    messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
                    return;
                }
                // Create backend game state
                GameStateService.MultiplayerGameState gameState = gameStateService.createGame(gameId);
                gameState.startGame(gameRoom.getPlayers());
                // Mark room as started
                gameRoom.startGame(playerName);
                // Notify all players that game has started
                Map<String, Object> response = new HashMap<>();
                response.put("type", "GAME_STARTED");
                response.put("gameId", gameId);
                response.put("currentPlayer", gameState.getCurrentPlayerName());
                response.put("topCard", gameState.getTopCard().toString());
                response.put("currentColor", gameState.getCurrentColor());
                response.put("players", gameState.getPlayerOrder());
                response.put("direction", gameState.isClockwise() ? 1 : -1);
                response.put("message", "Game started!");
                response.put("readyStates", gameRoom.getPlayerReadyStates());
                System.out.println("GAME STARTED");
                // Send game state to each player with their specific hand
                for (String player : gameState.getPlayerOrder()) {
                    Map<String, Object> playerResponse = new HashMap<>(response);
                    playerResponse.put("playerHand", gameState.getPlayerHand(player));
                    playerResponse.put("players", gameState.getPlayerOrder());
                    playerResponse.put("playerIndex", gameState.getPlayerOrder().indexOf(player));
                    // Calculate hand sizes for all players
                    Map<String, Integer> handSizes = new HashMap<>();
                    for (String p : gameState.getPlayerOrder()) {
                        handSizes.put(p, gameState.getPlayerHand(p).size());
                    }
                    playerResponse.put("handSizes", handSizes);
                    System.out.println("PLAYER ANSWER; PLAYERNAME: " + player);
                    System.out.println("PLAYER ANSWER; PLAYERORDER: " + gameState.getPlayerOrder());
                    System.out.println("PLAYER ANSWER; PLAYERNAME: " + gameState.getPlayerHand(player));
                    messagingTemplate.convertAndSendToUser(player, "/queue/gameState", playerResponse);
                }

                // Also broadcast to room
                System.out.println("TOPIC MSSG");
                messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("message", "Failed to start game: " + e.getMessage());
            System.out.println("ERR START GAME: " + e.getMessage());
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
        }
    }

    /**
     * Private Methode für die Klasse, um die Handgrößen der Spieler zu ermitteln
     * @param gameRoom
     * @return
     */
    private Map<String, Integer> getHandSizes(GameRoomService.GameRoom gameRoom) {
        Map<String, Integer> handSizes = new HashMap<>();
        for (String player : gameRoom.getPlayers()) {
            handSizes.put(player, gameRoom.getPlayerHandSize(player));
        }
        return handSizes;
    }

    /**
     * Methode fürs WS, die das Joinen eines Spielers handelt
     * @param joinMessage
     */
    // WebSocket Message Handlers for real-time gameplay
    @MessageMapping("/join") // e.g., /app/join - for WebSocket subscription
    public void joinGameWebSocket(Map<String, String> joinMessage) {
        System.out.println("Received join message");
        String gameId = joinMessage.get("gameId");
        String playerName = joinMessage.get("player");
        
        try {
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(gameId);
            
            if (gameRoom != null) {
                // Notify all players in the room about the WebSocket connection
                Map<String, Object> response = new HashMap<>();
                response.put("type", "PLAYER_CONNECTED");
                response.put("player", playerName);
                response.put("gameId", gameId);
                response.put("message", playerName + " connected to game");
                System.out.println("PLAYER_CONNECTED: " + playerName);
                messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
            }
        } catch (Exception e) {
            // Send error message back to the player
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("message", "Failed to join game via WebSocket: " + e.getMessage());
            System.out.println("ERR JOIN GAME: " + e.getMessage());
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
        }
    }

    /**
     * Methode fürs WS, die den Spielstatus abfragt und sendet
     * @param message
     */
    @MessageMapping("/getGameState")
    @SendToUser("/queue/gameState")
    public void getGameState(Map<String, String> message) {
        System.out.println("Received Game state message");
        String gameId = message.get("gameId");
        String playerName = message.get("player");
        System.out.println("GameID: " + gameId + ", playerName: " + playerName);
        GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(gameId);
        GameStateService.MultiplayerGameState gameState = gameStateService.getGame(gameId);
        if (gameRoom != null && gameState != null && gameRoom.isGameStarted()) {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "GAME_STATE");
            response.put("gameId", gameId);
            response.put("currentPlayer", gameState.getCurrentPlayerName());
            response.put("topCard", gameState.getTopCard().toString());
            response.put("currentColor", gameState.getCurrentColor());
            response.put("players", gameState.getPlayerOrder());
            response.put("direction", gameState.isClockwise() ? 1 : -1);
            response.put("message", "Current game state");
            response.put("readyStates", gameRoom.getPlayerReadyStates());
            // Per-user fields
            response.put("playerHand", gameState.getPlayerHand(playerName));
            response.put("playerIndex", gameState.getPlayerOrder().indexOf(playerName));
            Map<String, Integer> handSizes = new HashMap<>();
            System.out.println("GAME STATE CREATED");
            for (String p : gameState.getPlayerOrder()) {
                handSizes.put(p, gameState.getPlayerHand(p).size());
            }
            response.put("handSizes", handSizes);
            messagingTemplate.convertAndSendToUser(playerName, "/queue/gameState", response);
        } else if (gameRoom != null && !gameRoom.isGameStarted()) {
            System.out.println("GAME STATE CREATED; BUT GAME DIDNT START YET");
            Map<String, Object> response = new HashMap<>();
            response.put("type", "GAME_STATE");
            response.put("gameId", gameId);
            response.put("player", playerName);
            response.put("message", "Current game state");
            response.put("readyStates", gameRoom.getPlayerReadyStates());
            messagingTemplate.convertAndSendToUser(playerName, "/queue/gameState", response);
        }
    }
} 