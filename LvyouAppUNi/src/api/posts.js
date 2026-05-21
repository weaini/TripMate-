import request from './request'

// 获取动态列表
export const getPosts = (params) => {
  return request({
    url: '/posts',
    method: 'get',
    params,
  })
}

// 获取最新动态
export const getLatestPosts = (params) => {
  return request({
    url: '/posts/latest',
    method: 'get',
    params,
  })
}

// 获取热门动态
export const getHotPosts = (params) => {
  return request({
    url: '/posts/hot',
    method: 'get',
    params,
  })
}

// 获取动态详情
export const getPost = (id) => {
  return request({
    url: `/posts/${id}`,
    method: 'get',
  })
}

// 获取当前用户自己的动态（包含审核状态）
export const getMyPosts = (params) => {
  return request({
    url: '/posts/my',
    method: 'get',
    params,
  })
}

// 发布动态
export const createPost = (data) => {
  return request({
    url: '/posts',
    method: 'post',
    data,
  })
}

// 编辑动态
export const updatePost = (id, data) => {
  return request({
    url: `/posts/${id}`,
    method: 'put',
    data,
  })
}

// 上传动态图片
export const uploadPostImages = (postId, files) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request({
    url: `/posts/${postId}/images`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// 点赞动态
export const likePost = (id) => {
  return request({
    url: `/posts/${id}/like`,
    method: 'post',
  })
}

// 取消点赞
export const unlikePost = (id) => {
  return request({
    url: `/posts/${id}/like`,
    method: 'delete',
  })
}

// 删除动态
export const deletePost = (id) => {
  return request({
    url: `/posts/${id}`,
    method: 'delete',
  })
}
