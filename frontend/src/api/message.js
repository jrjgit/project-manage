import request from './request'

export function fetchMessages(page = 1, size = 20, type, isRead) {
  const params = { page, size }
  if (type && type !== 'all') params.type = type
  if (isRead !== null && isRead !== undefined) params.isRead = isRead
  return request.get('/messages', { params })
}

export function getUnreadCount() {
  return request.get('/messages/unread-count')
}

export function markRead(id) {
  return request.put(`/messages/${id}/read`)
}

export function markAllRead() {
  return request.put('/messages/read-all')
}
