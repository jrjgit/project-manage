import request from './request'

export const getUsers = () => request.get('/users')
export const getUser = (id) => request.get(`/users/${id}`)
export const updateUserRole = (id, data) => request.put(`/users/${id}/role`, data)
export const createUser = (data) => request.post('/auth/register', data)
export const updateUser = (id, data) => request.put(`/users/${id}`, data)
export const deleteUser = (id) => request.delete(`/users/${id}`)
export const getUserWorkload = () => request.get('/users/workload')
export const changeUserPassword = (id, password) => request.put(`/users/${id}/password`, { password })
