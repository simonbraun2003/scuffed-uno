import {API_ENDPOINTS} from "../config/Api.ts";
import type {AuthResponse, LoginRequest, RegisterRequest} from "../types/Auth.ts";

/**
 * Verwaltet die Authentifizierung von Nutzern und die Kommunikation dieser mit dem Backend.
 * Enthält Methoden für Login und Registrierung des Nutzers auf der Webseite.
 */
export class AuthService {

    /**
     * Führt den Login eines Nutzers durch, indem die eingegebenen Nutzerdaten an das Backend gesendet werden.
     *
     * @param {LoginRequest} request - Definierter Typ der Eingabedaten bestehend aus 'username' und 'password'
     * @param {string} request.username - Der Nutzername des Users
     * @param {string} request.password - Das Passwort des Users
     * @param {Promise<AuthResponse>} - Asynchrone Antwort des Backends bestehend aus 'token' und 'userID'
     * @throws {Error} - Wirft eine Fehlermeldung, wenn der Login fehlschlägt (z.B. bei falschen Anmeldedaten oder Serverfehler)
     */
    static async handleLogin({username, password}: LoginRequest): Promise<AuthResponse> {
        try {
            const url = `${API_ENDPOINTS.login}?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`;

            const response = await fetch(url, {
                method: "POST",
                headers: {
                    Accept: "*/*",
                },
            });

            if (!response.ok) {
                throw new Error(`Login failed: ${response.status}`);
            }

            return await response.json();

        } catch (error) {
            console.error("Login error:", error);
            throw error;
        }

    }

    /**
     * Führt die Registrierung eines Nutzers durch, indem die eingegebenen Nutzerdaten an das Backend gesendet werden.
     *
     * @param {RegisterRequest} request - Definierter Typ der Eingabedaten bestehend aus 'username', 'password' und 'email'
     * @param {string} request.username - Der Nutzername des Users
     * @param {string} request.password - Das Passwort des Users
     * @param {string} request.email - Die E-Mail des Nutzers
     * @param {Promise<AuthResponse>} - Asynchrone Antwort des Backends bestehend aus 'token' und 'userID'
     * @throws {Error} - Wirft eine Fehlermeldung, wenn die Registrierung fehlschlägt (z.B. bei bereits existierenden Nutzerdaten oder Serverfehler)
     */
    static async handleRegister({username, password, email}: RegisterRequest): Promise<AuthResponse> {
        try {
            // Destructure RegisterRequest direkt im Parameter
            const url = `${API_ENDPOINTS.register}?username=${encodeURIComponent(username)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`;

            const response = await fetch(url, {
                method: "POST",
                headers: {
                    Accept: "*/*",
                },
            });

            if (!response.ok) {
                throw new Error(`Registration failed: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error("Registration error:", error);
            throw error;
        }
    }
}
