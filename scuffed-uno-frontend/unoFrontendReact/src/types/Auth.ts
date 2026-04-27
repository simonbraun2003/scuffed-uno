/**
 * Request-Interface für Login an Backend-API.
 */
export interface LoginRequest {
    username: string;
    password: string;
}

/**
 * Request-Interface für Registrierung (erweitert LoginRequest).
 */
export interface RegisterRequest extends LoginRequest {
    email: string;
}

/**
 * Response-Interface von Auth-Endpunkten.
 */
export interface AuthResponse {
    token: string;
    userId: string;
}

/**
 * Type für AuthContext (State und Funktionen).
 */
export interface AuthContextType {
    isAuthenticated: boolean;
    login: (token: string, username: string) => void;
    logout: () => void;
}