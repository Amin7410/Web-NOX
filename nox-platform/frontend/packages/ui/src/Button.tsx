import * as React from "react";

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {

  variant?: "primary" | "secondary" | "outline" | "ghost" | "destructive";

  size?: "sm" | "md" | "lg";

  asChild?: boolean;
}

export const Button = React.forwardRef<
  HTMLButtonElement,
  ButtonProps
>(
  (
    {
      className,
      variant = "primary",
      size = "md",
      ...props
    },
    ref
  ) => {

    const baseStyles =
      "inline-flex items-center justify-center " +
      "rounded-lg font-semibold " +
      "transition-all duration-200 " +
      "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 " +
      "disabled:opacity-50 disabled:pointer-events-none " +
      "ring-offset-background active:scale-[0.96]"
      ;

    const variants = {
      primary:
        "bg-primary text-primary-foreground " +
        "hover:bg-primary/90 " +
        "shadow-[0_8px_16px_-6px_rgba(59,130,246,0.3)] " +
        "hover:shadow-[0_12px_20px_-6px_rgba(59,130,246,0.5)] ",

      secondary:
        "bg-secondary text-secondary-foreground " +
        "hover:bg-secondary/80 " +
        "border border-input backdrop-blur-sm",

      outline:
        "border border-input bg-transparent " +
        "hover:bg-accent hover:text-accent-foreground text-foreground",

      ghost:
        "hover:bg-accent hover:text-accent-foreground text-foreground",

      destructive:
        "bg-destructive text-destructive-foreground " +
        "hover:bg-destructive/90 border border-destructive/20",
    };

    const sizes = {
      sm: "h-8 px-3 text-xs",
      md: "h-10 px-4 py-2 text-sm",
      lg: "h-12 px-8 text-base",
    };

    const classes = [
      baseStyles,
      variants[variant],
      sizes[size],
      className,
    ]
      .filter(Boolean)
      .join(" ");

    return (
      <button
        ref={ref}
        className={classes}
        {...props}
      />
    );
  }
);

Button.displayName = "Button";
