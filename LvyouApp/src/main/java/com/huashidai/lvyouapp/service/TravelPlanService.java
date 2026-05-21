package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.TravelPlanCreateRequest;
import com.huashidai.lvyouapp.dto.TravelPlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelPlanService {
    TravelPlanResponse createPlan(TravelPlanCreateRequest request);
    Page<TravelPlanResponse> getMyPlans(Pageable pageable);
    TravelPlanResponse getPlan(Long id);
    void sharePlan(Long planId);
}


