import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import Input from '../components/Input'
import { login } from '../services/authService'
import { useAuth } from '../context/AuthContext'

function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const { loginUser } = useAuth()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const result = await login(email, password)
      loginUser(result.data.token)
      navigate('/dashboard')
    } catch (err) {
      setError('Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-sm bg-white border border-gray-200 rounded-xl p-8">
        <h1 className="text-xl font-medium text-gray-900 mb-6 text-center">
          Log in to PayUp
        </h1>

        {error && (
          <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-md px-3 py-2">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <Input
            label="Email"
            type="email"
            placeholder="you@company.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <Input
            label="Password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <div className="mt-6">
            <Button type="submit">{loading ? 'Logging in...' : 'Log in'}</Button>
          </div>
        </form>

        <p className="text-sm text-gray-500 text-center mt-4">
          Don't have an account? <span className="text-gray-900 font-medium cursor-pointer">Sign up</span>
        </p>
      </div>
    </div>
  )
}

export default LoginPage
