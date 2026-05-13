import request from './request'

export const getGroups = () => request.get('/groups')
export const getGroup = (id) => request.get(`/groups/${id}`)
export const createGroup = (data) => request.post('/groups', data)
export const updateGroup = (id, data) => request.put(`/groups/${id}`, data)
export const deleteGroup = (id) => request.delete(`/groups/${id}`)
export const addMember = (groupId, data) => request.post(`/groups/${groupId}/members`, data)
export const removeMember = (groupId, userId) => request.delete(`/groups/${groupId}/members/${userId}`)
export const getMyTeam = () => request.get('/groups/my-team')
