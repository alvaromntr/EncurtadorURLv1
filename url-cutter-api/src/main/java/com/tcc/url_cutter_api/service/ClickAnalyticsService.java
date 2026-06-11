package com.tcc.url_cutter_api.service;

import com.tcc.url_cutter_api.dto.ClickAnalyticsResponse;
import com.tcc.url_cutter_api.repo.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClickAnalyticsService {

    private final ClickEventRepository clickEventRepository;

    public Flux<ClickAnalyticsResponse> getAnalytics(Long urlId) {

        return clickEventRepository
                .findByUrlId(urlId)
                .collectList()
                .flatMapMany(events -> {

                    Map<LocalDate, Long> grouped =
                            events.stream()
                                    .collect(
                                            Collectors.groupingBy(
                                                    event ->
                                                            event.getClickedAt()
                                                                    .toLocalDate(),
                                                    Collectors.counting()
                                            )
                                    );

                    return Flux.fromIterable(grouped.entrySet())
                            .sort(Map.Entry.comparingByKey())
                            .map(entry ->
                                    new ClickAnalyticsResponse(
                                            entry.getKey().toString(),
                                            entry.getValue()
                                    )
                            );
                });
    }
}