export const taskStatusMeta = {
  pending:    { label: '待受理',     tone: 'default', color: '#94a3b8' },
  developing: { label: '开发中',     tone: 'warning', color: '#f59e0b' },
  testing:    { label: '综合测试中', tone: 'info',    color: '#2563eb' },
  closed:     { label: '已完成',     tone: 'success', color: '#10b981' }
}

export const bugStatusMeta = {
  unfixed:   { label: '未修复',         tone: 'warning', color: '#f59e0b' },
  fixed:     { label: '已修复',         tone: 'success', color: '#10b981' },
  not_a_bug: { label: '开发确认非Bug',  tone: 'info',    color: '#6366f1' },
  closed:    { label: '已关闭',         tone: 'default', color: '#94a3b8' }
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
