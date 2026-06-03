import request from './request'

export const getRevenueStats = (year) => request.get('/statistics/revenue', { params: { year } })
export const getPerformanceStats = (year, month) => request.get('/statistics/performance', { params: { year, month } })
export const getDashboard = () => request.get('/dashboard')
export const getDeveloperDashboard = () => request.get('/dashboard/developer')
export const getTesterDashboard = () => request.get('/dashboard/tester')
