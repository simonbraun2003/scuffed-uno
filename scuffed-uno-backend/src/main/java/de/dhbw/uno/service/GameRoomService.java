package de.dhbw.uno.service;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

@Service
public class GameRoomService {

    // Verwaltet alle aktiven Spielräume
    private Map<String, GameRoom> gameRooms = new HashMap<>();
    // Schützt Änderungen an der Room-Liste vor Race Conditions
    private final Lock lock = new ReentrantLock();

    public String createGameRoom(String creatorId) {
        lock.lock();
        try {
            // Erzeugt eine kurze, eindeutige Room-ID
            String roomId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            gameRooms.put(roomId, new GameRoom(roomId, creatorId));
            return roomId;
        } finally {
            lock.unlock();
        }
    }

    public GameRoom getGameRoom(String roomId) {
        return gameRooms.get(roomId);
    }

    public void addPlayerToRoom(String roomId, String playerId) {
        lock.lock();
        try {
            GameRoom room = gameRooms.get(roomId);
            // Spieler nur hinzufügen, wenn Raum existiert und noch Platz ist
            if (room != null && room.players.size() < 4) {
                room.addPlayer(playerId);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean removePlayerFromRoom(String roomId, String playerId) {
        lock.lock();
        try {
            GameRoom room = gameRooms.get(roomId);
            if (room != null) {
                boolean removed = room.removePlayer(playerId);
                // Leeren Raum automatisch löschen
                if (room.players.isEmpty()) {
                    gameRooms.remove(roomId);
                }
                return removed;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    // Repräsentiert einen einzelnen UNO-Spielraum mit komplettem Spielzustand
    public static class GameRoom {
        private String roomId;
        private String creatorId;
        private List<String> players = new ArrayList<>();
        private Map<String, List<Card>> playerHands = new HashMap<>();
        private List<Card> deck = new ArrayList<>();
        private List<Card> discardPile = new ArrayList<>();
        private int currentPlayerIndex = 0;
        private String currentColor;
        private boolean clockwise = true;
        private int drawStack = 0;
        private boolean gameStarted = false;
        private Map<String, Boolean> playerReady = new HashMap<>();

        public GameRoom(String roomId, String creatorId) {
            this.roomId = roomId;
            this.creatorId = creatorId;
            initializeDeck();
        }

        public void addPlayer(String playerId) {
            // Maximal 4 Spieler, jeder Spieler nur einmal
            if (players.size() < 4 && !players.contains(playerId)) {
                players.add(playerId);
                playerHands.put(playerId, new ArrayList<>());
                playerReady.put(playerId, false);
            }
        }

        public boolean removePlayer(String playerId) {
            playerHands.remove(playerId);
            playerReady.remove(playerId);
            return players.remove(playerId);
        }

        public void setPlayerReady(String playerId, boolean ready) {
            if (playerReady.containsKey(playerId)) {
                playerReady.put(playerId, ready);
            }
        }

        public boolean allPlayersReady() {
            return !playerReady.isEmpty() && playerReady.values().stream().allMatch(Boolean::booleanValue);
        }

        public Map<String, Boolean> getPlayerReadyStates() {
            return new HashMap<>(playerReady);
        }

        public void startGame(String playerId) {
            // Nur der Ersteller darf das Spiel starten
            if (!playerId.equals(creatorId)) {
                throw new IllegalStateException("Only the room creator can start the game.");
            }
            // Spielstart nur bei mindestens 2 Spielern
            if (players.size() >= 2 && !gameStarted) {
                gameStarted = true;
                shuffleDeck();

                // Jeder Spieler bekommt 7 Karten
                for (String player : players) {
                    List<Card> hand = new ArrayList<>();
                    for (int i = 0; i < 7; i++) {
                        hand.add(drawCard());
                    }
                    playerHands.put(player, hand);
                }

                // Erste Karte darf kein Wild Draw Four sein
                Card firstCard;
                do {
                    firstCard = drawCard();
                } while (firstCard.getValue().equals("Wild Draw Four"));

                discardPile.add(firstCard);
                currentColor = firstCard.getColor();
            }
        }

        public boolean playCard(String playerId, String cardValue, String cardColor, String chosenColor) {
            // Nur der aktuelle Spieler darf ausspielen
            if (!gameStarted || !players.get(currentPlayerIndex).equals(playerId)) {
                return false;
            }

            List<Card> playerHand = playerHands.get(playerId);
            Card cardToPlay = null;

            // Gesuchte Karte in der Hand finden
            for (Card card : playerHand) {
                if (card.getValue().equals(cardValue) && card.getColor().equals(cardColor)) {
                    cardToPlay = card;
                    break;
                }
            }

            // Karte muss vorhanden und regelkonform sein
            if (cardToPlay == null || !isValidPlay(cardToPlay)) {
                return false;
            }

            playerHand.remove(cardToPlay);
            discardPile.add(cardToPlay);

            // Spezialeffekte der Karte anwenden
            handleSpecialCard(cardToPlay, chosenColor);

            // Nächsten Spieler aktivieren
            moveToNextPlayer();

            return true;
        }

        public boolean isValidPlay(Card card) {
            if (discardPile.isEmpty()) return true;
            Card topCard = discardPile.get(discardPile.size() - 1);

            // Bei Ziehstapel nur gleiche Ziehkarte erlauben
            if (drawStack > 0) {
                if (topCard.getValue().equals("Draw Two")) {
                    return card.getValue().equals("Draw Two");
                }
                if (topCard.getValue().equals("Wild Draw Four")) {
                    return card.getValue().equals("Wild Draw Four");
                }
                return false;
            }

            // Standardregel: gleiche Farbe, gleicher Wert oder Wild
            return card.getValue().equals("Wild") ||
                    card.getValue().equals("Wild Draw Four") ||
                    card.getColor().equals(currentColor) ||
                    card.getValue().equals(topCard.getValue());
        }

        private void handleSpecialCard(Card card, String chosenColor) {
            String value = card.getValue();

            switch (value) {
                case "Skip":
                    // Nächsten Spieler überspringen
                    moveToNextPlayer();
                    break;
                case "Reverse":
                    // Spielrichtung umkehren
                    clockwise = !clockwise;
                    if (players.size() == 2) {
                        moveToNextPlayer();
                    }
                    break;
                case "Draw Two":
                    // Ziehstapel erhöhen
                    drawStack += 2;
                    break;
                case "Wild":
                    // Neue Farbe setzen
                    currentColor = chosenColor;
                    break;
                case "Wild Draw Four":
                    // Neue Farbe setzen und 4 Karten ziehen lassen
                    currentColor = chosenColor;
                    drawStack = 4;
                    break;
                default:
                    currentColor = card.getColor();
            }
        }

        private void moveToNextPlayer() {
            // Index abhängig von Spielrichtung weiterdrehen
            if (clockwise) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            } else {
                currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
            }
        }

        private Card drawCard() {
            // Wenn Deck leer ist, Ablagestapel neu mischen
            if (deck.isEmpty()) {
                if (discardPile.size() > 1) {
                    Card topCard = discardPile.remove(discardPile.size() - 1);
                    deck.addAll(discardPile);
                    discardPile.clear();
                    discardPile.add(topCard);
                    shuffleDeck();
                }
            }
            return deck.isEmpty() ? null : deck.remove(0);
        }

        private void shuffleDeck() {
            Collections.shuffle(deck);
        }

        private void initializeDeck() {
            String[] colors = {"red", "yellow", "green", "blue"};
            String[] values = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "Skip", "Reverse", "Draw Two"};

            // Standardkarten ins Deck legen
            for (String color : colors) {
                for (String value : values) {
                    deck.add(new Card(value, color));
                    if (!value.equals("0")) {
                        deck.add(new Card(value, color));
                    }
                }
            }

            // Wild- und Wild-Draw-Four-Karten hinzufügen
            for (int i = 0; i < 4; i++) {
                deck.add(new Card("Wild", "black"));
                deck.add(new Card("Wild Draw Four", "black"));
            }

            shuffleDeck();
        }

        // Getter für Spielzustand
        public String getRoomId() { return roomId; }
        public String getCreatorId() { return creatorId; }
        public List<String> getPlayers() { return new ArrayList<>(players); }
        public String getCurrentPlayer() {
            return players.isEmpty() ? null : players.get(currentPlayerIndex);
        }
        public Card getTopCard() {
            return discardPile.isEmpty() ? null : discardPile.get(discardPile.size() - 1);
        }
        public String getCurrentColor() { return currentColor; }
        public boolean isClockwise() { return clockwise; }
        public int getDrawStack() { return drawStack; }
        public boolean isGameStarted() { return gameStarted; }

        public int getPlayerHandSize(String playerId) {
            List<Card> hand = playerHands.get(playerId);
            return hand != null ? hand.size() : 0;
        }

        public List<Card> getPlayerHand(String playerId) {
            return playerHands.getOrDefault(playerId, new ArrayList<>());
        }
    }

    // Einfache Kartenklasse für UNO-Karten
    public static class Card {
        private String value;
        private String color;

        public Card(String value, String color) {
            this.value = value;
            this.color = color;
        }

        public String getValue() { return value; }
        public String getColor() { return color; }

        @Override
        public String toString() {
            return color + "_" + value;
        }
    }
}