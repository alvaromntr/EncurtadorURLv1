package com.tcc.url_cutter_api.service;

import com.tcc.url_cutter_api.model.Url;
import com.tcc.url_cutter_api.repo.ClickEventRepository;
import com.tcc.url_cutter_api.repo.UrlRepository;
import com.tcc.url_cutter_api.utils.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Service
public class SimpleURLShortenerService {

    private UrlRepository urlRepository;

    private ClickEventRepository clickEventRepository;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1); // ID do nó

    public SimpleURLShortenerService(UrlRepository urlRepository, ClickEventRepository clickEventRepository) {
        this.urlRepository = urlRepository;
        this.clickEventRepository = clickEventRepository;
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

    // Encurta as urls longas
    public Mono<String> encode(String originalUrl) {

        return urlRepository.findByOriginalUrl(originalUrl)
                .map(url -> baseUrl + url.getShortCode()) // ✅ corrigido
                .switchIfEmpty(Mono.defer(() -> {

                    Long hash = idGenerator.nextId(); // 🔥 agora é hash

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

                    return urlRepository.save(url)
                            .doOnNext(saved -> System.out.println("SALVO: " + saved))
                            .map(savedUrl -> baseUrl + savedUrl.getShortCode());
                }));
    }

    // Decodifica url
    public Mono<String> decode(String shortUrl, String baseUrl) {

        String shortCode = shortUrl.replace(baseUrl, "");
        return urlRepository.findByShortCode(shortCode)
                .map( url -> url.getOriginalUrl())
                .switchIfEmpty(Mono.error(new RuntimeException("URL não encontrada")));

    }


}