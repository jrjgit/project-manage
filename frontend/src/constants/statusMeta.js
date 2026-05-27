export const taskStatusMeta = {
  pending:    { label: '待受理',     tone: 'default', color: '#94a3b8' },
  developing: { label: '开发中',     tone: 'warning', color: '#f59e0b' },
  testing:    { label: '综合测试中', tone: 'info',    color: '#2563eb' },
  closed:     { label: '已完成',     tone: 'success', color: '#10b981' }
}

export const bugStatusMeta = {
  new: { label: '新建', tone: 'default', color: '#94a3b8' },
  assigned: { label: '待修复', tone: 'info', color: '#3b82f6' },
  fixing: { label: '修复中', tone: 'warning', color: '#f59e0b' },
  fixed: { label: '已修复', tone: 'success', color: '#10b981' },
  pending_verify: { label: '待验证', tone: 'info', color: '#2563eb' },
  closed: { label: '已关闭', tone: 'default', color: '#94a3b8' },
  reopened: { label: '重新打开', tone: 'error', color: '#ef4444' }
}

export const priorityMeta = {
  low: { label: '低', tone: 'default', color: '#94a3b8' },
  medium: { label: '中', tone: 'info', color: '#3b82f6' },
  high: { label: '高', tone: 'warning', color: '#f59e0b' },
  critical: { label: '紧急', tone: 'error', color: '#ef4444' }
}

export const severityMeta = {
  low: { label: '低', tone: 'default', color: '#94a3b8' },
  medium: { label: '中', tone: 'info', color: '#3b82f6' },
  high: { label: '高', tone: 'warning', color: '#f59e0b' },
  critical: { label: '紧急', tone: 'error', color: '#ef4444' }
}
