package com.tcc.url_cutter_api.service;

import com.tcc.url_cutter_api.model.ClickEvent;
import com.tcc.url_cutter_api.repo.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClickEventService {

    private final ClickEventRepository clickEventRepository;

    // 🔹 Registrar um novo clique
    public Mono<ClickEvent> registerClick(Long urlId,
                                          String ipAddress,
                                          String userAgent) {

        ClickEvent event = ClickEvent.builder()
                .urlId(urlId)
                .clickedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        return clickEventRepository.save(event);
    }

    public Mono<Void> registerClick(
            Long urlId,
            String ipAddress,
            String userAgent,
            String referer
    ) {

        ClickEvent event = ClickEvent.builder()
                .urlId(urlId)
                .clickedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .referer(referer)
                .build();

        return clickEventRepository.save(event)
                .then();
    }

    // 🔹 Listar cliques de uma URL específica
    public Flux<ClickEvent> getClicksByUrlId(Long urlId) {
        return clickEventRepository.findByUrlId(urlId);
    }

    // 🔹 Buscar cliques por IP (útil para segurança/analytics)
    public Flux<ClickEvent> getClicksByIp(String ipAddress) {
        return clickEventRepository.findByIpAddress(ipAddress);
    }

    // 🔹 Listar todos os eventos
    public Flux<ClickEvent> getAllClicks() {
        return clickEventRepository.findAll();
    }

    // 🔹 Buscar evento por ID
    public Mono<ClickEvent> getById(Long id) {
        return clickEventRepository.findById(id);
    }

    // 🔹 Remover evento
    public Mono<Void> deleteById(Long id) {
        return clickEventRepository.deleteById(id);
    }
}