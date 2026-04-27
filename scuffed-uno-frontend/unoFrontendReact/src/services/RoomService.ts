import {API_ENDPOINTS} from "../config/Api.ts";
import type {CreateRoomRespone, JoinRoomRequest, JoinRoomRespone} from "../types/Room.ts";

/**
 * Service-Klasse für Uno-Raum-Operationen (Create/Join).
 * Führt authentifizierte POST-Requests an Backend durch und handled Errors.
 */
export class RoomService {
    /**
     * Erstellt neuen Uno-Raum über Backend-API.
     * Sendet playerName im Body, erwartet roomId und playerCount zurück.
     *
     * @returns {Promise<CreateRoomRespone>} Response mit roomId, playerCount.
     * @throws {Error} Bei HTTP-Fehlern oder Netzwerkproblemen.
     */
    static async handleCreateRoom() : Promise<CreateRoomRespone> {
        try {
            const url = `${API_ENDPOINTS.createRoom}`;

            const response = await fetch(url, {
                method: "POST",
                headers: {
                    Accept: "*/*",
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                },
                body: JSON.stringify({
                    playerName: localStorage.getItem("username"),
                })
            });

            if (!response.ok) {
                throw new Error(`Create Room failed: ${response.status}`);
            }

            return await response.json();

        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    /**
     * Joined existierenden Uno-Raum über Backend-API.
     * Sendet roomId und playerName, erwartet playerCount zurück.
     *
     * @param {JoinRoomRequest} request - Request mit roomID.
     * @returns {Promise<JoinRoomRespone>} Response mit playerCount.
     * @throws {Error} Bei HTTP-Fehlern oder Netzwerkproblemen.
     */
    static async handleJoinRoom({roomID} : JoinRoomRequest) : Promise<JoinRoomRespone> {
        try {
            const url = `${API_ENDPOINTS.joinRoom}`;

            const response = await fetch(url, {
                method: "POST",
                headers: {
                    Accept: "*/*",
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                },
                body: JSON.stringify({
                    roomId: roomID,
                    playerName: localStorage.getItem("username"),
                })
            });

            if (!response.ok) {
                throw new Error(`Joining Room failed: ${response.status}`);
            }

            return await response.json();

        } catch (error) {
            console.error(error);
            throw error;
        }
    }
}