package com.tcc.url_cutter_api.service.security;

import com.tcc.url_cutter_api.model.auth.TwoFactorCode;
import com.tcc.url_cutter_api.repo.auth.TwoFactorCodeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpService {

    private final TwoFactorCodeRepository repository;

    public OtpService(TwoFactorCodeRepository repository) {
        this.repository = repository;
    }

    /**
     * Gera um código OTP de 6 dígitos
     */
    public String generateCode() {
        return String.valueOf(
                ThreadLocalRandom.current()
                        .nextInt(100000, 999999)
        );
    }

    /**
     * Salva o código OTP no banco
     */
    public Mono<TwoFactorCode> saveCode(
            UUID userId,
            String code
    ) {

        TwoFactorCode otp = new TwoFactorCode();

        otp.setUserId(userId);
        otp.setCode(code);

        // expira em 5 minutos
        otp.setExpiresAt(
                Instant.now().plusSeconds(300)
        );

        otp.setUsed(false);

        return repository.save(otp);
    }

    /**
     * Valida o OTP
     */
    public Mono<Boolean> validate(
            UUID userId,
            String code
    ) {

        return repository
                .findTopByUserIdOrderByExpiresAtDesc(userId)

                .flatMap(otp -> {

                    // código já usado
                    if (otp.isUsed()) {
                        return Mono.just(false);
                    }

                    // código expirado
                    if (otp.getExpiresAt().isBefore(Instant.now())) {
                        return Mono.just(false);
                    }

                    // código inválido
                    if (!otp.getCode().equals(code)) {
                        return Mono.just(false);
                    }

                    // marca como usado
                    otp.setUsed(true);

                    return repository.save(otp)
                            .thenReturn(true);
                })

                .defaultIfEmpty(false);
    }

    public Mono<Void> deleteByUserId(UUID userId) {
        return repository.deleteByUserId(userId);
    }

}
