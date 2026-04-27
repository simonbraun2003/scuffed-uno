// types/Game.ts

/**
 * Uno-Karte mit Wert und Farbe.
 */
export interface Card {
    value: string;
    color: string;
}

/**
 * User-spezifischer Game-State (persönliche Hand, Handgrößen anderer).
 */
export interface UserGameState {
    gameID: string;
    topCard: string;
    currentPlayer: string;
    players: string[];
    playerHand: Card[];
    handSizes: Record<string, number>;
    currentColor: string;
    type: "GAME_STATE" | "CARD_PLAYED" | "CARDS_DRAWN";
    message: string;
    readyStates: Record<string, boolean>;
    playerIndex: number;
    direction: number;
}

/**
 * Globaler Topic-Game-State (für alle sichtbar, ohne persönliche Hand).
 */
export interface TopicGameState {
    gameId: string;
    topCard: string;
    currentPlayer: string;
    players: string[];
    currentColor: string;
    direction: number;
    message: string;
    readyStates: Record<string, boolean>;
    type: "GAME_STARTED" | "CARD_PLAYED";
}

/**
 * Request zum Spielen einer Karte über WebSocket.
 */
export interface PlayCardRequest{
    gameID: string;
    card: string;
    color: string;
    playerName: string | null;
    chosenColor: string;
}

/**
 * Request zum Ziehen von Karten über WebSocket.
 */
export interface DrawCardRequest{
    gameID: string;
    playerName: string;
    drawCount: number | null;
}