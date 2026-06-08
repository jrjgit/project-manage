import request from './request'

export const getBugs = (params = {}) => request.get('/bugs', { params })
export const getBug = (id) => request.get(`/bugs/${id}`)
export const createBug = (data) => request.post('/bugs', data)
export const updateBug = (id, data) => request.put(`/bugs/${id}`, data)
export const changeBugStatus = (id, data) => request.patch(`/bugs/${id}/status`, data)
export const getBugHistory = (id) => request.get(`/bugs/${id}/history`)

export const uploadBugImage = (id, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/bugs/${id}/image`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const getBugImages = (id) => request.get(`/bugs/${id}/images`)

export const downloadBugImage = (id, imageId) =>
  request.get(`/bugs/${id}/images/${imageId}`, { responseType: 'blob' })

export const deleteBugImageById = (id, imageId) =>
  request.delete(`/bugs/${id}/images/${imageId}`)
