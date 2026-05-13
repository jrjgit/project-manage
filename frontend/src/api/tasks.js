import request from './request'

export const getTasks = (params = {}) => request.get('/tasks', { params })
export const getTask = (id) => request.get(`/tasks/${id}`)
export const createTask = (data) => request.post('/tasks', data)
export const updateTask = (id, data) => request.put(`/tasks/${id}`, data)
export const changeTaskStatus = (id, data) => request.patch(`/tasks/${id}/status`, data)
export const getTaskHistory = (id) => request.get(`/tasks/${id}/history`)
