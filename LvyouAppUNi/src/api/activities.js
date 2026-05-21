import request from './request'

// 获取活动列表
export const getActivities = (params) => {
  return request({
    url: '/activities',
    method: 'get',
    params,
  })
}

// 获取活动详情
export const getActivity = (id) => {
  return request({
    url: `/activities/${id}`,
    method: 'get',
  })
}

// 创建活动
export const createActivity = (data) => {
  return request({
    url: '/activities',
    method: 'post',
    data,
  })
}

// 更新活动
export const updateActivity = (id, data) => {
  return request({
    url: `/activities/${id}`,
    method: 'put',
    data,
  })
}

// 上传活动图片
export const uploadActivityImages = (activityId, files) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request({
    url: `/activities/${activityId}/images`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// 报名活动
export const joinActivity = (id, data) => {
  return request({
    url: `/activities/${id}/join`,
    method: 'post',
    data,
  })
}

// 取消报名
export const cancelJoinActivity = (id) => {
  return request({
    url: `/activities/${id}/join`,
    method: 'delete',
  })
}

// 审核报名
export const approveParticipant = (activityId, participantId) => {
  return request({
    url: `/activities/${activityId}/participants/${participantId}/approve`,
    method: 'post',
  })
}

// 拒绝报名
export const rejectParticipant = (activityId, participantId, reason) => {
  return request({
    url: `/activities/${activityId}/participants/${participantId}/reject`,
    method: 'post',
    data: { reason },
  })
}

// 获取我的活动
export const getMyActivities = (params) => {
  return request({
    url: '/activities/my',
    method: 'get',
    params,
  })
}

// 获取活动参与者列表
export const getActivityParticipants = (activityId, params) => {
  return request({
    url: `/activities/${activityId}/participants`,
    method: 'get',
    params,
  })
}

// 获取我的报名记录
export const getMyParticipations = (params) => {
  return request({
    url: '/activities/my-participations',
    method: 'get',
    params,
  })
}

// 检查用户参与状态
export const checkParticipation = (activityId) => {
  return request({
    url: `/activities/${activityId}/participation`,
    method: 'get',
  })
}
