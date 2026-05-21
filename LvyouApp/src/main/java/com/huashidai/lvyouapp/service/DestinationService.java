package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.entity.Destination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DestinationService {
    Destination getDestination(Long id);
    Page<Destination> searchDestinations(String keyword, Pageable pageable);
    Destination getOrCreateDestination(String name, Double latitude, Double longitude);
}


