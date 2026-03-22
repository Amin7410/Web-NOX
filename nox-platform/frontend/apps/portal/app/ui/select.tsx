import * as React from "react"
import { ChevronDown } from "lucide-react"

export interface SelectProps {
  value?: string
  onValueChange?: (value: string) => void
  placeholder?: string
  children: React.ReactNode
  className?: string
}

const Select = React.forwardRef<HTMLDivElement, SelectProps>(
  ({ value, onValueChange, placeholder, children, className, ...props }, ref) => {
    const [isOpen, setIsOpen] = React.useState(false)
    const [selectedValue, setSelectedValue] = React.useState(value || "")

    const handleSelect = (newValue: string) => {
      setSelectedValue(newValue)
      onValueChange?.(newValue)
      setIsOpen(false)
    }

    return (
      <div className="relative" ref={ref} {...props}>
        <button
          type="button"
          onClick={() => setIsOpen(!isOpen)}
          className={`flex h-10 w-full items-center justify-between rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm text-gray-900 placeholder:text-gray-500 focus:border-[#4F46E5] focus:ring-2 focus:ring-[#4F46E5] focus:ring-offset-2 focus:ring-offset-white disabled:cursor-not-allowed disabled:opacity-50 transition-colors ${className || ""}`}
        >
          <span className={selectedValue ? "" : "text-gray-500"}>
            {selectedValue || placeholder}
          </span>
          <ChevronDown className="h-4 w-4 text-gray-400" />
        </button>
        
        {isOpen && (
          <div className="absolute top-full left-0 right-0 z-50 mt-1 rounded-lg border border-gray-200 bg-white shadow-lg">
            <div className="max-h-60 overflow-auto py-1">
              {React.Children.map(children, (child) => {
                if (React.isValidElement(child)) {
                  return React.cloneElement(child as React.ReactElement, {
                    onSelect: handleSelect,
                    isSelected: selectedValue === child.props.value,
                  })
                }
                return child
              })}
            </div>
          </div>
        )}
      </div>
    )
  }
)
Select.displayName = "Select"

export interface SelectItemProps {
  value: string
  children: React.ReactNode
  onSelect?: (value: string) => void
  isSelected?: boolean
}

const SelectItem = React.forwardRef<HTMLDivElement, SelectItemProps>(
  ({ value, children, onSelect, isSelected, ...props }, ref) => {
    return (
      <div
        ref={ref}
        {...props}
        onClick={() => onSelect?.(value)}
        className={`relative flex cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none hover:bg-gray-50 focus:bg-gray-50 ${isSelected ? "bg-gray-100" : ""}`}
      >
        <span className="block truncate">{children}</span>
        {isSelected && (
          <span className="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
            <svg className="h-4 w-4 text-[#4F46E5]" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
            </svg>
          </span>
        )}
      </div>
    )
  }
)
SelectItem.displayName = "SelectItem"

export const SelectTrigger = React.forwardRef<HTMLButtonElement, React.ButtonHTMLAttributes<HTMLButtonElement>>(
  ({ className, children, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={`flex h-10 w-full items-center justify-between rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm text-gray-900 placeholder:text-gray-500 focus:border-[#4F46E5] focus:ring-2 focus:ring-[#4F46E5] focus:ring-offset-2 focus:ring-offset-white disabled:cursor-not-allowed disabled:opacity-50 transition-colors ${className || ""}`}
        {...props}
      >
        {children}
        <ChevronDown className="h-4 w-4 text-gray-400" />
      </button>
    )
  }
)

export const SelectValue = React.forwardRef<HTMLSpanElement, React.HTMLAttributes<HTMLSpanElement>>(
  ({ className, ...props }, ref) => {
    return <span ref={ref} className={`block truncate ${className || ""}`} {...props} />
  }
)

export const SelectContent = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, children, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={`absolute top-full left-0 right-0 z-50 mt-1 rounded-lg border border-gray-200 bg-white shadow-lg max-h-60 overflow-auto py-1 ${className || ""}`}
        {...props}
      >
        {children}
      </div>
    )
  }
)

export { Select, SelectItem, SelectTrigger, SelectValue, SelectContent }
