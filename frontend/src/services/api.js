import axios from 'axios'

const api = axios.create({
  baseURL: 'https://special-fiesta-pjwg96rr9gv637vp5-8080.app.github.dev/api/v1',
})

api.interceptors.request.use((config) => {
  const isAuthEndpoint = config.url.includes('/auth/login') || config.url.includes('/auth/signup')

  if (!isAuthEndpoint) {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }

  return config
})

export default api
