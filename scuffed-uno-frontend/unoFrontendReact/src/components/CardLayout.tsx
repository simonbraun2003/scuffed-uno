import { Card, Grid, type CardProps } from "@mui/material";

/**
 * Props‑Interface für CardLayout.
 * Erweitert die Standard‑CardProps von MUI um ein children‑Prop und eine optionale width.
 */
interface CardLayoutProps extends CardProps {
    /**
     * Inhalt der Card (z.B. Grid‑Items mit Buttons oder Formularen).
     */
    children: React.ReactNode;

    /**
     * Optionaler Breiten‑Wert der Card (z.B. "20%", "300px").
     * Standard ist "20%".
     */
    width?: string;
}

/**
 * Wiederverwendbare Karten‑Layout‑Komponente.
 * Stellt einen umrahmten Card‑Container mit flexibler Breite und zentriertem Inhalt bereit.
 */
export function CardLayout({ children, width = "20%", ...props }: CardLayoutProps) {
    return (
        <Card
            variant="outlined"
            sx={{ width, textAlign: "center" }}
            {...props}
        >
            <Grid container spacing={2} sx={{ margin: "30px" }}>
                {children}
            </Grid>
        </Card>
    );
}