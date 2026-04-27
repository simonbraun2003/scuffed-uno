import {CardLayout} from "../components/CardLayout.tsx";
import {Alert, CircularProgress, Divider, Grid, TextField} from "@mui/material";
import logo from "../assets/scuffed-dhbw-uno-logo.png";
import {CustomButton} from "../components/CustomButton.tsx";
import {useEffect, useState} from "react";
import {RoomService} from "../services/RoomService.ts";
import {useNavigate} from "react-router-dom";
import {useWebSocket} from "../context/WebSocketContext.tsx";

/**
 * Raum-Menü für Uno-Raum-Erstellung, -Beitritt und Spielstart.
 * Handhabt Create/Join-Raum-Logik, WebSocket-Init, Spielerzählung und Host-spezifische Start-Abbrechen-Buttons.
 * Redirect zu GamePage bei gameStarted.
 */
export function RoomMenu() {
    const navigate = useNavigate();
    const {
        initWebSocket,
        startGame,
        joinGameWs,
        playersInRoom,
        setInitialRoomState,
        gameStarted,
        gameID,
        setRoomID,
    } = useWebSocket();

    /**
     * Redirect zu GamePage bei gameStarted=true.
     */
    useEffect(() => {
        console.log("gameStarted changed to:", gameStarted);
        if (gameStarted) {
            navigate("/game-page");
        }
    }, [gameStarted, navigate]);

    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [roomCreated, setRoomCreated] = useState(false);
    const [joinedRoom, setJoinedRoom] = useState(false);
    const [isHost, setIsHost] = useState(false);

    /**
     * Erstellt neuen Uno-Raum über RoomService.
     * Setzt Room-ID, init WebSocket, join automatisch und markiert als Host.
     */
    const handleCreateRoom = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await RoomService.handleCreateRoom();
            setRoomID(data.roomId);
            setRoomCreated(true);
            setInitialRoomState(data.playerCount);
            initWebSocket(data.roomId, () => {
                joinGameWs(data.roomId, localStorage.getItem("username"));
            });
            setJoinedRoom(true);
        } catch (e) {
            console.log("Create Room failed:", e);
            setError("Erstellung eines Raumes fehlgeschlagen");
        } finally {
            setIsLoading(false);
            setIsHost(true);
        }
    };

    /**
     * Joined existierenden Raum per ID über RoomService.
     * Init WebSocket mit Delay für Join-Request.
     */
    const handleJoinRoom = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await RoomService.handleJoinRoom({ roomID: gameID });
            setInitialRoomState(data.playerCount)
            console.log(data);
            setJoinedRoom(true);

            initWebSocket(gameID);
            setTimeout(() => {
                joinGameWs(gameID, localStorage.getItem("username"));
            }, 500);
        } catch (e) {
            console.log("Joining Room failed:", e);
            setError("Beitreten eines Raumes fehlgeschlagen");
        } finally {
            setIsLoading(false);
        }
    };

    /**
     * Startet Spiel als Host (nur bei gültiger gameID).
     * Sendet startGame-Request an WebSocket.
     */
    const handleStartClick = () => {
        if (!gameID) return;
        startGame(gameID, localStorage.getItem("username"));
    };

    /**
     * Abbruch-Handler (zukünftig: Reset States und WS-Deactivate).
     */
    const handleCancel = () => {
        console.log("Missing Method");
    };

    return (
        <div
            className="game-menu"
            style={{
                textAlign: "center",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                flexDirection: "column",
                minHeight: "100vh",
                gap: "20px",
            }}
        >
            <img
                src={logo}
                alt="DHBW Uno Logo"
                style={{width: '300px', height: 'auto'}}
            />
            <CardLayout width="20%">
                {roomCreated || joinedRoom ? (
                    <>
                        <Grid size={12}>
                            <TextField
                                value={gameID}
                                disabled={false}
                                inputProps={{ style: { textAlign: "center" } }}
                            />
                        </Grid>
                        <Grid size={12}>
                            <label>Spieler im Raum: {playersInRoom}/4</label>
                        </Grid>
                        <Grid size={12}>
                            <TextField
                                fullWidth
                                label="Dein Name"
                                value={localStorage.getItem("username")}
                                disabled={true}
                            />
                        </Grid>

                        {isHost ? (
                            <>
                                <Grid size={6}>
                                    <CustomButton onClick={handleCancel}>Abbrechen</CustomButton>
                                </Grid>
                                <Grid size={6}>
                                    <CustomButton onClick={handleStartClick}>Starten</CustomButton>
                                </Grid>
                            </>
                        ) : (<></>)}
                    </>
                ) : (
                    <>
                        {error && (
                            <Alert severity="error" sx={{ width: "100%", mb: 2 }}>
                                {error}
                            </Alert>
                        )}
                        <Grid size={12}>
                            <CustomButton onClick={handleCreateRoom}>
                                Neuen Raum erstellen
                            </CustomButton>
                        </Grid>
                        <Grid size={12}>
                            <Divider
                                sx={{
                                    marginBottom: '15px',
                                    backgroundColor: 'red',
                                    height: '5%',
                                    width: '100%',
                                }}
                            />
                            <div>Existierendem Raum beitreten:</div>
                        </Grid>
                        <Grid size={12}>
                            <TextField
                                fullWidth
                                label="Raum ID eingeben"
                                type="text"
                                value={gameID}
                                onChange={(e) => setRoomID(e.target.value)}
                            />
                        </Grid>
                        <Grid size={12}>
                            <CustomButton onClick={handleJoinRoom} disabled={isLoading}>
                                {isLoading ? <CircularProgress size={24}/> : "Raum beitreten"}
                            </CustomButton>
                        </Grid>
                    </>
                )}
            </CardLayout>
        </div>
    );
}