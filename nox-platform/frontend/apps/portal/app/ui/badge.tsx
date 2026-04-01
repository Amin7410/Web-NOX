import * as React from "react"

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: "default" | "secondary" | "destructive" | "outline"
}

const Badge = React.forwardRef<HTMLDivElement, BadgeProps>(
  ({ className, variant = "default", ...props }, ref) => {
    const variantClasses = {
      default: "bg-[#4F46E5]/10 text-[#4F46E5] border-[#4F46E5]/20",
      secondary: "bg-gray-100 text-gray-800 border-gray-200", 
      destructive: "bg-[#EF4444]/10 text-[#EF4444] border-[#EF4444]/20",
      outline: "border border-gray-300 text-gray-800 bg-white"
    }

    return (
      <div
        className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-[#4F46E5] focus:ring-offset-2 ${variantClasses[variant]} ${className || ""}`}
        ref={ref}
        {...props}
      />
    )
  }
)
Badge.displayName = "Badge"

export { Badge }
