export const bugPrimaryViews = {
  pm: [
    { key: 'all', label: '全部 Bug', params: {} },
  ],
  dev_lead: [
    { key: 'mine', label: '待我修复', params: { mine: 'fix' } },
    { key: 'all', label: '全部 Bug', params: {} },
  ],
  dev: [
    { key: 'mine', label: '待我修复', params: { mine: 'fix' } },
    { key: 'all', label: '全部 Bug', params: {} },
  ],
  tester: [
    { key: 'verify', label: '待我验证', params: { mine: 'verify' } },
    { key: 'all', label: '全部 Bug', params: {} },
  ]
}
