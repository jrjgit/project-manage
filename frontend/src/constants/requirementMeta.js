export const requirementStatusMeta = {
  planned:          { label: '未开始',       tone: 'default', color: '#909399' },
  assigned:         { label: '已分配组长',   tone: 'info',    color: '#6366f1' },
  in_progress:      { label: '开发中',       tone: 'info',    color: '#2080f0' },
  integration_test: { label: '综合测试',     tone: 'warning', color: '#f59e0b' },
  business_test:    { label: '业务测试',     tone: 'warning', color: '#f0a020' },
  pending_release:  { label: '待发布',       tone: 'error',   color: '#d03050' },
  released:         { label: '已发布',       tone: 'success', color: '#18a058' },
  closed:           { label: '已关闭',       tone: 'default', color: '#909399' },
}

export const requirementSourceMeta = {
  internal: { label: '内部需求' },
  external: { label: '客户需求' },
}
