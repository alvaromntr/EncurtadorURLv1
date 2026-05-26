package com.tcc.url_cutter_api.service;

import com.tcc.url_cutter_api.controller.UrlController;
import com.tcc.url_cutter_api.dto.UrlRequest;
import com.tcc.url_cutter_api.dto.UrlResponse;
import com.tcc.url_cutter_api.dto.UrlShortenResponse;
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
@DisplayName("UrlController")
class UrlControllerTest {

    @Mock
    private SimpleURLShortenerService shortenerService;

    @InjectMocks
    private UrlController urlController;

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private static final String ORIGINAL_URL = "https://www.example.com/very/long/path";
    private static final String SHORT_URL    = "https://short.ly/abc123";

    private UrlRequest urlRequest(String url) {
        UrlRequest req = new UrlRequest();
        req.setUrl(url);
        return req;
    }

    private UrlResponse buildUrlResponse(Long id, String shortUrl, String originalUrl, Long clicks) {
        return new UrlResponse(id, shortUrl, originalUrl, clicks, LocalDateTime.now());
    }

    // =======================================================================
    // POST /api/shorten
    // =======================================================================

    @Nested
    @DisplayName("POST /api/shorten")
    class Shorten {

        @Test
        @DisplayName("deve encurtar a URL e retornar a URL curta")
        void shouldShortenAndReturnShortUrl() {
            when(shortenerService.encode(ORIGINAL_URL)).thenReturn(Mono.just(SHORT_URL));

            StepVerifier.create(urlController.shorten(urlRequest(ORIGINAL_URL)))
                    .assertNext(response ->
                            assertThat(response.shortUrl()).isEqualTo(SHORT_URL))
                    .verifyComplete();

            verify(shortenerService).encode(ORIGINAL_URL);
        }

        @Test
        @DisplayName("deve mapear a URL encurtada para UrlShortenResponse corretamente")
        void shouldMapToUrlShortenResponse() {
            when(shortenerService.encode(ORIGINAL_URL)).thenReturn(Mono.just(SHORT_URL));

            StepVerifier.create(urlController.shorten(urlRequest(ORIGINAL_URL)))
                    .assertNext(response -> {
                        assertThat(response).isInstanceOf(UrlShortenResponse.class);
                        assertThat(response.shortUrl()).isEqualTo(SHORT_URL);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve propagar erro do service")
        void shouldPropagateServiceError() {
            when(shortenerService.encode(ORIGINAL_URL))
                    .thenReturn(Mono.error(new RuntimeException("Falha ao encurtar")));

            StepVerifier.create(urlController.shorten(urlRequest(ORIGINAL_URL)))
                    .expectErrorMessage("Falha ao encurtar")
                    .verify();
        }
    }

    // =======================================================================
    // GET /api/my-urls
    // =======================================================================

    @Nested
    @DisplayName("GET /api/my-urls")
    class GetMyUrls {

        @Test
        @DisplayName("deve retornar todas as URLs do usuário")
        void shouldReturnAllUrls() {
            UrlResponse r1 = buildUrlResponse(1L, "https://short.ly/aaa", ORIGINAL_URL, 10L);
            UrlResponse r2 = buildUrlResponse(2L, "https://short.ly/bbb", "https://other.com", 5L);
            when(shortenerService.getAllUrls()).thenReturn(Flux.just(r1, r2));

            StepVerifier.create(urlController.getMyUrls())
                    .expectNext(r1)
                    .expectNext(r2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Flux vazio quando usuário não tem URLs")
        void shouldReturnEmptyFlux() {
            when(shortenerService.getAllUrls()).thenReturn(Flux.empty());

            StepVerifier.create(urlController.getMyUrls())
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar contagem de cliques correta em cada URL")
        void shouldReturnCorrectClickCount() {
            UrlResponse r = buildUrlResponse(1L, SHORT_URL, ORIGINAL_URL, 42L);
            when(shortenerService.getAllUrls()).thenReturn(Flux.just(r));

            StepVerifier.create(urlController.getMyUrls())
                    .assertNext(response ->
                            assertThat(response.clickCount()).isEqualTo(42L))
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve propagar erro do service")
        void shouldPropagateServiceError() {
            when(shortenerService.getAllUrls())
                    .thenReturn(Flux.error(new RuntimeException("DB indisponível")));

            StepVerifier.create(urlController.getMyUrls())
                    .expectErrorMessage("DB indisponível")
                    .verify();
        }
    }

    // =======================================================================
    // DELETE /api/{id}
    // =======================================================================

    @Nested
    @DisplayName("DELETE /api/{id}")
    class Delete {

        @Test
        @DisplayName("deve deletar a URL pelo ID e completar sem emitir item")
        void shouldDeleteAndComplete() {
            when(shortenerService.deleteById(1L)).thenReturn(Mono.empty());

            StepVerifier.create(urlController.delete(1L))
                    .verifyComplete();

            verify(shortenerService).deleteById(1L);
        }

        @Test
        @DisplayName("deve propagar erro do service")
        void shouldPropagateServiceError() {
            when(shortenerService.deleteById(1L))
                    .thenReturn(Mono.error(new RuntimeException("Falha ao deletar")));

            StepVerifier.create(urlController.delete(1L))
                    .expectErrorMessage("Falha ao deletar")
                    .verify();
        }
    }
}