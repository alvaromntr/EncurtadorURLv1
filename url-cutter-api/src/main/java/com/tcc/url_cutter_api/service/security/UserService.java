package com.tcc.url_cutter_api.service.security;

import com.tcc.url_cutter_api.dto.security.AuthRequestRecord;
import com.tcc.url_cutter_api.enums.auth.UserStatus;
import com.tcc.url_cutter_api.model.auth.User;
import com.tcc.url_cutter_api.repo.auth.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Mono<User> findByEmail(String username) {
        return userRepository.findByEmail(username);
    }

    public Mono<Boolean> existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Mono<User> save(User user) {
        user.setPasswordHash(user.getPasswordHash()); // Encrypt password before saving
        return userRepository.save(user);
    }

    public Mono<ResponseEntity<Void>> sigUp(@RequestBody AuthRequestRecord user) {

        User newUser = new User();
        newUser.setId(null); // força INSERT
        newUser.setEmail(user.email());
        //newUser.setPasswordHash(user.pw());
        newUser.setPasswordHash(user.pw());
        newUser.setStatus(UserStatus.ACTIVE);

        userRepository.save(newUser)
                .subscribe(
                        value -> System.out.println("Valor: " + value),   // onNext
                        error -> System.err.println("Erro: " + error),    // onError
                        () -> System.out.println("User signed !")   // onComplete
                );

        return Mono.just(ResponseEntity.noContent().build());
    }

    public Mono<ResponseEntity<Void>> login(@RequestBody AuthRequestRecord auth) {

        return userRepository.findByEmail(auth.email())
                // verifica a senha
                .filter(user -> user.getPasswordHash().equals(auth.pw()))
                // se passou pelo filter → autenticado
                .map(user -> ResponseEntity.noContent().<Void>build())
                // se não encontrou usuário OU senha inválida
                .switchIfEmpty(
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
                );
    }

    public Mono<Void> deleteById(UUID id) {
        return userRepository.deleteById(id);
    }

    public Mono<Void> deleteByEmail(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user -> userRepository.deleteById(user.getId()));
    }
}
