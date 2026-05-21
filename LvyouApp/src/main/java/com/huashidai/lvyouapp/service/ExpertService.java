package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.ExpertApplicationRequest;
import com.huashidai.lvyouapp.dto.ExpertApplicationResponse;

/**
 * 达人服务接口
 */
public interface ExpertService {
    
    /**
     * 申请成为旅游达人
     */
    void applyForExpert(ExpertApplicationRequest request);
    
    /**
     * 获取我的达人申请状态
     */
    ExpertApplicationResponse getMyApplication();
    
    /**
     * 取消达人申请
     */
    void cancelApplication();
}
