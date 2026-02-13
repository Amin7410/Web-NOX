import * as React from "react";

export interface InputProps
  extends Omit<
    React.InputHTMLAttributes<HTMLInputElement>,
    "size"
  > {

  size?: "sm" | "md" | "lg";

  error?: boolean;

  startIcon?: React.ReactNode;
}

export const Input = React.forwardRef<
  HTMLInputElement,
  InputProps
>(
  (
    {
      className,
      size = "md",
      error,
      startIcon,
      ...props
    },
    ref
  ) => {

    const baseStyles =
      "flex w-full rounded-md border " +
      "bg-zinc-950/50 px-3 py-1 text-base " +
      "shadow-sm transition-all duration-200 " +
      "file:border-0 file:bg-transparent file:text-sm file:font-medium " +
      "placeholder:text-zinc-500 " +
      "focus-visible:outline-none focus-visible:ring-2 " +
      "disabled:cursor-not-allowed disabled:opacity-50 " +
      "text-zinc-100";

    const variants = {
      default:
        "border-zinc-800 " +
        "focus-visible:border-primary focus-visible:ring-primary/20",

      error:
        "border-red-500/50 " +
        "focus-visible:border-red-500 focus-visible:ring-red-500/20 " +
        "text-red-400 placeholder:text-red-400/50",
    };

    const sizes = {
      sm: "h-9 text-sm",
      md: "h-11 text-base",
      lg: "h-14 text-lg px-4",
    };

    const iconPadding = startIcon ? "pl-10" : "";

    const classes = [
      baseStyles,
      error ? variants.error : variants.default,
      sizes[size],
      iconPadding,
      className,
    ]
      .filter(Boolean)
      .join(" ");

    return (
      <div className="relative w-full">
        {startIcon && (
          <div
            className="
              absolute left-3 top-1/2
              -translate-y-1/2
              text-muted-foreground
              pointer-events-none
            "
          >
            {startIcon}
          </div>
        )}

        <input
          ref={ref}
          className={classes}
          {...props}
        />
      </div>
    );
  }
);

Input.displayName = "Input";
