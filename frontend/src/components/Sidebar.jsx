import { NavLink } from 'react-router-dom'

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: '◱' },
  { to: '/orders', label: 'Orders', icon: '▤' },
  { to: '/payments', label: 'Payments', icon: '▢' },
  { to: '/refunds', label: 'Refunds', icon: '↺' },
  { to: '/webhooks', label: 'Webhooks', icon: '⚡' },
]

function Sidebar() {
  return (
    <div className="w-45 bg-gray-50 border-r border-gray-200 p-4">
      <div className="font-medium text-base mb-6 px-2">PayUp</div>
      <nav className="flex flex-col gap-1">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex items-center gap-2 px-3 py-2 rounded-md text-sm ${
                isActive
                  ? 'bg-blue-50 text-blue-700 font-medium'
                  : 'text-gray-600 hover:bg-gray-100'
              }`
            }
          >
            <span>{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>
    </div>
  )
}

export default Sidebar
