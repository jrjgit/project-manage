import request from './request'

export const getDictionaries = (type) => request.get('/dictionaries', { params: type ? { type } : {} })
export const createDictionary = (data) => request.post('/dictionaries', data)
export const updateDictionary = (id, data) => request.put(`/dictionaries/${id}`, data)
export const deleteDictionary = (id) => request.delete(`/dictionaries/${id}`)
