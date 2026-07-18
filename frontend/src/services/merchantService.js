import api from './api'

export async function getMerchantProfile() {
  const response = await api.get('/merchants/me')
  return response.data
}
