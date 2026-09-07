import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 120000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && typeof payload.code === 'number' && payload.code !== 0) {
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(new Error(payload.message))
    }
    return payload
  },
  (error) => {
    const status = error.response?.status
    const payload = error.response?.data
    let message: string = payload?.message || error.message || '网络错误'
    if (status === 401) {
      // 40101 = token 过期，40102 = token 无效，其余 = 未登录
      if (payload?.code === 40101) message = '登录已过期，请重新登录'
      else if (payload?.code === 40102) message = '登录凭证无效，请重新登录'
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      const here = window.location.pathname + window.location.search
      if (!window.location.pathname.startsWith('/login')) {
        const redirect = here && here !== '/' ? `?redirect=${encodeURIComponent(here)}` : ''
        window.location.assign(`/login${redirect}`)
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    } else if (!error.response) {
      message = '无法连接服务器，请检查后端是否启动'
    }
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
)

export default http
