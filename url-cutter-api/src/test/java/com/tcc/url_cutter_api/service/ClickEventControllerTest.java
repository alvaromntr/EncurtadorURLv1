package com.tcc.url_cutter_api.service;

import com.tcc.url_cutter_api.controller.ClickEventController;
import com.tcc.url_cutter_api.model.ClickEvent;
import com.tcc.url_cutter_api.service.ClickEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClickEventController")
class ClickEventControllerTest {

    @Mock
    private ClickEventService clickEventService;

    @InjectMocks
    private ClickEventController clickEventController;

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private static final Long   URL_ID     = 1L;
    private static final Long   EVENT_ID   = 10L;
    private static final String IP         = "192.168.0.1";
    private static final String USER_AGENT = "Mozilla/5.0";

    private ClickEvent buildEvent(Long id, Long urlId, String ip) {
        return ClickEvent.builder()
                .id(id)
                .urlId(urlId)
                .clickedAt(LocalDateTime.now())
                .ipAddress(ip)
                .userAgent(USER_AGENT)
                .referer("https://referer.com")
                .build();
    }

    // =======================================================================
    // POST /api/clicks  — registerClick
    // =======================================================================

    @Nested
    @DisplayName("POST /api/clicks")
    class RegisterClick {

        @Test
        @DisplayName("deve registrar clique com IP e User-Agent fornecidos")
        void shouldRegisterClickWithProvidedHeaders() {
            ClickEvent saved = buildEvent(EVENT_ID, URL_ID, IP);
            when(clickEventService.registerClick(URL_ID, IP, USER_AGENT))
                    .thenReturn(Mono.just(saved));

            StepVerifier.create(clickEventController.registerClick(URL_ID, USER_AGENT, IP))
                    .assertNext(event -> {
                        assertThat(event.getId()).isEqualTo(EVENT_ID);
                        assertThat(event.getUrlId()).isEqualTo(URL_ID);
                        assertThat(event.getIpAddress()).isEqualTo(IP);
                        assertThat(event.getUserAgent()).isEqualTo(USER_AGENT);
                    })
                    .verifyComplete();

            verify(clickEventService).registerClick(URL_ID, IP, USER_AGENT);
        }

        @Test
        @DisplayName("deve usar 'unknown' como IP quando X-Forwarded-For não é fornecido")
        void shouldFallbackToUnknownWhenIpHeaderIsMissing() {
            ClickEvent saved = buildEvent(EVENT_ID, URL_ID, "unknown");
            when(clickEventService.registerClick(URL_ID, "unknown", USER_AGENT))
                    .thenReturn(Mono.just(saved));

            // ipAddress = null simula ausência do header
            StepVerifier.create(clickEventController.registerClick(URL_ID, USER_AGENT, null))
                    .assertNext(event ->
                            assertThat(event.getIpAddress()).isEqualTo("unknown"))
                    .verifyComplete();

            verify(clickEventService).registerClick(URL_ID, "unknown", USER_AGENT);
        }

        @Test
        @DisplayName("deve aceitar User-Agent nulo")
        void shouldAcceptNullUserAgent() {
            ClickEvent saved = buildEvent(EVENT_ID, URL_ID, IP);
            when(clickEventService.registerClick(URL_ID, IP, null))
                    .thenReturn(Mono.just(saved));

            StepVerifier.create(clickEventController.registerClick(URL_ID, null, IP))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(clickEventService).registerClick(URL_ID, IP, null);
        }

        @Test
        @DisplayName("deve propagar erro do service")
        void shouldPropagateServiceError() {
            when(clickEventService.registerClick(URL_ID, IP, USER_AGENT))
                    .thenReturn(Mono.error(new RuntimeException("Falha ao registrar")));

            StepVerifier.create(clickEventController.registerClick(URL_ID, USER_AGENT, IP))
                    .expectErrorMessage("Falha ao registrar")
                    .verify();
        }
    }

    // =======================================================================
    // GET /api/clicks/url/{urlId}  — getClicksByUrlId
    // =======================================================================

    @Nested
    @DisplayName("GET /api/clicks/url/{urlId}")
    class GetClicksByUrlId {

        @Test
        @DisplayName("deve retornar todos os cliques da URL")
        void shouldReturnClicksForUrl() {
            ClickEvent e1 = buildEvent(1L, URL_ID, "10.0.0.1");
            ClickEvent e2 = buildEvent(2L, URL_ID, "10.0.0.2");
            when(clickEventService.getClicksByUrlId(URL_ID)).thenReturn(Flux.just(e1, e2));

            StepVerifier.create(clickEventController.getClicksByUrlId(URL_ID))
                    .expectNext(e1)
                    .expectNext(e2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Flux vazio quando URL não tem cliques")
        void shouldReturnEmptyFluxWhenNoClicks() {
            when(clickEventService.getClicksByUrlId(URL_ID)).thenReturn(Flux.empty());

            StepVerifier.create(clickEventController.getClicksByUrlId(URL_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve propagar erro do service")
        void shouldPropagateServiceError() {
            when(clickEventService.getClicksByUrlId(URL_ID))
                    .thenReturn(Flux.error(new RuntimeException("DB indisponível")));

            StepVerifier.create(clickEventController.getClicksByUrlId(URL_ID))
                    .expectErrorMessage("DB indisponível")
                    .verify();
        }
    }

    // =======================================================================
    // GET /api/clicks/ip/{ipAddress}  — getClicksByIp
    // =======================================================================

    @Nested
    @DisplayName("GET /api/clicks/ip/{ipAddress}")
    class GetClicksByIp {

        @Test
        @DisplayName("deve retornar cliques do IP informado")
        void shouldReturnClicksForIp() {
            ClickEvent e1 = buildEvent(1L, URL_ID, IP);
            ClickEvent e2 = buildEvent(2L, 2L, IP);
            when(clickEventService.getClicksByIp(IP)).thenReturn(Flux.just(e1, e2));

            StepVerifier.create(clickEventController.getClicksByIp(IP))
                    .expectNext(e1)
                    .expectNext(e2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Flux vazio quando IP não tem cliques")
        void shouldReturnEmptyFluxWhenNoClicksForIp() {
            when(clickEventService.getClicksByIp(IP)).thenReturn(Flux.empty());

            StepVerifier.create(clickEventController.getClicksByIp(IP))
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve propagar erro do service")
        void shouldPropagateServiceError() {
            when(clickEventService.getClicksByIp(IP))
                    .thenReturn(Flux.error(new RuntimeException("Timeout")));

            StepVerifier.create(clickEventController.getClicksByIp(IP))
                    .expectErrorMessage("Timeout")
                    .verify();
        }
    }

    // =======================================================================
    // GET /api/clicks  — getAllClicks
    // =======================================================================

    @Nested
    @DisplayName("GET /api/clicks")
    class GetAllClicks {

        @Test
        @DisplayName("deve retornar todos os cliques")
        void shouldReturnAllClicks() {
            ClickEvent e1 = buildEvent(1L, 1L, "10.0.0.1");
            ClickEvent e2 = buildEvent(2L, 2L, "10.0.0.2");
            ClickEvent e3 = buildEvent(3L, 3L, "10.0.0.3");
            when(clickEventService.getAllClicks()).thenReturn(Flux.just(e1, e2, e3));

            StepVerifier.create(clickEventController.getAllClicks())
                    .expectNext(e1)
                    .expectNext(e2)
                    .expectNext(e3)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Flux vazio quando não há cliques")
        void shouldReturnEmptyFlux() {
            when(clickEventService.getAllClicks()).thenReturn(Flux.empty());

            StepVerifier.create(clickEventController.getAllClicks())
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve propagar erro do service")
        void shouldPropagateServiceError() {
            when(clickEventService.getAllClicks())
                    .thenReturn(Flux.error(new RuntimeException("Erro de conexão")));

            StepVerifier.create(clickEventController.getAllClicks())
                    .expectErrorMessage("Erro de conexão")
                    .verify();
        }
    }

    // =======================================================================
    // GET /api/clicks/{id}  — getById
    // =======================================================================

    @Nested
    @DisplayName("GET /api/clicks/{id}")
    class GetById {

        @Test
        @DisplayName("deve retornar o clique quando encontrado")
        void shouldReturnClickWhenFound() {
            ClickEvent event = buildEvent(EVENT_ID, URL_ID, IP);
            when(clickEventService.getById(EVENT_ID)).thenReturn(Mono.just(event));

            StepVerifier.create(clickEventController.getById(EVENT_ID))
                    .expectNext(event)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Mono vazio quando não encontrado")
        void shouldReturnEmptyWhenNotFound() {
            when(clickEventService.getById(EVENT_ID)).thenReturn(Mono.empty());

            StepVerifier.create(clickEventController.getById(EVENT_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve propagar erro do service")
        void shouldPropagateServiceError() {
            when(clickEventService.getById(EVENT_ID))
                    .thenReturn(Mono.error(new RuntimeException("Falha no banco")));

            StepVerifier.create(clickEventController.getById(EVENT_ID))
                    .expectErrorMessage("Falha no banco")
                    .verify();
        }
    }

    // =======================================================================
    // DELETE /api/clicks/{id}  — deleteById
    // =======================================================================

    @Nested
    @DisplayName("DELETE /api/clicks/{id}")
    class DeleteById {

        @Test
        @DisplayName("deve deletar o clique e completar sem emitir item")
        void shouldDeleteAndComplete() {
            when(clickEventService.deleteById(EVENT_ID)).thenReturn(Mono.empty());

            StepVerifier.create(clickEventController.deleteById(EVENT_ID))
                    .verifyComplete();

            verify(clickEventService).deleteById(EVENT_ID);
        }

        @Test
        @DisplayName("deve propagar erro do service")
        void shouldPropagateServiceError() {
            when(clickEventService.deleteById(EVENT_ID))
                    .thenReturn(Mono.error(new RuntimeException("Falha ao deletar")));

            StepVerifier.create(clickEventController.deleteById(EVENT_ID))
                    .expectErrorMessage("Falha ao deletar")
                    .verify();
        }
    }
}