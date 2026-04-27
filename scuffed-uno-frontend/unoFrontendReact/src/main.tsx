import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'

import {BrowserRouter, Route, Routes} from "react-router-dom";
import {LandingPage} from "./pages/LandingPage.tsx";
import {UserMenu} from "./pages/UserMenu.tsx";
import {AuthProvider} from "./context/AuthContext.tsx";
import {ProtectedRoute} from "./components/ProtectedRoute.tsx";
import {RoomMenu} from "./pages/RoomMenu.tsx";
import {GamePage} from "./pages/GamePage.tsx";
import {WebSocketProvider} from "./context/WebSocketContext.tsx";

/**
 * Root-Render-Funktion der Uno-React-App.
 * Nestet Provider (Auth → Router → WebSocket) und definiert alle Routen.
 * Public: LandingPage; Protected: UserMenu, RoomMenu, GamePage.
 */
createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <AuthProvider>
            <BrowserRouter>
                <WebSocketProvider>
                    <Routes>
                        <Route path="/" element={<LandingPage/>}/>
                        <Route element={<ProtectedRoute/>}>
                            <Route path="/game-menu" element={<UserMenu/>}/>

                            <Route path="/room-menu" element={<RoomMenu/>}/>
                            <Route path="/game-page" element={<GamePage/>}/>

                        </Route>
                    </Routes>
                </WebSocketProvider>
            </BrowserRouter>
        </AuthProvider>
    </StrictMode>,
)