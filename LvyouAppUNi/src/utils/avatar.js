/**
 * 头像URL处理工具函数
 */

/**
 * 获取完整的头像URL
 * @param {string} avatarUrl - 头像URL
 * @returns {string} 完整的头像URL
 */
export const getFullAvatarUrl = (avatarUrl) => {
  if (!avatarUrl) return ''
  
  // 如果已经是完整URL，直接返回
  if (avatarUrl.startsWith('http') || avatarUrl.startsWith('data:')) {
    return avatarUrl
  }
  
  // 如果是相对路径，添加后端服务器地址
  if (avatarUrl.startsWith('/uploads/')) {
    const baseUrl = 'http://localhost:8081'
    return `${baseUrl}${avatarUrl}`
  }
  
  return avatarUrl
}

/**
 * 获取用户头像URL
 * @param {object} user - 用户对象
 * @returns {string} 头像URL
 */
export const getUserAvatarUrl = (user) => {
  if (!user || !user.avatar) return ''
  return getFullAvatarUrl(user.avatar)
}
