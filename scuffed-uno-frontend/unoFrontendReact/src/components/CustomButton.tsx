import { Button, type ButtonProps } from "@mui/material";

/**
 * Wiederverwendbare Button‑Komponente mit einheitlichem visuellen Stil.
 * Verwendet den MUI‑Button im Varianten‑Stil „contained“ und setzt eine feste Hintergrundfarbe.
 */
export function CustomButton({ children, ...props }: ButtonProps) {
    return (
        <Button
            variant="contained"
            sx={{ backgroundColor: "#fdda00", width: "100%" }}
            {...props}
        >
            {children}
        </Button>
    );
}