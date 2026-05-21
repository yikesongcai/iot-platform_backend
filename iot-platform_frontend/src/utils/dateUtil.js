/**
 * 日期时间格式化工具
 * @module utils/dateUtil
 */

/**
 * 格式化日期时间为指定格式
 * @param {Date|string|number} date - 日期对象、日期字符串或时间戳
 * @param {string} [format='YYYY-MM-DD HH:mm:ss'] - 格式化字符串
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
    if (!date) return ''

    let dateObj
    if (date instanceof Date) {
        dateObj = date
    } else if (typeof date === 'string') {
        // 处理IOS日期格式问题（将"-"替换为"/"）
        const isoDate = date.replace(/-/g, '/').replace(/T/g, ' ')
        dateObj = new Date(isoDate)
    } else if (typeof date === 'number') {
        dateObj = new Date(date)
    } else {
        return ''
    }

    // 如果转换后的日期无效，则返回空字符串
    if (isNaN(dateObj.getTime())) return ''

    const padStart = (value, length = 2) => {
        return String(value).padStart(length, '0')
    }

    const year = dateObj.getFullYear()
    const month = padStart(dateObj.getMonth() + 1)
    const day = padStart(dateObj.getDate())
    const hours = padStart(dateObj.getHours())
    const minutes = padStart(dateObj.getMinutes())
    const seconds = padStart(dateObj.getSeconds())

    return format
        .replace(/YYYY/g, year)
        .replace(/MM/g, month)
        .replace(/DD/g, day)
        .replace(/HH/g, hours)
        .replace(/mm/g, minutes)
        .replace(/ss/g, seconds)
}

/**
 * 将日期时间字符串转换为Date对象
 * @param {string} dateString - 日期时间字符串
 * @returns {Date} 转换后的Date对象
 */
export function parseDateTime(dateString) {
    if (!dateString) return new Date(NaN)

    // 处理IOS日期格式问题（将"-"替换为"/"）
    const isoDate = dateString.replace(/-/g, '/').replace(/T/g, ' ')
    return new Date(isoDate)
}

/**
 * 获取两个日期之间的天数差
 * @param {Date|string} startDate - 开始日期
 * @param {Date|string} endDate - 结束日期
 * @returns {number} 天数差
 */
export function getDaysBetween(startDate, endDate) {
    const start = startDate instanceof Date ? startDate : parseDateTime(startDate)
    const end = endDate instanceof Date ? endDate : parseDateTime(endDate)

    if (isNaN(start.getTime()) || isNaN(end.getTime())) return 0

    const timeDiff = Math.abs(end.getTime() - start.getTime())
    return Math.floor(timeDiff / (1000 * 60 * 60 * 24))
}

/**
 * 获取相对时间描述（如：3天前，1小时前等）
 * @param {Date|string} date - 日期时间
 * @returns {string} 相对时间描述
 */
export function getRelativeTime(date) {
    const now = new Date()
    const targetDate = date instanceof Date ? date : parseDateTime(date)

    if (isNaN(targetDate.getTime())) return ''

    const timeDiff = now.getTime() - targetDate.getTime()
    const seconds = Math.floor(timeDiff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    const months = Math.floor(days / 30)
    const years = Math.floor(months / 12)

    if (years > 0) return `${years}年前`
    if (months > 0) return `${months}个月前`
    if (days > 0) return `${days}天前`
    if (hours > 0) return `${hours}小时前`
    if (minutes > 0) return `${minutes}分钟前`
    return `${seconds}秒前`
}

/**
 * 获取当前季度的开始和结束日期
 * @returns {{start: Date, end: Date}} 季度开始和结束日期
 */
export function getCurrentQuarter() {
    const now = new Date()
    const currentMonth = now.getMonth()
    const quarterStartMonth = Math.floor(currentMonth / 3) * 3
    const quarterEndMonth = quarterStartMonth + 2

    const start = new Date(now.getFullYear(), quarterStartMonth, 1)
    const end = new Date(now.getFullYear(), quarterEndMonth + 1, 0)

    return { start, end }
}

/**
 * 检查日期是否在某个范围内
 * @param {Date} date - 要检查的日期
 * @param {Date} start - 范围开始日期
 * @param {Date} end - 范围结束日期
 * @returns {boolean} 是否在范围内
 */
export function isDateInRange(date, start, end) {
    const time = date.getTime()
    return time >= start.getTime() && time <= end.getTime()
}

/**
 * 添加天数到指定日期
 * @param {Date} date - 原始日期
 * @param {number} days - 要添加的天数
 * @returns {Date} 新日期
 */
export function addDays(date, days) {
    const result = new Date(date)
    result.setDate(result.getDate() + days)
    return result
}

/**
 * 获取本周的开始和结束日期
 * @returns {{start: Date, end: Date}} 周开始和结束日期
 */
export function getCurrentWeek() {
    const now = new Date()
    const day = now.getDay()
    const diff = now.getDate() - day + (day === 0 ? -6 : 1) // 周一是第一天

    const start = new Date(now.setDate(diff))
    start.setHours(0, 0, 0, 0)

    const end = new Date(start)
    end.setDate(start.getDate() + 6)
    end.setHours(23, 59, 59, 999)

    return { start, end }
}

export default {
    formatDateTime,
    parseDateTime,
    getDaysBetween,
    getRelativeTime,
    getCurrentQuarter,
    isDateInRange,
    addDays,
    getCurrentWeek
}
