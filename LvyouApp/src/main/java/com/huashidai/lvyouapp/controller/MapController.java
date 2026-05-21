package com.huashidai.lvyouapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 地图/地点相关接口（后端代理调用高德）
 */
@RestController
@RequestMapping("/api/map")
public class MapController {

    private static final Logger log = LoggerFactory.getLogger(MapController.class);

    @Value("${amap.place-search-url:https://restapi.amap.com/v3/place/text}")
    private String placeSearchUrl;

    @Value("${amap.key:}")
    private String amapKey;

    /**
     * 预留扩展参数（如 sig、jscode 等），通过配置文件注入，默认可留空
     */
    @Value("${amap.extra-params:}")
    private String extraParamsConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 地点关键词搜索（前端统一调用此接口）
     */
    @GetMapping("/place-search")
    public ResponseEntity<?> searchPlaces(
            @RequestParam("keywords") String keywords,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "offset", defaultValue = "20") int offset,
            @RequestParam(value = "city", required = false) String city
    ) {
        if (amapKey == null || amapKey.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "服务器未配置高德 Web 服务 Key");
            error.put("code", "AMAP_KEY_NOT_CONFIGURED");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("key", amapKey);
            params.add("keywords", keywords);
            params.add("page", String.valueOf(page));
            params.add("offset", String.valueOf(offset));
            if (city != null && !city.isBlank()) {
                params.add("city", city);
            }

            // 解析额外参数配置（形如 key1=value1&key2=value2），便于灵活扩展
            if (extraParamsConfig != null && !extraParamsConfig.isBlank()) {
                String[] pairs = extraParamsConfig.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2 && !kv[0].isBlank() && !kv[1].isBlank()) {
                        params.add(kv[0].trim(), kv[1].trim());
                    }
                }
            }

            String url = placeSearchUrl + "?" + toQueryString(params);

            Map<?, ?> amapResp = restTemplate.getForObject(url, Map.class);
            return ResponseEntity.ok(amapResp);
        } catch (Exception e) {
            log.error("调用高德地点搜索失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "调用地图服务失败，请稍后重试");
            error.put("code", "AMAP_CALL_FAILED");
            return ResponseEntity.badRequest().body(error);
        }
    }

    private String toQueryString(MultiValueMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, java.util.List<String>> entry : params.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(key).append("=").append(value);
            }
        }
        return sb.toString();
    }
}

