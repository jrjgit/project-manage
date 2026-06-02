export const taskPrimaryViews = {
  pm: [
    { key: 'all', label: '全部任务', params: {} },
    { key: 'risk', label: '风险项', params: { focus: 'risk' } },
    { key: 'testing', label: '测试中', params: { status: 'testing' } }
  ],
  dev_lead: [
    { key: 'todo', label: '待受理', params: { mine: 'todo' } },
    { key: 'all', label: '全部任务', params: {} }
  ],
  dev: [
    { key: 'todo', label: '我的待办', params: { mine: 'todo' } },
    { key: 'mine', label: '我的参与', params: { mine: 'all' } },
    { key: 'all', label: '全部任务', params: {} }
  ],
  tester_lead: [
    { key: 'testing', label: '测试中', params: { status: 'testing' } },
    { key: 'all', label: '全部任务', params: {} }
  ],
  tester: [
    { key: 'todo', label: '我的待办', params: { mine: 'todo' } },
    { key: 'testing', label: '测试中', params: { status: 'testing' } },
    { key: 'all', label: '全部任务', params: {} }
  ]
}
