package com.tcc.url_cutter_api.service;

import com.tcc.url_cutter_api.model.Url;
import com.tcc.url_cutter_api.repo.UrlRepository;
import com.tcc.url_cutter_api.controller.UrlSenderController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UrlSenderController")
class UrlSenderControllerTest {

    @Mock private UrlRepository urlRepository;
    @Mock private ClickEventService clickEventService;
    @Mock private SimpleURLShortenerService shortenerService;

    @InjectMocks
    private UrlSenderController urlSenderController;

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private static final String SHORT_CODE   = "abc123";
    private static final String ORIGINAL_URL = "https://www.example.com/very/long/path";
    private static final Long   URL_ID       = 1L;
    private static final String IP           = "192.168.0.1";
    private static final String USER_AGENT   = "Mozilla/5.0";

    private Url buildUrl(Long id, String shortCode, String originalUrl) {
        Url url = new Url();
        url.setId(id);
        url.setShortCode(shortCode);
        url.setOriginalUrl(originalUrl);
        return url;
    }

    /** Request com IP e User-Agent definidos */
    private ServerHttpRequest mockRequest(String ip, String userAgent) throws Exception {
        ServerHttpRequest request = mock(ServerHttpRequest.class);

        InetSocketAddress remoteAddress = new InetSocketAddress(
                InetAddress.getByName(ip), 0
        );
        lenient().when(request.getRemoteAddress()).thenReturn(remoteAddress);

        org.springframework.http.HttpHeaders headers =
                new org.springframework.http.HttpHeaders();
        if (userAgent != null) {
            headers.add("User-Agent", userAgent);
        }
        lenient().when(request.getHeaders()).thenReturn(headers);

        return request;
    }

    /** Request sem endereço remoto (remoteAddress == null) */
    private ServerHttpRequest mockRequestWithNullIp(String userAgent) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        lenient().when(request.getRemoteAddress()).thenReturn(null);

        org.springframework.http.HttpHeaders headers =
                new org.springframework.http.HttpHeaders();
        if (userAgent != null) {
            headers.add("User-Agent", userAgent);
        }
        lenient().when(request.getHeaders()).thenReturn(headers);

        return request;
    }

    // =======================================================================
    // GET /r/{shortCode}
    // =======================================================================

    @Nested
    @DisplayName("GET /r/{shortCode}")
    class Redirect {

        @Test
        @DisplayName("deve redirecionar com 302 para a URL original quando shortCode existe")
        void shouldRedirectWhenShortCodeFound() throws Exception {
            Url url = buildUrl(URL_ID, SHORT_CODE, ORIGINAL_URL);
            when(urlRepository.findByShortCode(SHORT_CODE)).thenReturn(Mono.just(url));
            when(clickEventService.registerClick(URL_ID, IP, USER_AGENT))
                    .thenReturn(Mono.empty());

            StepVerifier.create(urlSenderController.redirect(SHORT_CODE, mockRequest(IP, USER_AGENT)))
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
                        assertThat(response.getHeaders().getLocation())
                                .isEqualTo(URI.create(ORIGINAL_URL));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve registrar o clique com IP e User-Agent corretos")
        void shouldRegisterClickWithCorrectIpAndUserAgent() throws Exception {
            Url url = buildUrl(URL_ID, SHORT_CODE, ORIGINAL_URL);
            when(urlRepository.findByShortCode(SHORT_CODE)).thenReturn(Mono.just(url));
            when(clickEventService.registerClick(URL_ID, IP, USER_AGENT))
                    .thenReturn(Mono.empty());

            urlSenderController.redirect(SHORT_CODE, mockRequest(IP, USER_AGENT)).block();

            verify(clickEventService).registerClick(URL_ID, IP, USER_AGENT);
        }

        @Test
        @DisplayName("deve usar 'unknown' como IP quando remoteAddress é nulo")
        void shouldFallbackToUnknownWhenRemoteAddressIsNull() {
            Url url = buildUrl(URL_ID, SHORT_CODE, ORIGINAL_URL);
            when(urlRepository.findByShortCode(SHORT_CODE)).thenReturn(Mono.just(url));
            when(clickEventService.registerClick(URL_ID, "unknown", USER_AGENT))
                    .thenReturn(Mono.empty());

            StepVerifier.create(urlSenderController.redirect(SHORT_CODE,
                            mockRequestWithNullIp(USER_AGENT)))
                    .assertNext(response ->
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND))
                    .verifyComplete();

            verify(clickEventService).registerClick(URL_ID, "unknown", USER_AGENT);
        }

        @Test
        @DisplayName("deve aceitar User-Agent nulo (header ausente)")
        void shouldHandleNullUserAgent() throws Exception {
            Url url = buildUrl(URL_ID, SHORT_CODE, ORIGINAL_URL);
            when(urlRepository.findByShortCode(SHORT_CODE)).thenReturn(Mono.just(url));
            when(clickEventService.registerClick(URL_ID, IP, null))
                    .thenReturn(Mono.empty());

            StepVerifier.create(urlSenderController.redirect(SHORT_CODE, mockRequest(IP, null)))
                    .assertNext(response ->
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND))
                    .verifyComplete();

            verify(clickEventService).registerClick(URL_ID, IP, null);
        }

        @Test
        @DisplayName("deve retornar 404 quando shortCode não existe")
        void shouldReturn404WhenShortCodeNotFound() throws Exception {
            when(urlRepository.findByShortCode(SHORT_CODE)).thenReturn(Mono.empty());

            StepVerifier.create(urlSenderController.redirect(SHORT_CODE, mockRequest(IP, USER_AGENT)))
                    .assertNext(response ->
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                    .verifyComplete();

            verify(clickEventService, never()).registerClick(any(), any(), any());
        }

        @Test
        @DisplayName("deve propagar erro do UrlRepository")
        void shouldPropagateRepositoryError() throws Exception {
            when(urlRepository.findByShortCode(SHORT_CODE))
                    .thenReturn(Mono.error(new RuntimeException("DB indisponível")));

            StepVerifier.create(urlSenderController.redirect(SHORT_CODE, mockRequest(IP, USER_AGENT)))
                    .expectErrorMessage("DB indisponível")
                    .verify();
        }

        @Test
        @DisplayName("deve propagar erro do ClickEventService")
        void shouldPropagateClickEventServiceError() throws Exception {
            Url url = buildUrl(URL_ID, SHORT_CODE, ORIGINAL_URL);
            when(urlRepository.findByShortCode(SHORT_CODE)).thenReturn(Mono.just(url));
            when(clickEventService.registerClick(URL_ID, IP, USER_AGENT))
                    .thenReturn(Mono.error(new RuntimeException("Falha ao registrar clique")));

            StepVerifier.create(urlSenderController.redirect(SHORT_CODE, mockRequest(IP, USER_AGENT)))
                    .expectErrorMessage("Falha ao registrar clique")
                    .verify();
        }

        @Test
        @DisplayName("deve montar Location header com a URL original exata")
        void shouldSetExactLocationHeader() throws Exception {
            String longUrl = "https://www.example.com/path?q=search&lang=pt-BR#section";
            Url url = buildUrl(URL_ID, SHORT_CODE, longUrl);
            when(urlRepository.findByShortCode(SHORT_CODE)).thenReturn(Mono.just(url));
            when(clickEventService.registerClick(URL_ID, IP, USER_AGENT))
                    .thenReturn(Mono.empty());

            StepVerifier.create(urlSenderController.redirect(SHORT_CODE, mockRequest(IP, USER_AGENT)))
                    .assertNext(response ->
                            assertThat(response.getHeaders().getLocation())
                                    .isEqualTo(URI.create(longUrl)))
                    .verifyComplete();
        }
    }
}