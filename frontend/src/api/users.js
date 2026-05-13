import request from './request'

export const getUsers = () => request.get('/users')
export const getUser = (id) => request.get(`/users/${id}`)
export const updateUserRole = (id, data) => request.put(`/users/${id}/role`, data)
