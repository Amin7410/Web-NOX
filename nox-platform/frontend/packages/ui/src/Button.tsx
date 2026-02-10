import * as React from "react";

export interface ButtonProps {
    children: React.ReactNode;
    onClick?: () => void;
}

export function Button({ children, onClick }: ButtonProps) {
    return (
        <button
            onClick={onClick}
            style={{
                padding: "10px 20px",
                fontSize: "16px",
                borderRadius: "4px",
                cursor: "pointer",
                backgroundColor: "#007bff",
                color: "white",
                border: "none",
            }}
        >
            {children}
        </button>
    );
}
