package com.tcc.url_cutter_api.controller.security;

import com.tcc.url_cutter_api.dto.security.AuthRequestRecord;
import com.tcc.url_cutter_api.model.auth.User;
import com.tcc.url_cutter_api.repo.auth.UserRepository;
import com.tcc.url_cutter_api.service.security.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    UserController(UserRepository userRepository, UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Mono<User> save(@RequestBody User user) {
        return userService.save(user);
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<Void>> signup(
            @RequestBody AuthRequestRecord request
    ) {
        return userService.sigUp(request);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Void>> login(
            @RequestBody AuthRequestRecord request
    ) {
        return userService.login(request);
    }

}