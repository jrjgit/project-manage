import request from './request'

export const getRequirements = (params) => request.get('/requirements', { params })
export const getRequirement = (id) => request.get(`/requirements/${id}`)
export const createRequirement = (data) => request.post('/requirements', data)
export const updateRequirement = (id, data) => request.put(`/requirements/${id}`, data)
export const changeRequirementStatus = (id, status) => request.patch(`/requirements/${id}/status`, { status })
export const deleteRequirement = (id) => request.delete(`/requirements/${id}`)
export const getOpsRequirements = () => request.get('/requirements/ops')
export const getProjectRequirements = () => request.get('/requirements/project')
export const addToRelease = (id, iterationId) => request.put(`/requirements/${id}/release`, { iteration_id: iterationId })
export const removeFromRelease = (id) => request.delete(`/requirements/${id}/release`)
