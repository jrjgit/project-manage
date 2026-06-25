import request from './request'

export const getRevenueStats = (params) => request.get('/statistics/revenue', { params })
export const getPerformanceStats = (params) => request.get('/statistics/performance', { params })
export const getDashboard = () => request.get('/dashboard')
export const getDeveloperDashboard = () => request.get('/dashboard/developer')
export const getTesterDashboard = () => request.get('/dashboard/tester')
export const getProgressAnomalies = () => request.get('/dashboard/progress-anomalies')
