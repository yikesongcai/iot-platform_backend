import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAlarmList } from '@/api/alarm'

export const useAlarmStore = defineStore('alarm', () => {
    const alarmList = ref([])
    const loading = ref(false)
    const error = ref(null)

    const fetchAlarmList = async () => {
        try {
            loading.value = true
            error.value = null
            const response = await getAlarmList()
            alarmList.value = response.data.data || []
        } catch (err) {
            error.value = err.message || '获取告警列表失败'
            console.error('获取告警列表失败:', err)
        } finally {
            loading.value = false
        }
    }

    return {
        alarmList,
        loading,
        error,
        fetchAlarmList
    }
})
