import { defineStore } from 'pinia'
import { ref, onUnmounted } from 'vue'
import { getUnreadCount } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  let timer = null

  async function refreshUnreadCount() {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.count
    } catch {
      // ignore
    }
  }

  function startPolling() {
    refreshUnreadCount()
    timer = setInterval(refreshUnreadCount, 30000)
  }

  function stopPolling() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  return {
    unreadCount,
    refreshUnreadCount,
    startPolling,
    stopPolling
  }
})
