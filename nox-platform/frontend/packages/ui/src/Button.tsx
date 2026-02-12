import * as React from "react";

export interface ButtonProps {
    children: React.ReactNode;
    onClick?: () => void;
}

export function Button({ children, onClick }: ButtonProps) {
    return (
        <button
            onClick={onClick}
            className="px-5 py-2.5 text-base rounded cursor-pointer bg-primary text-primary-foreground border-none hover:bg-primary/90 transition-colors"
        >
            {children}
        </button>
    );
}
