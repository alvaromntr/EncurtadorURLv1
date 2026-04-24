package com.tcc.url_cutter_api.service;

import com.tcc.url_cutter_api.dto.UrlResponse;
import com.tcc.url_cutter_api.model.Url;
import com.tcc.url_cutter_api.repo.UrlRepository;
import com.tcc.url_cutter_api.utils.SecurityUtils;
import com.tcc.url_cutter_api.utils.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class SimpleURLShortenerService {

    private UrlRepository urlRepository;

    private final SecurityUtils securityUtils;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1); // ID do nó

    public SimpleURLShortenerService(UrlRepository urlRepository, SecurityUtils securityUtils) {
        this.urlRepository = urlRepository;
        this.securityUtils = securityUtils;
    }

    // Caracteres Base62
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


    // Envoca a URL base lá do .properties
    @Value("${app.base-url}")
    private String baseUrl;

    // Metodo que converte para Base62
    private static String convertToBase62(long num) {

        StringBuilder sb = new StringBuilder();

        while (num > 0) {
            int remainder = (int)(num % 62);
            sb.append(BASE62.charAt(remainder));
            num = num / 62;
        }

        return sb.reverse().toString();
    }

    public Mono<Url> createUrl(String originalUrl, UUID userId) {
        Url url = new Url();
        url.setOriginalUrl(originalUrl);
        url.setUserId(userId);
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0L);

        return urlRepository.save(url);
    }

//    public Mono<UUID> getCurrentUserId() {
//        return ReactiveSecurityContextHolder.getContext()
//                .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication())
//                .map(auth -> (AuthenticatedUser) auth.getPrincipal())
//                .map(AuthenticatedUser::id); // 👈 aqui
//    }

    public Flux<UrlResponse> getAllUrls() {

        return securityUtils.getCurrentUserId()
                .flatMapMany(userId ->
                        urlRepository.findByUserId(userId)
                                .map(url -> new UrlResponse(
                                        url.getId(),
                                        baseUrl + url.getShortCode(),
                                        url.getOriginalUrl(),
                                        url.getClickCount(),
                                        url.getCreatedAt()
                                ))
                );
    }

    public Mono<Void> deleteById(Long id) {

        return securityUtils.getCurrentUserId()
                .flatMap(userId ->
                        urlRepository.findById(id)
                                .switchIfEmpty(Mono.error(new RuntimeException("URL not found")))
                                .filter(url -> url.getUserId().equals(userId))
                                .switchIfEmpty(Mono.error(new RuntimeException("Access denied")))
                                .flatMap(urlRepository::delete)
                );
    }

    public Mono<String> encode(String originalUrl) {

        return urlRepository.findByOriginalUrl(originalUrl)
                .map(url -> baseUrl + url.getShortCode())
                .switchIfEmpty(Mono.defer(() ->

                        securityUtils.getCurrentUserId() // 👈 pega o usuário aqui
                                .flatMap(userId -> {

                                    Long hash = idGenerator.nextId();
                                    String shortCode = convertToBase62(hash);

                                    Url url = new Url();

                                    url.setHash(hash);
                                    url.setOriginalUrl(
                                            originalUrl.startsWith("http")
                                                    ? originalUrl
                                                    : "https://" + originalUrl
                                    );
                                    url.setShortCode(shortCode);
                                    url.setClickCount(0L);
                                    url.setCreatedAt(LocalDateTime.now());

                                    url.setUserId(userId); // 👈 AQUI está a associação

                                    return urlRepository.save(url)
                                            .doOnNext(saved -> System.out.println("SALVO: " + saved))
                                            .map(savedUrl -> baseUrl + savedUrl.getShortCode());
                                })
                ));
    }

    // Decodifica url
    public Mono<String> decode(String shortUrl, String baseUrl) {

        String shortCode = shortUrl.replace(baseUrl, "");
        return urlRepository.findByShortCode(shortCode)
                .map( url -> url.getOriginalUrl())
                .switchIfEmpty(Mono.error(new RuntimeException("URL não encontrada")));

    }


}