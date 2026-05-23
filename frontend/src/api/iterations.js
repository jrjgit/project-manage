import request from './request'

export const getIterations = () => request.get('/iterations')
export const createIteration = (data) => request.post('/iterations', data)
export const updateIteration = (id, data) => request.put(`/iterations/${id}`, data)
export const deleteIteration = (id) => request.delete(`/iterations/${id}`)
