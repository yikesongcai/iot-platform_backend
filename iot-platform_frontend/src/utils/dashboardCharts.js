import * as echarts from 'echarts'

/**
 * 初始化设备类型图表
 */
export const initDeviceTypeChart = (chartDom, deviceTypes) => {
    if (!chartDom) return null

    const chart = echarts.init(chartDom)
    const option = {
        tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        series: [{
            name: '设备类型',
            type: 'pie',
            radius: ['50%', '70%'],
            data: deviceTypes
        }]
    }

    chart.setOption(option)
    return chart
}

/**
 * 初始化消息趋势图表
 */
export const initMessageTrendChart = (chartDom, trendData) => {
    if (!chartDom || !trendData?.length) return null

    const chart = echarts.init(chartDom)
    const option = {
        tooltip: {
            trigger: 'axis',
            formatter: params => {
                const data = trendData[params[0].dataIndex]
                return `
          <div>时间: ${data.hour}</div>
          <div>总消息: ${data.messageCount}</div>
          <div>告警数: ${data.warnCount}</div>
        `
            }
        },
        xAxis: {
            type: 'category',
            data: trendData.map(item => item.hour)
        },
        yAxis: { type: 'value' },
        series: [
            {
                name: '总消息',
                type: 'line',
                data: trendData.map(item => item.messageCount),
                lineStyle: { color: '#2e7d32', width: 3 }
            },
            {
                name: '告警消息',
                type: 'line',
                data: trendData.map(item => item.warnCount),
                lineStyle: { color: '#f56c6c', width: 3 }
            }
        ]
    }

    chart.setOption(option)
    return chart
}
