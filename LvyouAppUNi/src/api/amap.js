import { AMAP_WEB_SERVICE_KEY, AMAP_PLACE_SEARCH_URL, AMAP_EXTRA_PARAMS } from '@/config/amap'

/**
 * 使用高德地点搜索服务，根据关键字查询地点列表
 * 仅做最简单封装，调用方负责防抖与错误展示
 */
export async function searchPlacesByKeyword(keyword, options = {}) {
  if (!keyword || !keyword.trim()) {
    return []
  }

  if (!AMAP_PLACE_SEARCH_URL) {
    throw new Error('地图服务地址未配置')
  }

  const {
    page = 1,
    offset = 20,
    city = '', // 可选：指定城市，留空表示全国
  } = options

  const params = new URLSearchParams({
    // 后端已负责携带 key，这里只传业务参数
    keywords: keyword.trim(),
    page: String(page),
    offset: String(offset),
    city,
  })

  // 从配置中附加自定义参数（如你确实需要在前端带 sig/jscode 等）
  if (AMAP_EXTRA_PARAMS && typeof AMAP_EXTRA_PARAMS === 'object') {
    Object.entries(AMAP_EXTRA_PARAMS).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') {
        params.append(k, String(v))
      }
    })
  }

  const url = `${AMAP_PLACE_SEARCH_URL}?${params.toString()}`

  const resp = await fetch(url)
  let data = null
  try {
    data = await resp.json()
  } catch {
    data = null
  }

  if (!resp.ok) {
    const message = data?.message || `请求高德接口失败: ${resp.status}`
    throw new Error(message)
  }

  // 兼容后端统一响应结构：{ success, data, message }
  if (data && typeof data === 'object' && Object.prototype.hasOwnProperty.call(data, 'success')) {
    if (!data.success) {
      throw new Error(data.message || '地图服务调用失败')
    }
    data = data.data
  }

  if (!data || data.status !== '1') {
    const info = data?.info || data?.message || '高德接口返回错误'
    // 直接透传高德返回的信息，方便你根据需要调整配置/参数
    throw new Error(info)
  }

  // data.pois 为地点数组
  return Array.isArray(data.pois) ? data.pois : []
}

