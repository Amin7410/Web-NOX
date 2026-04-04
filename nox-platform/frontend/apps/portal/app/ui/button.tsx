import * as React from "react"

const buttonVariants = {
  variants: {
    default: "bg-[rgb(var(--accent))] text-[rgb(var(--accent-foreground))] hover:bg-[rgb(var(--accent))]/90 hover:shadow-[0_0_0_16px_rgba(56,189,248,0.18)] active:scale-95 focus:ring-2 focus:ring-[rgb(var(--ring))] focus:ring-offset-2 focus:ring-offset-[rgb(var(--background))] shadow-sm transition-all duration-200 ease-in-out",
    destructive: "bg-[rgb(var(--destructive))] text-[rgb(var(--destructive-foreground))] hover:bg-[#DC2626] active:scale-95 focus:ring-2 focus:ring-[rgb(var(--destructive))] focus:ring-offset-2 focus:ring-offset-[rgb(var(--background))] shadow-sm transition-all duration-200 ease-in-out",
    outline: "border border-[rgb(var(--border))] bg-[rgb(var(--surface))] text-[rgb(var(--foreground))] hover:bg-[rgb(var(--surface))] hover:text-[rgb(var(--foreground))] active:scale-95 focus:ring-2 focus:ring-[rgb(var(--ring))] focus:ring-offset-2 focus:ring-offset-[rgb(var(--background))] transition-all duration-200 ease-in-out",
    secondary: "bg-[rgb(var(--surface))] text-[rgb(var(--foreground))] hover:bg-[rgb(var(--surface))] active:scale-95 focus:ring-2 focus:ring-[rgb(var(--ring))] focus:ring-offset-2 focus:ring-offset-[rgb(var(--background))] transition-all duration-200 ease-in-out",
    ghost: "text-[rgb(var(--foreground))] hover:bg-[rgb(var(--surface))] hover:text-[rgb(var(--foreground))] active:scale-95 focus:ring-2 focus:ring-[rgb(var(--ring))] focus:ring-offset-2 focus:ring-offset-[rgb(var(--background))] transition-all duration-200 ease-in-out",
    link: "text-[rgb(var(--accent))] underline-offset-4 hover:underline active:scale-95 focus:ring-2 focus:ring-[rgb(var(--ring))] focus:ring-offset-2 focus:ring-offset-[rgb(var(--background))] transition-all duration-200 ease-in-out",
  },
  sizes: {
    default: "h-10 px-4 py-2 text-sm font-medium rounded-lg inline-flex items-center gap-2",
    sm: "h-9 px-3 text-sm font-medium rounded-md inline-flex items-center gap-1.5",
    lg: "h-12 px-6 text-sm font-medium rounded-lg inline-flex items-center gap-3",
    icon: "h-10 w-10 rounded-lg inline-flex items-center justify-center",
  },
}

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: keyof typeof buttonVariants.variants
  size?: keyof typeof buttonVariants.sizes
  asChild?: boolean
  loading?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "default", size = "default", asChild = false, loading = false, children, disabled, ...props }, ref) => {
    const variantClass = buttonVariants.variants[variant]
    const sizeClass = buttonVariants.sizes[size]
    
    const Comp = asChild ? "span" : "button"
    const isDisabled = disabled || loading
    
    return (
      <Comp
        className={`${variantClass} ${sizeClass} ${className || ""}`}
        ref={ref}
        disabled={isDisabled}
        {...props}
      >
        {loading && (
          <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"></div>
        )}
        {children}
      </Comp>
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
