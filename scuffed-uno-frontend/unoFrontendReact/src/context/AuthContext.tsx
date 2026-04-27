import { createContext, useContext, useState } from "react";
import type { AuthContextType } from "../types/Auth.ts";

/**
 * React Context für die Authentifizierung des Users.
 * Verwaltet den Login-Status und stellt `login`/`logout`-Funktionen bereit.
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Provider-Komponente, die den Authentifizierungsstatus und -funktionen für alle Child-Komponenten bereitstellt.
 *
 * @param {Object} props - Komponenten-Props.
 * @param {React.ReactNode} props.children - Child-Komponenten, die auf den Auth-Context zugreifen.
 */
export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    /**
     * Speichert das Auth-Token im localStorage und setzt den Auth-Status auf `true`.
     *
     * @param {string} token - Das Auth-Token, das nach erfolgreicher Anmeldung vom Backend zurückgegeben wird.
     * @param {string} username - Der Nutzername, der für die Anmeldung benötigt wird
     */
    const login = (token: string, username: string) => {
        localStorage.setItem("token", token);
        localStorage.setItem("username", username);
        setIsAuthenticated(true);
    };

    /**
     * Entfernt das Auth-Token aus dem localStorage und setzt den Auth-Status auf `false`.
     */
    const logout = () => {
        localStorage.removeItem("token");
        setIsAuthenticated(false);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

/**
 * Custom Hook, um auf den Auth-Context zuzugreifen.
 *
 * @returns {AuthContextType} - Der Auth-Context mit `isAuthenticated`, `login` und `logout`.
 * @throws {Error} - Falls der Hook außerhalb eines `AuthProvider` aufgerufen wird.
 */
export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) throw new Error("useAuth must be used within an AuthProvider");
    return context;
}
