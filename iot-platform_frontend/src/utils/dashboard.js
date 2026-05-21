import request from '@/utils/request'

/**
 * 获取仪表盘核心数据
 */
export const fetchDashboardData = () => {
    return request({
        url: '/panel',
        method: 'get'
    })
}

/**
 * 获取48小时消息趋势数据
 */
export const fetchMessageTrend = () => {
    return request({
        url: '/message-trend',
        method: 'get'
    })
}

/**
 * 合并获取所有仪表盘数据
 */
export const getCombinedDashboardData = async () => {
    try {
        const [dashboardRes, trendRes] = await Promise.all([
            fetchDashboardData(),
            fetchMessageTrend()
        ])

        return {
            ...dashboardRes.data.data,
            messageTrendLast48h: trendRes.data.data || []
        }
    } catch (error) {
        console.error('获取仪表盘数据失败:', error)
        throw error
    }
}
