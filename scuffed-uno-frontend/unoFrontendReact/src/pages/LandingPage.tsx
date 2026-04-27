import '../styles/LandingPage.css'
import {LoginForm} from "../components/LoginForm.tsx";
import logo from '../assets/scuffed-dhbw-uno-logo.png';

/**
 * Landing Page mit Logo und Login-Formular.
 * Zentriert Logo und LoginForm vertikal/horizontal im Viewport.
 */
export function LandingPage() {
    return (
        <>
            <div
                className="div-landing"
                style={{
                    textAlign: "center",
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    flexDirection: 'column',
                    minHeight: '100vh',
                    gap: '20px',
                }}>
                <img
                    src={logo}
                    alt="DHBW Uno Logo"
                    style={{width: '300px', height: 'auto'}}
                />
                <LoginForm/>
            </div>
        </>
    )
}