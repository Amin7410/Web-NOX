import * as React from "react"

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, ...props }, ref) => {
    return (
      <input
        type={type}
        className={`flex h-10 w-full rounded-lg border border-[rgb(var(--border))] bg-[rgb(var(--bg-surface))] px-4 py-2.5 text-sm text-[rgb(var(--text-main))] placeholder:text-[rgb(var(--text-sub))] focus:border-[rgb(var(--accent))] focus:ring-2 focus:ring-[rgba(var(--accent),0.24)] focus:ring-offset-2 focus:ring-offset-[rgb(var(--bg-app))] caret-[rgb(var(--text-main))] active:scale-95 disabled:cursor-not-allowed disabled:opacity-50 transition-all duration-200 ease-in-out ${className || ""}`}
        ref={ref}
        {...props}
      />
    )
  }
)
Input.displayName = "Input"

export { Input }
