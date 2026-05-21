// 高德地图配置（仅前端调用 Web Service）
// TODO: 请在这里填写你自己的高德 Web 服务 key
// 申请地址见官网「Web服务 API」文档
export const AMAP_WEB_SERVICE_KEY = '93582df28abd99e6354450c2a0ef89e9'

// 地点搜索接口地址：
// - 现在统一改为调用后端代理：/api/map/place-search
// - 后端再去请求高德官方接口
export const AMAP_PLACE_SEARCH_URL = '/api/map/place-search'

// 额外的查询参数（可选）
// - 用于支持你在高德控制台开启的安全机制，比如：
//   - 你自己的后端网关要求的额外参数
// - 默认留空，前端不会多带任何安全相关参数
export const AMAP_EXTRA_PARAMS = {
  // 在此对象中按需追加自定义参数，例如：
  // sig: '',
  // jscode: '',
}

