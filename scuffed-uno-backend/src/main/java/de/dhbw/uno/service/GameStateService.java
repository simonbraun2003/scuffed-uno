package de.dhbw.uno.service;

import de.dhbw.uno.dto.CardDataDTO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameStateService {

    // Spielzustand für einen einzelnen Raum
    public static class MultiplayerGameState {
        private List<CardDataDTO> deck; // Nachziehstapel
        private List<CardDataDTO> discardPile; // Ablagestapel
        private Map<String, List<CardDataDTO>> playerHands; // Spielername -> Handkarten
        private List<String> playerOrder; // Reihenfolge der Spieler
        private int currentPlayerIndex; // Aktiver Spieler
        private boolean clockwise; // Spielrichtung
        private String currentColor; // Aktuelle Farbe
        private int drawStack; // Aufgestaute Ziehkarten
        private Random random;
        private int direction = 1;
        private boolean wildDrawFourChallenge = false;
        private String challengingPlayer = null;
        private String challengedPlayer = null;
        private boolean gameStarted = false;

        public MultiplayerGameState() {
            this.deck = new ArrayList<>();
            this.discardPile = new ArrayList<>();
            this.playerHands = new HashMap<>();
            this.playerOrder = new ArrayList<>();
            this.clockwise = true;
            this.drawStack = 0;
            this.random = new Random();
            initializeDeck();
        }

        // Baut den UNO-Kartenstapel auf
        private void initializeDeck() {
            String[] colors = {"red", "yellow", "green", "blue"};
            String[] values = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "Skip", "Reverse", "Draw Two"};

            for (String color : colors) {
                for (String value : values) {
                    deck.add(new CardDataDTO(value, color));
                    if (!value.equals("0")) {
                        deck.add(new CardDataDTO(value, color));
                    }
                }
            }

            for (int i = 0; i < 4; i++) {
                deck.add(new CardDataDTO("Wild", "black"));
                deck.add(new CardDataDTO("Wild Draw Four", "black"));
            }
        }

        // Startet das Spiel und verteilt Karten
        public void startGame(List<String> players) {
            if (players.size() < 2) {
                throw new IllegalStateException("At least 2 players required");
            }

            this.playerOrder = new ArrayList<>(players);
            Collections.shuffle(playerOrder);
            shuffleDeck();

            for (String player : playerOrder) {
                List<CardDataDTO> hand = new ArrayList<>();
                for (int j = 0; j < 7; j++) {
                    hand.add(drawCard());
                }
                playerHands.put(player, hand);
            }

            // Erste Karte darf keine Wild-Karte sein
            CardDataDTO firstCard = drawCard();
            while (firstCard.getValue().equals("Wild") || firstCard.getValue().equals("Wild Draw Four")) {
                deck.add(firstCard);
                shuffleDeck();
                firstCard = drawCard();
            }
            discardPile.add(firstCard);
            currentColor = firstCard.getColor();
            currentPlayerIndex = 0;
            gameStarted = true;
        }

        private void shuffleDeck() {
            Collections.shuffle(deck, random);
        }

        // Zieht eine Karte vom Stapel
        public CardDataDTO drawCard() {
            if (deck.isEmpty()) {
                if (discardPile.size() > 1) {
                    CardDataDTO topCard = discardPile.remove(discardPile.size() - 1);
                    deck.addAll(discardPile);
                    discardPile.clear();
                    discardPile.add(topCard);
                    shuffleDeck();
                }
            }
            return deck.isEmpty() ? null : deck.remove(0);
        }

        // Prüft, ob eine Karte gespielt werden darf
        public boolean canPlayCard(CardDataDTO card, String playerName) {
            CardDataDTO topCard = getTopCard();

            if (drawStack > 0) {
                if (topCard.getValue().equals("Draw Two")) {
                    return card.getValue().equals("Draw Two");
                }
                if (topCard.getValue().equals("Wild Draw Four")) {
                    return card.getValue().equals("Wild Draw Four");
                }
                return false;
            }

            if (card.getValue().equals("Wild Draw Four")) {
                List<CardDataDTO> playerHand = playerHands.get(playerName);
                if (playerHand != null) {
                    for (CardDataDTO handCard : playerHand) {
                        if (handCard != card && handCard.getColor().equals(currentColor)) {
                            return false;
                        }
                    }
                }
                return true;
            }

            if (card.getValue().equals("Wild")) {
                return true;
            }

            return card.getColor().equals(currentColor) || card.getValue().equals(topCard.getValue());
        }

        // Spielt eine Karte und verarbeitet Spezialeffekte
        public void playCard(String playerName, CardDataDTO card, String chosenColor) {
            if (!getCurrentPlayerName().equals(playerName)) {
                throw new IllegalStateException("Not your turn");
            }

            if (!canPlayCard(card, playerName)) {
                throw new IllegalStateException("Invalid card play");
            }

            List<CardDataDTO> playerHand = playerHands.get(playerName);
            playerHand.removeIf(c -> c.getValue().equals(card.getValue()) && c.getColor().equals(card.getColor()));
            discardPile.add(card);

            if (card.getValue().equals("Wild") || card.getValue().equals("Wild Draw Four")) {
                currentColor = chosenColor;
            } else {
                currentColor = card.getColor();
            }

            switch (card.getValue()) {
                case "Skip":
                    moveToNextPlayer();
                    moveToNextPlayer();
                    break;
                case "Reverse":
                    direction *= -1;
                    clockwise = direction == 1;
                    if (playerOrder.size() == 2) {
                        moveToNextPlayer();
                    }
                    moveToNextPlayer();
                    break;
                case "Draw Two":
                    drawStack += 2;
                    moveToNextPlayer();
                    break;
                case "Wild Draw Four":
                    drawStack = 4;
                    wildDrawFourChallenge = true;
                    challengedPlayer = playerName;
                    moveToNextPlayer();
                    break;
                default:
                    moveToNextPlayer();
                    break;
            }
        }

        // Gibt einem Spieler Karten
        public void drawCards(String playerName, int count) {
            List<CardDataDTO> playerHand = playerHands.get(playerName);
            if (playerHand == null) return;

            for (int i = 0; i < count; i++) {
                CardDataDTO card = drawCard();
                if (card != null) {
                    playerHand.add(card);
                }
            }
        }

        // Behandelt erzwungenes Ziehen
        public void handleForcedDraw(String playerName) {
            if (drawStack > 0) {
                drawCards(playerName, drawStack);
                drawStack = 0;
                moveToNextPlayer();
            }
        }

        // Wechselt zum nächsten Spieler
        public void moveToNextPlayer() {
            currentPlayerIndex = (currentPlayerIndex + direction + playerOrder.size()) % playerOrder.size();
        }

        public String getCurrentPlayerName() {
            if (currentPlayerIndex >= 0 && currentPlayerIndex < playerOrder.size()) {
                return playerOrder.get(currentPlayerIndex);
            }
            return null;
        }

        public CardDataDTO getTopCard() {
            return discardPile.isEmpty() ? null : discardPile.get(discardPile.size() - 1);
        }

        public boolean isGameOver() {
            return playerHands.values().stream().anyMatch(List::isEmpty);
        }

        public String getWinner() {
            for (Map.Entry<String, List<CardDataDTO>> entry : playerHands.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    return entry.getKey();
                }
            }
            return null;
        }

        public String getCurrentColor() { return currentColor; }
        public int getCurrentPlayerIndex() { return currentPlayerIndex; }
        public List<CardDataDTO> getPlayerHand(String playerName) { return playerHands.get(playerName); }
        public boolean isClockwise() { return direction == 1; }
        public int getDrawStack() { return drawStack; }
        public boolean isGameStarted() { return gameStarted; }
        public List<String> getPlayerOrder() { return playerOrder; }
        public Map<String, List<CardDataDTO>> getAllPlayerHands() { return playerHands; }

        public void setCurrentColor(String color) { this.currentColor = color; }
        public void setDrawStack(int amount) { this.drawStack = amount; }
    }

    // Verwaltet alle Spielräume
    private final Map<String, MultiplayerGameState> gameStates = new HashMap<>();

    public MultiplayerGameState createGame(String roomId) {
        MultiplayerGameState gameState = new MultiplayerGameState();
        gameStates.put(roomId, gameState);
        return gameState;
    }

    public MultiplayerGameState getGame(String roomId) {
        return gameStates.get(roomId);
    }

    public void removeGame(String roomId) {
        gameStates.remove(roomId);
    }

    public boolean gameExists(String roomId) {
        return gameStates.containsKey(roomId);
    }
}