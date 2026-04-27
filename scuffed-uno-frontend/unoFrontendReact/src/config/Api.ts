/**
 * Basis‑URL des UNO‑Backend‑Servers.
 */
// export const API_BASE_URL = "http://88.151.194.71:8096";
export const API_BASE_URL = "https://unoserver.etiennebader.de";

/**
 * Objekt, das alle wichtigen Backend‑Endpoints des UNO‑Spiels kapselt.
 * Die URLs werden aus API_BASE_URL und den jeweiligen Pfaden zusammengesetzt.
 * Wird von Diensten wie AuthService oder GameService verwendet, um Anfragen an das Backend zu senden.
 */
export const API_ENDPOINTS = {
    /**
     * POST‑Endpoint für Login‑Anfragen.
     */
    login: `${API_BASE_URL}/auth/login`,

    /**
     * POST‑Endpoint für Registrierungs‑Anfragen.
     */
    register: `${API_BASE_URL}/auth/register`,

    /**
     * POST‑Endpoint zum Erstellen eines neuen Spielraums.
     */
    createRoom: `${API_BASE_URL}/api/game/create-room`,

    /**
     * POST‑Endpoint zum Beitreten eines existierenden Spielraums.
     */
    joinRoom: `${API_BASE_URL}/api/game/join-room`,

    /**
     * POST‑Endpoint zum Aus‑Spielen einer Karte während einer Runde.
     */
    playCard: `${API_BASE_URL}/api/game/app/play-card`,
};