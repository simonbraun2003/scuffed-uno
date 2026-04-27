// src/context/WebSocketContext.tsx
import {
    createContext,
    useContext,
    useEffect,
    useRef,
    useState,
    type ReactNode,
} from "react";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import type {DrawCardRequest, PlayCardRequest, TopicGameState, UserGameState} from "../types/Game.ts";
import {useNavigate} from "react-router-dom";

/**
 * React Context für die WebSocket-Verbindung zum Uno-Backend.
 * Verwaltet STOMP-Client, Raum- und Spielzustände sowie alle Game-Aktionen (Karte spielen, Ziehen, etc.).
 * Stellt Funktionen für Join, Start, Play/Draw und State-Subscriptions bereit.
 */
type WsContextType = {
    stompClient: Client | null;
    initWebSocket: (roomId: string, onReady?: () => void) => void;
    startGame: (gameId: string, player: string | unknown) => void;
    joinGameWs: (gameId: string, player: string | unknown) => void;
    getGameState: (gameId: string, player: string | unknown) => void;
    playersInRoom?: number;
    roomStatus?: number;
    setInitialRoomState: (playerCount?: number, roomStatus?: number) => void;
    gameStarted: boolean;
    gameID: string;
    setRoomID: (id: string) => void;
    playCard: ({gameID, card, color, playerName, chosenColor}: PlayCardRequest) => void;
    topicGameState: TopicGameState | null;
    userGameState: UserGameState | null;
    drawCard: ({gameID, playerName, drawCount}: DrawCardRequest) => void
};

const WebSocketContext = createContext<WsContextType | undefined>(undefined);

/**
 * Provider-Komponente für WebSocket-Verbindung und Uno-Game-States.
 * Initialisiert STOMP-Client mit SockJS-Fallback, abonniert Topics/Queues und handhabt alle Game-Aktionen.
 *
 * @param {Object} props - Komponenten-Props.
 * @param {ReactNode} props.children - Child-Komponenten, die auf WS-Context zugreifen.
 */
export const WebSocketProvider = ({ children }: { children: ReactNode }) => {
    const stompClientRef = useRef<Client | null>(null);
    const [playersInRoom, setPlayersInRoom] = useState<number>();
    const [roomStatus, setRoomStatus] = useState<number>();
    const [gameStarted, setGameStarted] = useState(false);
    const [topicGameState, setTopicGameState] = useState<TopicGameState | null>(null);
    const [userGameState, setUserGameState] = useState<UserGameState | null>(null);
    const [gameID, setGameID] = useState<string>("");

    const navigate = useNavigate();

    /**
     * Setzt initialen Raumzustand (Spieleranzahl und Status).
     *
     * @param {number} [playerCount] - Anzahl Spieler im Raum.
     * @param {number} [status] - Aktueller Raumstatus (z.B. 0=wartend, 1=gestartet).
     */
    const setInitialRoomState = (playerCount?: number, status?: number) => {
        if (playerCount !== undefined) setPlayersInRoom(playerCount);
        if (status !== undefined) setRoomStatus(status);
    };

    /**
     * Initialisiert WebSocket-Verbindung für angegebenen Raum.
     * Erstellt STOMP-Client, abonniert Room/Game/Queue-Topics und ruft Callback nach Connect auf.
     *
     * @param {string} roomId - ID des Uno-Raums.
     * @param {() => void} [onReady] - Optionale Callback-Funktion nach erfolgreichem Connect.
     */
    /**
     * ACHTUNG: Beim Beitreten in einem existierenden game Room kann es im Schnitt bei 1 von 5 Versuchen dazu kommen, dass
     * folgende Fehlermeldung geworfen wird:
     *
     * Broker error Failed to send message to ExecutorSubscribableChannel[clientInboundChannel]
     *
     * Die Ursache des Fehlers konnte im gegebenen Zeitrahmen nicht gefunden werden, es liegen aber folgende Vermutungen vor:
     *
     * 1) Die Websocket / STOMP Connection verbindet sich nicht korrekt und bricht daher die Verbindung ab während das Frontend weiterhin Daten sendet
     * 2) Die Anwendung ruf die "publish"-Funktionen auf bevor "onConnect" komplett durchlaufen wurde
     * 3) Die Anwendung lädt schneller als eine Antwort des Backends auf eine Frage des Frontends vorliegt, weshalb es zu einem Fehler kommt
     */
    const initWebSocket = (roomId: string, callback?: () => void) => {
        if (stompClientRef.current && stompClientRef.current.active) {
            callback?.()
            return;
        }

        const client = new Client({
            webSocketFactory: () =>
                new SockJS("https://unoserver.etiennebader.de/uno-websocket"),
            connectHeaders: {
                Authorization: "Bearer " + localStorage.getItem("token"),
            },
            reconnectDelay: 0,
            debug: msg => console.log("[STOMP]", msg),
            onConnect: () => {
                console.log("WS connected for room", roomId);

                client.subscribe(`/topic/room/${roomId}`, (message: IMessage) => {
                    try {
                        const body = JSON.parse(message.body);
                        if (body.playerCount !== undefined) {
                            setPlayersInRoom(body.playerCount);
                        }
                        if (body.roomStatus !== undefined) {
                            setRoomStatus(body.roomStatus);
                        }

                        setGameID(roomId)
                    } catch (e) {
                        console.error("Failed to parse room message", e, message.body);
                    }
                });

                client.subscribe(`/topic/game/${roomId}`, (message: IMessage) => {
                    const body = JSON.parse(message.body);
                    if(body.type)
                        setTopicGameState(body)
                    console.log("/topic/game/...:", body);

                    if (body.type === "GAME_STARTED") {
                        setGameStarted(true);
                    }
                    if(body.type === "PLAYER_CONNECTED"){
                        setPlayerReady(gameID, localStorage.getItem("username"));
                        console.log("PLAYER_CONNECTED AND SET READY");
                    }
                    if(body.type === "GAME_OVER") {
                        console.log("-----------WINNING-----------")
                        navigate("/room-menu")
                    }
                });

                client.subscribe(`/user/${localStorage.getItem("username")}/queue/errors`, (message: IMessage) => {
                    const body = JSON.parse(message.body);
                    console.log("user/.../queue/error:", body);
                });

                client.subscribe(`/user/${localStorage.getItem("username")}/queue/gameState`, (message: IMessage) => {
                    const body = JSON.parse(message.body);
                    console.log("/user/.../queue/gameState:", body);
                    setUserGameState(body)
                });

                setTimeout(() => {
                    callback?.();
                    getGameState(roomId, localStorage.getItem("username"));
                }, 200);
            },
            onStompError: frame => {
                console.error("!_! Broker error", frame.headers["message"], frame.body);
                navigate("/game-menu");
            },
        });

        stompClientRef.current = client;
        client.activate();
    };

    useEffect(() => {
        return () => {
            if (stompClientRef.current && stompClientRef.current.active) {
                stompClientRef.current.deactivate();
            }
        };
    }, []);

    /**
     * Startet das Uno-Spiel und setzt Spieler als ready.
     * Sendet /app/startGame und /app/playerReady an Backend.
     *
     * @param {string} gameId - ID des Spiels.
     * @param {string | unknown} player - Spielername.
     */
    const startGame = async (gameId: string, player: string | unknown): Promise<void> => {
        stompClientRef.current?.publish({
            destination: "/app/startGame",
            body: JSON.stringify({ gameId, player }),
        });
        stompClientRef.current?.publish({
            destination: "/app/playerReady",
            body: JSON.stringify({ gameId, player }),
        });
        await getGameState(gameId, player);
    };

    /**
     * Prüft, ob STOMP-Client verbunden ist.
     *
     * @returns {boolean} True, wenn Client aktiv und connected.
     */
    const isConnected = (): boolean => {
        const client = stompClientRef.current;
        return !!(client &&
            client.active &&
            client.connected &&
            client.state === 1);
    };

    /**
     * Joined ein bestehendes Uno-Spiel.
     * Sendet /app/join nur bei aktiver Verbindung.
     *
     * @param {string} gameId - ID des Spiels.
     * @param {string | unknown} player - Spielername.
     */
    const joinGameWs = async (gameId: string, player: string | unknown): Promise<void> => {
        if(!isConnected()) {
            console.log("KEINE VERBINDUNG - ABBRECHEN!")
            return;
        }
        stompClientRef.current?.publish({
            destination: "/app/join",
            body: JSON.stringify({ gameId, player }),
        });
    };

    /**
     * Sendet Player-Ready-Status an Backend.
     *
     * @param {string} gameId - ID des Spiels.
     * @param {string | unknown} player - Spielername.
     */
    const setPlayerReady = async (gameId: string, player: string | unknown): Promise<void> => {
        stompClientRef.current?.publish({
            destination: "/app/playerReady",
            body: JSON.stringify({ gameId, player }),
        });
    }

    /**
     * Fordert aktuellen Game-State vom Backend an.
     * Sendet /app/getGameState; Response kommt über /user/queue/gameState.
     *
     * @param {string} gameId - ID des Spiels.
     * @param {string | unknown} player - Spielername.
     */
    const getGameState = async (gameId: string, player: string | unknown): Promise<void> => {
        stompClientRef.current?.publish({
            destination: "/app/getGameState",
            body: JSON.stringify({ gameId, player }),
        });
    };

    /**
     * Spielt eine Karte im Uno-Spiel.
     * Sendet /app/playCard mit Karte, Farbe und optionaler gewählter Farbe.
     *
     * @param {PlayCardRequest} request - Spiel-Request mit gameID, card, color, playerName, chosenColor.
     */
    const playCard = async ({gameID, card, color, playerName, chosenColor}: PlayCardRequest): Promise<void> => {
        stompClientRef.current?.publish({
            destination: "/app/playCard",
            body: JSON.stringify({
                gameId: gameID,
                card: card,
                color: color,
                player: playerName,
                chosenColor: chosenColor,
            })
        });
    };

    /**
     * Zieht Karten im Uno-Spiel.
     * Sendet /app/drawCard mit Anzahl zu ziehender Karten.
     *
     * @param {DrawCardRequest} request - Draw-Request mit gameID, playerName, drawCount.
     */
    const drawCard = async ({gameID, playerName, drawCount}: DrawCardRequest): Promise<void> => {
        stompClientRef.current?.publish({
            destination: "/app/drawCard",
            body: JSON.stringify({
                gameId: gameID,
                player: playerName,
                drawCount: drawCount,
            })
        });
    };

    return (
        <WebSocketContext.Provider
            value={{
                stompClient: stompClientRef.current,
                initWebSocket,
                startGame,
                joinGameWs,
                getGameState,
                playersInRoom,
                roomStatus,
                setInitialRoomState,
                gameStarted,
                gameID,
                setRoomID: setGameID,
                playCard,
                topicGameState,
                userGameState,
                drawCard
            }}
        >
            {children}
        </WebSocketContext.Provider>
    );
};

/**
 * Custom Hook für Zugriff auf WebSocketContext.
 *
 * @returns {WsContextType} - Context mit Client, States und allen Game-Funktionen.
 * @throws {Error} - Falls Hook außerhalb von WebSocketProvider verwendet wird.
 */
export const useWebSocket = (): WsContextType => {
    const ctx = useContext(WebSocketContext);
    if (!ctx) {
        throw new Error("useWebSocket must be used within WebSocketProvider");
    }
    return ctx;
};