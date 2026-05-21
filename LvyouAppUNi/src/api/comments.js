import request from './request'

// 获取活动评论列表
export const getActivityComments = (activityId, params) => {
  return request({
    url: `/comments/activities/${activityId}`,
    method: 'get',
    params,
  })
}

// 创建评论
export const createComment = (activityId, data) => {
  return request({
    url: `/comments/activities/${activityId}`,
    method: 'post',
    data,
  })
}

// 删除评论
export const deleteComment = (commentId) => {
  return request({
    url: `/comments/${commentId}`,
    method: 'delete',
  })
}

// 点赞评论
export const likeComment = (commentId) => {
  return request({
    url: `/comments/${commentId}/like`,
    method: 'post',
  })
}

// 取消点赞
export const unlikeComment = (commentId) => {
  return request({
    url: `/comments/${commentId}/like`,
    method: 'delete',
  })
}

// 获取顶级评论（用于动态评论）
export const getTopLevelComments = (postId, params) => {
  return request({
    url: `/comments/posts/${postId}`,
    method: 'get',
    params,
  })
}

// 获取动态评论列表（包含所有层级和统计信息）
export const getPostCommentsWithStats = (postId, params) => {
  return request({
    url: `/comments/posts/${postId}/with-stats`,
    method: 'get',
    params,
  })
}

// 获取回复
export const getReplies = (commentId) => {
  return request({
    url: `/comments/${commentId}/replies`,
    method: 'get',
  })
}

// 添加评论（动态评论）
export const addComment = (postId, data) => {
  return request({
    url: `/comments/posts/${postId}`,
    method: 'post',
    data,
  })
}