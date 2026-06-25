import request from './request'

export const getTasks = (params = {}) => request.get('/tasks', { params })
export const getTask = (id) => request.get(`/tasks/${id}`)
export const createTask = (data) => request.post('/tasks', data)
export const updateTask = (id, data) => request.put(`/tasks/${id}`, data)
export const changeTaskStatus = (id, data) => request.patch(`/tasks/${id}/status`, data)
export const acceptTask = (id) => request.post(`/tasks/${id}/accept`)
export const getTaskHistory = (id) => request.get(`/tasks/${id}/history`)
export const getTaskProgressHistory = (id) => request.get(`/tasks/${id}/progress-history`)
export const addTaskAssignee = (id, data) => request.post(`/tasks/${id}/assignees`, data)
export const removeTaskAssignee = (id, userId) => request.delete(`/tasks/${id}/assignees/${userId}`)
export const deleteTask = (id) => request.delete(`/tasks/${id}`)
