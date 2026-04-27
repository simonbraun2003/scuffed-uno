/**
 * Response bei Raum-Erstellung (vom Backend).
 */
export interface CreateRoomRespone {
    roomId: string;
    players: Array<{}>;
    playerCount: number;
}

/**
 * Request zum Beitreten eines Raums.
 */
export interface JoinRoomRequest {
    roomID: string;
}

/**
 * Response bei Raum-Beitritt (vom Backend).
 */
export interface JoinRoomRespone {
    players: Array<{}>;
    playerCount: number;
}