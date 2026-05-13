import request from './request'

export const getProjects = () => request.get('/projects')
export const getProject = (id) => request.get(`/projects/${id}`)
export const createProject = (data) => request.post('/projects', data)
export const updateProject = (id, data) => request.put(`/projects/${id}`, data)
export const deleteProject = (id) => request.delete(`/projects/${id}`)
