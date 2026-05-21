import axios from 'axios'

const API_BASE = process.env.VUE_APP_API_URL || '/api'

export function getAlarmList() {
    return axios.get(`${API_BASE}/alarm/list`)
}
export function getAlarmDetail(id) {
    return axios.get(`${API_BASE}/alarm/detail/${id}`)
}
