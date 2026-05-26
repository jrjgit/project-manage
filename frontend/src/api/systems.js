import request from './request'

export const getSystems = () => request.get('/systems')
export const getSystem = (id) => request.get(`/systems/${id}`)
export const createSystem = (data) => request.post('/systems', data)
export const updateSystem = (id, data) => request.put(`/systems/${id}`, data)
export const deleteSystem = (id) => request.delete(`/systems/${id}`)
