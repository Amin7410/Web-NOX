import * as React from "react"

const buttonVariants = {
  variants: {
    default: "bg-[#4F46E5] text-white hover:bg-[#4338CA] focus:ring-2 focus:ring-[#4F46E5] focus:ring-offset-2 shadow-sm hover:shadow-md transition-all duration-200",
    destructive: "bg-[#EF4444] text-white hover:bg-[#DC2626] focus:ring-2 focus:ring-[#EF4444] focus:ring-offset-2 shadow-sm hover:shadow-md transition-all duration-200",
    outline: "border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 hover:text-gray-900 focus:ring-2 focus:ring-[#4F46E5] focus:ring-offset-2 transition-all duration-200",
    secondary: "bg-gray-100 text-gray-900 hover:bg-gray-200 focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-all duration-200",
    ghost: "text-gray-700 hover:bg-gray-100 hover:text-gray-900 focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-all duration-200",
    link: "text-[#4F46E5] underline-offset-4 hover:underline focus:ring-2 focus:ring-[#4F46E5] focus:ring-offset-2 transition-all duration-200",
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
