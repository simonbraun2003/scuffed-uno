import logo from "../assets/scuffed-dhbw-uno-logo.png";
import { Divider, Grid } from "@mui/material";
import { CardLayout } from "../components/CardLayout.tsx";
import { CustomButton } from "../components/CustomButton.tsx";
import {useNavigate} from "react-router-dom";

/**
 * UserMenu-Komponente – Hauptmenü des UNO-Spiels.
 * Angezeigt werden das Spiel-Logo und Optionen zum Starten des Spiels oder öffnen der Bestenliste an.
 * Verwendet `CardLayout` für die Struktur und `CustomButton` für interaktive Elemente.
 */
export function UserMenu() {
    const navigate = useNavigate();

    const handlePlayGame = () => {
        navigate("/room-menu");
    }

    return (
        <>
            {/* Haupt-Container: Zentriert alle Inhalte vertikal und horizontal */}
            <div
                className="game-menu"
                style={{
                    textAlign: "center",
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    flexDirection: "column",
                    minHeight: "100vh", // Füllt die gesamte Viewport-Höhe
                    gap: "20px", // Abstand zwischen Logo und Buttons
                }}
            >
                {/* Spiel-Logo mit fester Breite und automatischer Höhe */}
                <img
                    src={logo}
                    alt="UNO-Spiel-Logo" // Barrierefreiheit: Beschreibt das Bild für Screenreader
                    style={{ width: "300px", height: "auto" }}
                />

                {/* Karten-Layout für die Menü-Buttons (20% der Elternbreite) */}
                <CardLayout width="20%">
                    {/* Button zum Spielstart (vollständige Grid-Breite) */}
                    <Grid size={12}>
                        <CustomButton onClick={handlePlayGame}>Play Game</CustomButton>
                    </Grid>

                    {/* Trennlinie und Leaderboard-Button (vollständige Grid-Breite) */}
                    <Grid size={12}>
                        <Divider sx={{
                            marginBottom: '15px',
                            backgroundColor: 'red', // UNO-Farbe für Konsistenz
                            height: '5%',
                            width: '100%',
                        }} />
                        <CustomButton>Leaderboard</CustomButton>
                    </Grid>
                </CardLayout>
            </div>
        </>
    );
}
