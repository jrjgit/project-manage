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
  tester_lead: [
    { key: 'verify', label: '待验证', params: { mine: 'verify' } },
    { key: 'all', label: '全部 Bug', params: {} },
    { key: 'critical', label: '高风险', params: { severity: 'critical' } }
  ],
  tester: [
    { key: 'verify', label: '待我验证', params: { mine: 'verify' } },
    { key: 'created', label: '我创建的', params: { mine: 'created' } },
    { key: 'all', label: '全部 Bug', params: {} }
  ]
}
