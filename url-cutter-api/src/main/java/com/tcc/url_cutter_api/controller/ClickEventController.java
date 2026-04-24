package com.tcc.url_cutter_api.controller;

import com.tcc.url_cutter_api.model.ClickEvent;
import com.tcc.url_cutter_api.service.ClickEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/clicks")
@RequiredArgsConstructor
public class ClickEventController {

    private final ClickEventService clickEventService;

    // 🔹 Registrar clique
    @PostMapping
    public Mono<ClickEvent> registerClick(@RequestParam Long urlId,
                                          @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                          @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {

        // fallback simples caso não venha header
        String ip = (ipAddress != null) ? ipAddress : "unknown";

        return clickEventService.registerClick(urlId, ip, userAgent);
    }

    // 🔹 Listar cliques por URL
    @GetMapping("/url/{urlId}")
    public Flux<ClickEvent> getClicksByUrlId(@PathVariable Long urlId) {
        return clickEventService.getClicksByUrlId(urlId);
    }

    // 🔹 Buscar cliques por IP
    @GetMapping("/ip/{ipAddress}")
    public Flux<ClickEvent> getClicksByIp(@PathVariable String ipAddress) {
        return clickEventService.getClicksByIp(ipAddress);
    }

    // 🔹 Listar todos
    @GetMapping
    public Flux<ClickEvent> getAllClicks() {
        return clickEventService.getAllClicks();
    }

    // 🔹 Buscar por ID
    @GetMapping("/{id}")
    public Mono<ClickEvent> getById(@PathVariable Long id) {
        return clickEventService.getById(id);
    }

    // 🔹 Deletar
    @DeleteMapping("/{id}")
    public Mono<Void> deleteById(@PathVariable Long id) {
        return clickEventService.deleteById(id);
    }
}