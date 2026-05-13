export const taskPrimaryViews = {
  pm: [
    { key: 'all', label: '全部任务', params: {} },
    { key: 'risk', label: '风险项', params: { focus: 'risk' } },
    { key: 'testing', label: '测试中', params: { status: 'testing' } }
  ],
  dev_lead: [
    { key: 'group', label: '本组任务', params: { mine: 'group' } },
    { key: 'todo', label: '待接手', params: { mine: 'todo' } },
    { key: 'all', label: '全部任务', params: {} }
  ],
  dev: [
    { key: 'todo', label: '我的待办', params: { mine: 'todo' } },
    { key: 'mine', label: '我的参与', params: { mine: 'all' } },
    { key: 'all', label: '全部任务', params: {} }
  ],
  tester_lead: [
    { key: 'verify-pool', label: '测试池', params: { mine: 'verify-pool' } },
    { key: 'active', label: '测试中', params: { status: 'testing' } },
    { key: 'all', label: '全部任务', params: {} }
  ],
  tester: [
    { key: 'todo', label: '我的待办', params: { mine: 'todo' } },
    { key: 'mine', label: '我的参与', params: { mine: 'all' } },
    { key: 'all', label: '全部任务', params: {} }
  ]
}
