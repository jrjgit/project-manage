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

// Document APIs
export const uploadRequirementDocument = (id, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/requirements/${id}/document`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const downloadRequirementDocument = (id) => request.get(`/requirements/${id}/document`, { responseType: 'blob' })
export const deleteRequirementDocument = (id) => request.delete(`/requirements/${id}/document`)

// Feature APIs
export const getFeatures = (requirementId) => request.get(`/requirements/${requirementId}/features`)
export const createFeature = (requirementId, data) => request.post(`/requirements/${requirementId}/features`, data)
export const updateFeature = (id, data) => request.put(`/features/${id}`, data)
export const changeFeatureStatus = (id, status) => request.patch(`/features/${id}/status`, { status })
export const deleteFeature = (id) => request.delete(`/features/${id}`)
