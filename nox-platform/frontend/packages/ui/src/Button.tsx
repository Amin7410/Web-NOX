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
      "rounded-lg font-medium " +
      "border border-white/50 " +
      "transition-all duration-200 " +
      "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 " +
      "disabled:opacity-50 disabled:pointer-events-none " +
      "ring-offset-background active:scale-[0.90]"
      ;

    const variants = {
      primary:
        "bg-blue-600 text-white " +
        "hover:bg-blue-600/90 " +
        "shadow-[0_0_20px_-5px_rgba(37,99,235,0.4)] " +
        "hover:shadow-[0_0_25px_-5px_rgba(37,99,235,0.6)] " +
        "border border-blue-500/50",

      secondary:
        "bg-white/10 text-white " +
        "hover:bg-white/20 " +
        "border border-white/10 backdrop-blur-sm",

      outline:
        "border border-white/20 bg-transparent " +
        "hover:bg-white/5 text-white",

      ghost:
        "hover:bg-white/10 text-white",

      destructive:
        "bg-red-500/10 text-red-400 " +
        "hover:bg-red-500/20 border border-red-500/20",
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
