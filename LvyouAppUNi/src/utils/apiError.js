/**
 * 解析 axios 错误中的后端 ApiResponse（HTTP 4xx/5xx 或 200 但 success:false）
 */
export function getApiErrorMessage(error, fallback = '操作失败') {
  const d = error?.response?.data
  if (d && typeof d === 'object' && typeof d.message === 'string' && d.message.trim()) {
    return d.message.trim()
  }
  const msg = error?.message
  if (typeof msg === 'string' && msg && !msg.includes('status code')) {
    return msg
  }
  return fallback
}

export function getApiErrorCode(error) {
  const d = error?.response?.data
  if (d && typeof d === 'object' && d.code) {
    return d.code
  }
  return undefined
}

export function isPointsInsufficientError(error) {
  return getApiErrorCode(error) === 'POINTS_INSUFFICIENT'
}
