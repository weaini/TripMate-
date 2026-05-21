import request from './request'

export function fetchNotifications(params = { page: 0, size: 10 }) {
  return request({ url: '/notifications', method: 'get', params })
}

export function fetchUnreadCount() {
  return request({ url: '/notifications/unread-count', method: 'get' })
}

export function markRead(ids = []) {
  return request({ url: '/notifications/mark-read', method: 'post', data: ids })
}

export function markAllRead() {
  return request({ url: '/notifications/read-all', method: 'post' })
}

export function createNotification(payload) {
  return request({ url: '/notifications/create', method: 'post', params: payload })
}

// preferences
export function getMyNotificationPreference() {
  return request({ url: '/notification-preferences/me', method: 'get' })
}

export function updateMyNotificationPreference(pref) {
  return request({ url: '/notification-preferences/me', method: 'put', data: pref })
}

