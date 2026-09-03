import axios from 'axios'
import { useAuthStore } from '@/store/auth.js'

const api = axios.create({
  baseURL: '',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    // 추천은 로그인 후 보조적으로 불러오는 데이터다. 추천 서비스의 일시적인
    // 인증/통신 오류가 전체 로그인 세션을 지우면 안 된다.
    const isRecommendationRequest = err.config?.url?.startsWith('/api/recommend/')
    if (err.response?.status === 401 && !isRecommendationRequest) {
      console.error('[API] 401 Unauthorized, request url =', err.config?.url)
      const auth = useAuthStore()
      auth.logout()
    }
    return Promise.reject(err)
  }
)

export default api
