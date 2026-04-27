import { useAuth } from "../context/AuthContext.tsx";
import { Outlet, Navigate } from "react-router-dom";

/**
 * Schutz‑Wrapper für eingeloggte Bereiche.
 * Wenn der Nutzer eingeloggt ist (isAuthenticated), wird der Inhalt (Outlet) angezeigt.
 */
export function ProtectedRoute() {
    const { isAuthenticated } = useAuth();
    return isAuthenticated ? <Outlet /> : <Navigate to="/" />;
}