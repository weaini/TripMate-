import request from './request'

// 获取攻略列表
export const getStrategies = (params) => {
  return request({
    url: '/strategies',
    method: 'get',
    params,
  })
}

// 获取攻略详情
export const getStrategy = (id) => {
  return request({
    url: `/strategies/${id}`,
    method: 'get',
  })
}

// 创建攻略
export const createStrategy = (data) => {
  return request({
    url: '/strategies',
    method: 'post',
    data,
  })
}

// 上传攻略图片
export const uploadStrategyImages = (strategyId, files) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request({
    url: `/strategies/${strategyId}/images`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// 点赞攻略
export const likeStrategy = (id) => {
  return request({
    url: `/strategies/${id}/like`,
    method: 'post',
  })
}

// 取消点赞
export const unlikeStrategy = (id) => {
  return request({
    url: `/strategies/${id}/like`,
    method: 'delete',
  })
}

// 删除攻略
export const deleteStrategy = (id) => {
  return request({
    url: `/strategies/${id}`,
    method: 'delete',
  })
}
