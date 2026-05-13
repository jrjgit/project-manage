export function compactParams(params = {}) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
  )
}

export function buildViewQuery(view) {
  return compactParams({
    view: view?.key,
    ...(view?.params || {})
  })
}

export function pickActiveView(views = [], routeQuery = {}) {
  if (!views.length) return null
  return views.find((view) => view.key === routeQuery.view) || views[0]
}

export function mergeViewParams(view, extraParams = {}) {
  return compactParams({
    ...(view?.params || {}),
    ...extraParams
  })
}
