import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
    baseURL: process.env.VUE_APP_API_URL || '/api',
    timeout: 10000
})

// Request interceptor — attach JWT token
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => Promise.reject(error)
)

// Response interceptor
service.interceptors.response.use(
    response => {
        return response.data
    },
    error => {
        if (error.response?.status === 403) {
            ElMessage.error(error.response?.data?.msg || '游客无权执行此操作，请先登录')
        } else {
            ElMessage.error(error.response?.data?.msg || error.message)
        }
        return Promise.reject(error)
    }
)

export default service
