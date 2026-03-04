import request from '@/utils/request'

export function savePomodoro(data) {
  return request({
    url: '/pomodoro',
    method: 'post',
    data
  })
}

export function getTodayPomodoroCount(userId) {
  return request({
    url: '/pomodoro/today-count',
    method: 'get',
    params: { userId }
  })
}

export function getPomodoroRecords(userId) {
  return request({
    url: '/pomodoro',
    method: 'get',
    params: { userId }
  })
}
