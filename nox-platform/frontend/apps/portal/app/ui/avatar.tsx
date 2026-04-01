import * as React from "react"

export interface AvatarProps extends React.HTMLAttributes<HTMLDivElement> {}
export interface AvatarImageProps extends React.ImgHTMLAttributes<HTMLImageElement> {}
export interface AvatarFallbackProps extends React.HTMLAttributes<HTMLDivElement> {}

const Avatar = React.forwardRef<HTMLDivElement, AvatarProps>(
  ({ className, ...props }, ref) => {
    return (
      <div
        className={`relative flex h-10 w-10 shrink-0 overflow-hidden rounded-full border-2 border-gray-200 ${className || ""}`}
        ref={ref}
        {...props}
      />
    )
  }
)
Avatar.displayName = "Avatar"

const AvatarImage = React.forwardRef<HTMLImageElement, AvatarImageProps>(
  ({ className, ...props }, ref) => {
    return (
      <img
        className={`aspect-square h-full w-full object-cover ${className || ""}`}
        ref={ref}
        {...props}
      />
    )
  }
)
AvatarImage.displayName = "AvatarImage"

const AvatarFallback = React.forwardRef<HTMLDivElement, AvatarFallbackProps>(
  ({ className, ...props }, ref) => {
    return (
      <div
        className={`flex h-full w-full items-center justify-center rounded-full bg-[#4F46E5] text-white font-medium text-sm ${className || ""}`}
        ref={ref}
        {...props}
      />
    )
  }
)
AvatarFallback.displayName = "AvatarFallback"

export { Avatar, AvatarImage, AvatarFallback }
