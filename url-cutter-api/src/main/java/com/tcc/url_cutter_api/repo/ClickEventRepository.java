package com.tcc.url_cutter_api.repo;

import com.tcc.url_cutter_api.model.ClickEvent;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ClickEventRepository extends ReactiveCrudRepository<ClickEvent, Long> {

    Flux<ClickEvent> findByUrlId(Long urlId);

    Flux<ClickEvent> findByIpAddress(String ipAddress);
}