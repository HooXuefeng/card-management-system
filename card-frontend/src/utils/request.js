import axios from 'axios'

// 创建axios实例，匹配后端上下文路径和IP
const service = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端地址+上下文路径/api
  timeout: 5000 // 请求超时时间
  // withCredentials: true // 注释掉这行，避免跨域问题
})

// 请求拦截器：添加token到请求头（登录后用）
service.interceptors.request.use(
  config => {
    // 优先从localStorage获取token，如果没有再从sessionStorage获取
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}` // 后端如果用JWT需要这个
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理未登录
service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    // 仅在请求非登录接口时，才提示未登录
    if (error.response && error.response.status === 401) {
      // 排除登录接口本身的401（比如密码错误）
      if (!error.config.url.includes('/login')) {
        // 清除所有存储的token
        localStorage.removeItem('token')
        sessionStorage.removeItem('token')
        alert('登录已过期，请重新登录')
        window.location.href = '/login' // 跳登录页
      }
    }
    console.error('响应错误:', error)
    return Promise.reject(error)
  }
)

export default service