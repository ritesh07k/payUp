function Button({ children, onClick, type = 'button', variant = 'primary' }) {
  const baseStyles = 'w-full py-2 px-4 rounded-md text-sm font-medium transition-colors'
  const variantStyles = {
    primary: 'bg-gray-900 text-white hover:bg-gray-800',
    secondary: 'bg-white text-gray-900 border border-gray-300 hover:bg-gray-50',
  }

  return (
    <button
      type={type}
      onClick={onClick}
      className={`${baseStyles} ${variantStyles[variant]}`}
    >
      {children}
    </button>
  )
}

export default Button
