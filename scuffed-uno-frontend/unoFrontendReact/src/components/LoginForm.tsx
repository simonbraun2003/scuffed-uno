import { useState } from "react";
import { Divider, Grid, TextField, Alert, CircularProgress } from "@mui/material";
import { AuthService } from "../services/AuthService.ts";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.tsx";
import { CardLayout } from "./CardLayout.tsx";
import { CustomButton } from "./CustomButton.tsx";

/**
 * Login‑Formular‑Komponente für das UNO‑Frontend.
 * Ermöglicht das Einloggen und Registrieren eines Nutzers über AuthService.
 * Beim Erfolg wird der Nutzer in den AuthContext eingeloggt und zur /game-menu‑Seite navigiert.
 */
export function LoginForm() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    /**
     * Führt einen Login‑Vorgang durch.
     * Schaltet isLoading ein, ruft AuthService.handleLogin auf und speichert Token und Username im AuthContext.
     * Bei Erfolg wird zur /game-menu‑Seite navigiert; bei Fehler wird eine Fehlermeldung angezeigt.
     */
    const handleLogin = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await AuthService.handleLogin({ username, password });
            login(data.token, username);
            navigate("/game-menu");
        } catch (error: unknown) {
            console.error("Login failed:", error);
            setError("Login fehlgeschlagen. Bitte überprüfe deine Angaben.");
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Führt einen Registrier‑Vorgang durch.
     * Nutzt denselben Username als Basis für eine Dummy‑E‑Mail (username@mail.de) und registriert den Nutzer.
     * Bei Erfolg wird ebenfalls eingeloggt und zur /game-menu‑Seite navigiert; bei Fehler eine Fehlermeldung angezeigt.
     */
    const handleRegister = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await AuthService.handleRegister({
                username,
                password,
                email: `${username}@mail.de`,
            });
            login(data.token, username);
            navigate("/game-menu");
        } catch (error: unknown) {
            console.error("Registration failed:", error);
            setError("Registrierung fehlgeschlagen. Bitte versuche es erneut.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <CardLayout width="20%">
            {/* Fehler‑Alert, falls Login/Registrierung fehlschlägt */}
            {error && (
                <Alert severity="error" sx={{ width: "100%", mb: 2 }}>
                    {error}
                </Alert>
            )}

            {/* Username‑Eingabefeld */}
            <Grid size={12}>
                <TextField
                    fullWidth
                    label="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
            </Grid>

            {/* Password‑Eingabefeld (sichtbar ausgeblendet wegen type="password") */}
            <Grid size={12}>
                <TextField
                    fullWidth
                    label="Password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
            </Grid>

            {/* Login‑Button mit Lade‑Animation */}
            <Grid size={12}>
                <CustomButton onClick={handleLogin} disabled={isLoading}>
                    {isLoading ? <CircularProgress size={24} /> : "Login"}
                </CustomButton>
            </Grid>

            {/* Trennlinie und Registrier‑Button */}
            <Grid size={12}>
                <Divider
                    sx={{
                        marginBottom: "15px",
                        backgroundColor: "red",
                        height: "5%",
                        width: "100%",
                    }}
                />
                <CustomButton onClick={handleRegister} disabled={isLoading}>
                    {isLoading ? <CircularProgress size={24} /> : "Register"}
                </CustomButton>
            </Grid>
        </CardLayout>
    );
}