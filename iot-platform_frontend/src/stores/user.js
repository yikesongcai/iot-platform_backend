import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isGuest = computed(() => !token.value)

  function setAuth(authToken, authUser) {
    token.value = authToken
    user.value = authUser
    localStorage.setItem('token', authToken)
    localStorage.setItem('user', JSON.stringify(authUser))
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isGuest, setAuth, clearAuth }
})
