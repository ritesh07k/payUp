import Sidebar from './Sidebar'

function Layout({ children }) {
  return (
    <div className="min-h-screen flex bg-white">
      <Sidebar />
      <div className="flex-1 p-8">{children}</div>
    </div>
  )
}

export default Layout
