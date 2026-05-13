import request from './request'

export const getBugs = (params = {}) => request.get('/bugs', { params })
export const getBug = (id) => request.get(`/bugs/${id}`)
export const createBug = (data) => request.post('/bugs', data)
export const updateBug = (id, data) => request.put(`/bugs/${id}`, data)
export const changeBugStatus = (id, data) => request.patch(`/bugs/${id}/status`, data)
export const getBugHistory = (id) => request.get(`/bugs/${id}/history`)
