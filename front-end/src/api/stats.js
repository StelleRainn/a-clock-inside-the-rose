import request from '@/utils/request'

export function getDailyFocusStats(userId) {
  return request({
    url: '/stats/focus-daily',
    method: 'get',
    params: { userId }
  })
}

export function getTaskStatusStats(userId) {
  return request({
    url: '/stats/task-status',
    method: 'get',
    params: { userId }
  })
}
