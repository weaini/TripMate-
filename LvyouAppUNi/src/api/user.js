import request from './request'

// 获取当前用户信息
export const getCurrentUser = () => {
  return request({
    url: '/auth/me',
    method: 'get',
  })
}

// 更新用户资料
export const updateUserProfile = (data) => {
  return request({
    url: '/users/profile',
    method: 'put',
    data,
  })
}

// 上传头像
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('avatar', file)
  
  return request({
    url: '/users/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// 修改密码
export const changePassword = (data) => {
  return request({
    url: '/users/password',
    method: 'put',
    data,
  })
}

// 获取用户动态
export const getUserPosts = (userId, params) => {
  return request({
    url: `/posts/user/${userId}`,
    method: 'get',
    params,
  })
}

// 获取用户关注列表
export const getUserFollowing = (userId, params) => {
  return request({
    url: `/users/${userId}/following`,
    method: 'get',
    params,
  })
}

// 获取用户粉丝列表
export const getUserFollowers = (userId, params) => {
  return request({
    url: `/users/${userId}/followers`,
    method: 'get',
    params,
  })
}

// 关注用户
export const followUser = (userId) => {
  return request({
    url: `/users/${userId}/follow`,
    method: 'post',
  })
}

// 取消关注用户
export const unfollowUser = (userId) => {
  return request({
    url: `/users/${userId}/follow`,
    method: 'delete',
  })
}

/** 当前用户是否已关注 userId */
export const getFollowStatus = (userId) => {
  return request({
    url: `/users/${userId}/follow-status`,
    method: 'get',
  })
}

// 删除动态 (临时添加，避免模块缓存问题)
export const deletePost = (id) => {
  return request({
    url: `/posts/${id}`,
    method: 'delete',
  })
}

// 获取当前用户积分记录（分页）
export const getPointsRecords = (params) => {
  return request({
    url: '/users/me/points-records',
    method: 'get',
    params,
  })
}
