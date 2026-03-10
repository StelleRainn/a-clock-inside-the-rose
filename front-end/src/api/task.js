import request from '@/utils/request'

export function getTasks(userId) {
  return request({
    url: '/tasks',
    method: 'get',
    params: { userId }
  })
}

export function createTask(data) {
  return request({
    url: '/tasks',
    method: 'post',
    data
  })
}

export function updateTask(id, data) {
  return request({
    url: `/tasks/${id}`,
    method: 'put',
    data
  })
}

export function deleteTask(id) {
  return request({
    url: `/tasks/${id}`,
    method: 'delete'
  })
}

export function reorderTasks(taskIds) {
  return request({
    url: '/tasks/reorder',
    method: 'post',
    data: taskIds
  })
}
