// 开发环境使用代理，生产环境使用相对路径（由 Nginx 代理转发）
export const BASE_URL = process.env.VUE_APP_API_URL || "/api"
export const TIMEOUT = 10000