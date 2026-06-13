import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)

  async function refreshUnreadCount() {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.count
    } catch {}
  }

  return {
    unreadCount,
    refreshUnreadCount
  }
})
