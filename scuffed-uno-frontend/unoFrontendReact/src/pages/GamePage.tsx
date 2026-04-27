import type {Card} from "../types/Game.ts";
import {useNavigate} from "react-router-dom";
import {useWebSocket} from "../context/WebSocketContext.tsx";
import {useEffect, useState} from "react";
import {CardLayout} from "../components/CardLayout.tsx";
import {Card as MuiCard, CardActionArea, CardContent, Grid, Typography} from "@mui/material";
import {CustomButton} from "../components/CustomButton.tsx";

/**
 * Hauptspiel-Seite für Uno.
 * Zeigt Spielbrett, aktuelle Ablagestapel-Karte, Handkarten, andere Spieler und Steuerelemente.
 * Handhabt Kartenauswahl, Farbwahl für Wildcards, Play/Draw-Aktionen.
 */
export function GamePage() {
    const navigate = useNavigate();

    const {
        gameStarted,
        userGameState,
        topicGameState,
        getGameState,
        gameID,
        playCard,
        drawCard,
    } = useWebSocket();

    const [colorTxtfield, setColorTxtfield] = useState<string>("");
    const [currentCard, setCurrentCard] = useState<String | undefined>(userGameState?.topCard);
    const [currentColor, setCurrentColor] = useState<string | undefined>(userGameState?.currentColor);
    const [selectedCard, setSelectedCard] = useState<Card | null>(null);
    const [cardIsPlayed, setCardIsPlayed] = useState(false);

    /**
     * Redirect bei nicht gestartetem Spiel und initialer Game-State-Anfrage.
     */
    useEffect(() => {
        if (!gameStarted) {
            navigate("/room-menu");
        }
        getGameState(gameID, localStorage.getItem("username"));
    }, [gameStarted, navigate, gameID]);

    /**
     * Updated Ablagestapel-Karte und Farbe bei Änderungen im Game-State.
     */
    useEffect(() => {
        if(userGameState?.type !== "CARDS_DRAWN" && userGameState?.topCard !== currentCard) {
            setCurrentCard(userGameState?.topCard);
            setCurrentColor(userGameState?.currentColor);
            console.log(currentCard +" ; "+currentColor)
        }
    }, [userGameState, topicGameState]);

    /**
     * Logging für ausgewählte Karte (Debug).
     */
    useEffect(() => {
        console.log(selectedCard);
    }, [selectedCard]);

    /**
     * Reset selectedCard, falls Karte nicht mehr in Hand vorhanden (nach Play/Draw).
     */
    useEffect(() => {
        if (userGameState) {
            const cardStillExists = userGameState.playerHand.some(
                c => c.color === selectedCard?.color && c.value === selectedCard?.value
            );
            if (!cardStillExists && selectedCard) {
                setSelectedCard(null);
            }
        }
    }, [userGameState?.playerHand]);

    const player = localStorage.getItem("username") ?? "";

    // Nur weiter rendern, wenn gameStatus vollständig da ist
    if (!topicGameState || !userGameState) {
        return (
            <div className="game-state">
                <h1>Spielbrett</h1>
                <p>Spieler: <strong>{player}</strong></p>
                <p>Lade Spielstand…</p>
            </div>
        );
    }

    const isYourTurn = userGameState.currentPlayer === player;

    /**
     * Spielt ausgewählte Karte (prüft Existenz, Farbwahl bei black).
     * Sendet playCard-Request und reset States.
     */
    const handlePlayCard = () => {
        if (!selectedCard) {
            alert("Bitte zuerst eine Karte auswählen!");
            return;
        }
        setCardIsPlayed(true);

        const inputColor = selectedCard.color;
        const inputValue = selectedCard.value;

        // Prüfe ob diese spezifische Karte wirklich noch auf der Hand ist
        const cardIndex = userGameState.playerHand.findIndex(
            c => c.color === inputColor && c.value === inputValue
        );

        if (cardIndex === -1) {
            alert("Diese Karte ist nicht mehr auf der Hand.");
            setSelectedCard(null);
            return;
        }

        // Black cards brauchen Farbwahl
        if (inputColor === "black") {
            if(colorTxtfield === "") {
                alert("Aktionskarte benötigt eine Folgefarbe!");
                return;
            }
        }

        playCard({
            gameID,
            card: inputValue,
            color: inputColor,
            playerName: player,
            chosenColor: colorTxtfield,
        });

        // State zurücksetzen
        setSelectedCard(null);
        setColorTxtfield("");
        setCardIsPlayed(false);
    };

    /**
     * Zieht Karten (drawCount=null → Standard 1 Karte).
     */
    const handleDrawCard =() => {
        drawCard({gameID: gameID, playerName: player, drawCount: null});
    }

    return (
        <div
            style={{
                width: "100%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                flexDirection: "column",
                gap: "16px",
                padding: "16px",
            }}
        >
            <CardLayout width="80%">
                <Grid>
                    <Grid size={6}>
                        <p>Ich bin: {localStorage.getItem("username")}</p>
                        <h2>Spielstatus</h2>
                        <p>
                            <strong>Aktueller Spieler:</strong>{" "}
                            {topicGameState.currentPlayer}
                        </p>
                        <p>
                            <strong>Richtung:</strong>{" "}
                            {topicGameState.direction === 1
                                ? "Uhrzeigersinn"
                                : "Gegenuhrzeigersinn"}
                        </p>
                        {currentCard && (
                            <div style={{
                                display: "flex",
                                justifyContent: "center",
                                margin: "16px 0"
                            }}>
                                <MuiCard
                                    sx={{
                                        width: 120,
                                        height: 170,
                                        borderRadius: 3,
                                        border: "2px solid #333",
                                        backgroundColor:
                                            currentColor === "red"
                                                ? "#D72600"
                                                : currentColor === "green"
                                                    ? "#379711"
                                                    : currentColor === "blue"
                                                        ? "#0956BF"
                                                        : currentColor === "yellow"
                                                            ? "#ECD407"
                                                            : "#222",
                                        color: currentColor === "black" ? "#fff" : "#000",
                                        boxShadow: 6,
                                        cursor: "default",
                                        transition: "0.3s",
                                        "&:hover": {
                                            transform: "rotate(1deg) scale(1.02)"
                                        }
                                    }}
                                >
                                    <CardContent
                                        sx={{
                                            height: "100%",
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                            textAlign: "center",
                                            p: 1.5
                                        }}
                                    >
                                        <Typography
                                            variant="body1"
                                            sx={{
                                                fontWeight: 700,
                                                fontSize: "1.1rem",
                                                lineHeight: 1.2
                                            }}
                                        >
                                            {currentCard}
                                        </Typography>
                                    </CardContent>
                                </MuiCard>
                            </div>
                        )}
                    </Grid>

                    <Grid size={6}>
                        <section>
                            <h3>Deine Hand</h3>
                            {userGameState.playerHand.length > 0 ? (
                                <div style={{ display: "flex", gap: "12px", flexWrap: "wrap" }}>
                                    {userGameState.playerHand.map((card, idx) => {
                                        const isSelected =
                                            selectedCard?.color === card.color &&
                                            selectedCard?.value === card.value;

                                        return (
                                            <MuiCard
                                                key={idx}
                                                sx={{
                                                    width: 90,
                                                    height: 130,
                                                    borderRadius: 3,
                                                    border: isSelected ? "3px solid #1976d2" : "1px solid #ccc",
                                                    backgroundColor:
                                                        card.color === "red"
                                                            ? "#D72600"
                                                            : card.color === "green"
                                                                ? "#379711"
                                                                : card.color === "blue"
                                                                    ? "#0956BF"
                                                                    : card.color === "yellow"
                                                                        ? "#ECD407"
                                                                        : "#222",
                                                    color: card.color === "black" ? "#fff" : "#000",
                                                    cursor: "pointer",
                                                    boxShadow: isSelected ? 4 : 1,
                                                    transition: "0.2s",
                                                    "&:hover": { transform: "translateY(-2px)", boxShadow: 4 },
                                                }}
                                            >
                                                <CardActionArea
                                                    sx={{ width: "100%", height: "100%" }}
                                                    onClick={() => setSelectedCard(card)}
                                                >
                                                    <CardContent
                                                        sx={{
                                                            height: "100%",
                                                            display: "flex",
                                                            alignItems: "center",
                                                            justifyContent: "center",
                                                            textAlign: "center",
                                                        }}
                                                    >
                                                        <Typography variant="body2">{card.value}</Typography>
                                                    </CardContent>
                                                </CardActionArea>
                                            </MuiCard>
                                        );
                                    })}
                                </div>
                            ) : (
                                <p>Keine Karten auf der Hand</p>
                            )}
                        </section>

                        <section>
                            <h3>Alle Spieler</h3>
                            <ul>
                                {userGameState.players?.map((p) => (
                                    <li key={p}>
                                        {p} ({userGameState.handSizes?.[p] ?? "?"} Karten)
                                    </li>
                                ))}
                            </ul>
                        </section>
                    </Grid>
                </Grid>
                <Grid  style={{ marginTop: "24px" }}>
                    <Grid size={12}>
                        <h2>Spielerinteraktion</h2>
                    </Grid>
                    <Grid size={12}>
                        {isYourTurn ? (
                            <>
                                {selectedCard && (
                                    <div style={{ marginBottom: "8px", padding: "12px", backgroundColor: "#e3f2fd", borderRadius: 4 }}>
                                        <Typography variant="body1">
                                            <strong>Ausgewählt:</strong> {selectedCard.color} {selectedCard.value}
                                        </Typography>
                                    </div>
                                )}

                                {selectedCard?.color === "black" && (
                                    <div style={{ display: "flex", gap: "12px", justifyContent: "center", marginBottom: "16px" }}>
                                        {["red", "green", "blue", "yellow"].map((color) => {
                                            const isActive = colorTxtfield === color;
                                            const colorMap: Record<string, string> = {
                                                red: "#D72600",
                                                green: "#379711",
                                                blue: "#0956BF",
                                                yellow: "#ECD407"
                                            };
                                            const emojiMap: Record<string, string> = {
                                                red: "🔴",
                                                green: "🟢",
                                                blue: "🔵",
                                                yellow: "🟡"
                                            };

                                            return (
                                                <CustomButton
                                                    key={color}
                                                    onClick={() => setColorTxtfield(color)}
                                                    disabled={cardIsPlayed}
                                                    sx={{
                                                        minWidth: 80,
                                                        padding: "12px 8px",
                                                        borderRadius: 3,
                                                        transition: "all 0.2s",
                                                        backgroundColor: isActive
                                                            ? colorMap[color]
                                                            : cardIsPlayed
                                                                ? "#ccc"
                                                                : "#f5f5f5",
                                                        color: isActive
                                                            ? "white"
                                                            : cardIsPlayed
                                                                ? "#999"
                                                                : "#666",
                                                        boxShadow: isActive ? "0 4px 12px rgba(0,0,0,0.3)" : "none",
                                                        transform: isActive ? "scale(1.05)" : "scale(1)",
                                                        fontWeight: isActive ? 700 : 500,
                                                        "&:hover": {
                                                            backgroundColor: isActive
                                                                ? colorMap[color]
                                                                : cardIsPlayed
                                                                    ? "#ccc"
                                                                    : colorMap[color] + "20",
                                                            transform: isActive ? "scale(1.05)" : "scale(1.02)",
                                                        }
                                                    }}
                                                >
                                                    {emojiMap[color]} {color.charAt(0).toUpperCase() + color.slice(1)}
                                                </CustomButton>
                                            );
                                        })}
                                    </div>
                                )}

                                <CustomButton
                                    onClick={handlePlayCard}
                                    disabled={!selectedCard || (selectedCard.color === "black" && colorTxtfield === "") || cardIsPlayed}
                                >
                                    {cardIsPlayed ? "Wird gespielt..." : "Karte spielen"}
                                </CustomButton>
                                <CustomButton onClick={handleDrawCard} disabled={cardIsPlayed}>
                                    Karte(n) aufnehmen
                                </CustomButton>
                            </>
                        ) : (
                            <p>Warte auf Spieler <strong>{topicGameState.currentPlayer}</strong>.</p>
                        )}
                    </Grid>
                </Grid>
            </CardLayout>
        </div>
    );
}