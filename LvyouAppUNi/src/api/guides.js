import request from './request'

/**
 * 攻略相关API
 */

// 创建攻略
export const createGuide = (data) => {
  return request({
    url: '/guides',
    method: 'post',
    data
  })
}

// 更新攻略
export const updateGuide = (id, data) => {
  return request({
    url: `/guides/${id}`,
    method: 'put',
    data
  })
}

// 删除攻略
export const deleteGuide = (id) => {
  return request({
    url: `/guides/${id}`,
    method: 'delete'
  })
}

// 获取攻略详情
export const getGuideById = (id) => {
  return request({
    url: `/guides/${id}`,
    method: 'get'
  })
}

// 获取攻略列表
export const getGuides = (params) => {
  return request({
    url: '/guides',
    method: 'get',
    params
  })
}

// 搜索攻略
export const searchGuides = (params) => {
  return request({
    url: '/guides/search',
    method: 'get',
    params
  })
}

// 获取我的攻略
export const getMyGuides = (params) => {
  return request({
    url: '/guides/my',
    method: 'get',
    params
  })
}

// 点赞/取消点赞攻略
export const toggleGuideLike = (id) => {
  return request({
    url: `/guides/${id}/like`,
    method: 'post'
  })
}

// 上传攻略封面图片
export const uploadGuideCover = (id, file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: `/guides/${id}/cover`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 创建攻略评论
export const createGuideComment = (guideId, data) => {
  return request({
    url: `/guides/${guideId}/comments`,
    method: 'post',
    data
  })
}

// 获取攻略评论
export const getGuideComments = (guideId, params) => {
  return request({
    url: `/guides/${guideId}/comments`,
    method: 'get',
    params
  })
}

// 点赞/取消点赞评论
export const toggleCommentLike = (commentId) => {
  return request({
    url: `/guides/comments/${commentId}/like`,
    method: 'post'
  })
}

// 删除评论
export const deleteGuideComment = (commentId) => {
  return request({
    url: `/guides/comments/${commentId}`,
    method: 'delete'
  })
}

// 获取攻略统计信息
export const getGuideStats = () => {
  return request({
    url: '/guides/stats',
    method: 'get'
  })
}
