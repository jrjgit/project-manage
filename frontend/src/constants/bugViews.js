export const bugPrimaryViews = {
  pm: [
    { key: 'all', label: '全部 Bug', params: {} },
    { key: 'critical', label: '高风险', params: { severity: 'critical' } },
    { key: 'reopened', label: '重新打开', params: { status: 'reopened' } }
  ],
  dev_lead: [
    { key: 'group', label: '本组 Bug', params: { mine: 'group' } },
    { key: 'reopened', label: '重新打开', params: { status: 'reopened' } },
    { key: 'all', label: '全部 Bug', params: {} }
  ],
  dev: [
    { key: 'fix', label: '待我修复', params: { mine: 'fix' } },
    { key: 'reopened', label: '重新打开', params: { status: 'reopened' } },
    { key: 'all', label: '全部 Bug', params: {} }
  ],
  tester: [
    { key: 'verify', label: '待验证', params: { status: 'pending_verify' } },
    { key: 'all', label: '全部 Bug', params: {} }
  ]
}
