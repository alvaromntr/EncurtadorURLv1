package com.tcc.url_cutter_api.controller.security;

import com.tcc.url_cutter_api.controller.UrlController;
import com.tcc.url_cutter_api.dto.ChangePasswordRequest;
import com.tcc.url_cutter_api.dto.security.*;
import com.tcc.url_cutter_api.enums.auth.RoleName;
import com.tcc.url_cutter_api.enums.auth.UserStatus;
import com.tcc.url_cutter_api.model.auth.User;
import com.tcc.url_cutter_api.service.EmailService;
import com.tcc.url_cutter_api.service.security.OtpService;
import com.tcc.url_cutter_api.service.security.RoleService;
import com.tcc.url_cutter_api.service.security.UserRoleService;
import com.tcc.url_cutter_api.service.security.UserService;
import com.tcc.url_cutter_api.utils.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JWTUtil jwtUtil;

    private final UserService userService;

    private final RoleService roleService;

    private final UserRoleService userRoleService;

    private final OtpService otpService;

    private final EmailService emailService;

    private final BCryptPasswordEncoder encoder;

    private static final Logger logger =
            LoggerFactory.getLogger(UrlController.class);

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(
            @Valid @RequestBody AuthRequestRecord authRequest
    ) {

        return userService.findByEmail(authRequest.email())

                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "INVALID_CREDENTIALS"
                                )
                        )
                )

                .flatMap(user -> {

                    if (user.getStatus() != UserStatus.ACTIVE) {

                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "EMAIL_NOT_VERIFIED"
                                )
                        );
                    }

                    if (!encoder.matches(
                            authRequest.pw(),
                            user.getPasswordHash()
                    )) {

                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "INVALID_CREDENTIALS"
                                )
                        );
                    }

                    String code = otpService.generateCode();

                    return otpService.saveCode(user.getId(), code)

                            .then(
                                    emailService.sendCode(
                                            user.getEmail(),
                                            code
                                    )
                            )

                            .thenReturn(
                                    ResponseEntity.ok(
                                            "2FA_REQUIRED"
                                    )
                            );
                });
    }

    @PostMapping("/verify-2fa")
    public Mono<ResponseEntity<AuthResponseRecord>> verify2FA(
            @RequestBody Verify2FARequest request
    ) {

        return userService.findByEmail(request.email())

                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "USER_NOT_FOUND"
                                )
                        )
                )

                .flatMap(user ->

                        otpService.validate(
                                        user.getId(),
                                        request.code()
                                )

                                .flatMap(valid -> {

                                    if (!valid) {

                                        return Mono.error(
                                                new ResponseStatusException(
                                                        HttpStatus.BAD_REQUEST,
                                                        "INVALID_CODE"
                                                )
                                        );
                                    }

                                    return userRoleService
                                            .findByUserId(user.getId())

                                            .flatMap(userRole ->
                                                    roleService.findById(
                                                            userRole.getRoleId()
                                                    )
                                            )

                                            .map(role ->
                                                    "ROLE_" + role.getName()
                                            )

                                            .collectList()

                                            .map(roles -> {

                                                String token =
                                                        jwtUtil.generateToken(
                                                                user.getEmail(),
                                                                user.getId().toString(),
                                                                roles
                                                        );

                                                return ResponseEntity.ok(
                                                        new AuthResponseRecord(
                                                                token,
                                                                roles.get(0),
                                                                UserResponseDTO.from(user)
                                                        )
                                                );
                                            });
                                })
                );
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<?>> signup(
            @Valid @RequestBody AuthRequestRecord authRequest
    ) {

        logger.info(
                "Signup request received for email: {}",
                authRequest.email()
        );

        return userService.existsByEmail(authRequest.email())

                .flatMap(exists -> {

                    if (exists) {

                        logger.warn(
                                "Email {} já existe",
                                authRequest.email()
                        );

                        return Mono.just(
                                ResponseEntity
                                        .status(HttpStatus.CONFLICT)
                                        .body("EMAIL_ALREADY_EXISTS")
                        );
                    }

                    User user = new User();

                    user.setFirstName(
                            authRequest.firstName()
                    );

                    user.setLastName(
                            authRequest.lastName()
                    );

                    user.setEmail(
                            authRequest.email()
                    );

                    user.setPassword(
                            authRequest.pw()
                    );

                    user.setStatus(
                            UserStatus.INACTIVE
                    );

                    user.setCreatedAt(
                            Instant.now()
                    );

                    return userService.save(user)

                            .flatMap(savedUser -> {

                                String code =
                                        otpService.generateCode();

                                return otpService
                                        .saveCode(
                                                savedUser.getId(),
                                                code
                                        )

                                        .then(
                                                emailService.sendCode(
                                                                savedUser.getEmail(),
                                                                code
                                                        )

                                                        .onErrorResume(error -> {

                                                            logger.error(
                                                                    "Erro ao enviar email",
                                                                    error
                                                            );

                                                            return Mono.empty();
                                                        })
                                        )

                                        .thenReturn(
                                                ResponseEntity
                                                        .status(HttpStatus.CREATED)
                                                        .body(
                                                                "VERIFY_EMAIL_REQUIRED"
                                                        )
                                        );
                            });
                });
    }

    @PostMapping("/change-password")
    public Mono<ResponseEntity<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {

        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser) authentication.getPrincipal();

        UUID userId = authenticatedUser.id();

        return userService.findById(userId)

                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "USER_NOT_FOUND"
                                )
                        )
                )

                .flatMap(user -> {

                    if (!encoder.matches(
                            request.currentPassword(),
                            user.getPasswordHash()
                    )) {

                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "INVALID_PASSWORD"
                                )
                        );
                    }

                    user.setPassword(
                            request.newPassword()
                    );

                    return userService.save(user);
                })

                .thenReturn(
                        ResponseEntity.noContent().build()
                );
    }

    @PostMapping("/verify-signup")
    public Mono<ResponseEntity<AuthResponseDTO>> verifySignup(
            @RequestBody Verify2FARequest request
    ) {

        return userService.findByEmail(request.email())

                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "USER_NOT_FOUND"
                                )
                        )
                )

                .flatMap(user ->

                        otpService.validate(
                                        user.getId(),
                                        request.code()
                                )

                                .flatMap(valid -> {

                                    if (!valid) {

                                        return Mono.error(
                                                new ResponseStatusException(
                                                        HttpStatus.BAD_REQUEST,
                                                        "INVALID_CODE"
                                                )
                                        );
                                    }

                                    user.setStatus(UserStatus.ACTIVE);

                                    return userService.save(user)

                                            .flatMap(updatedUser ->

                                                    roleService.findByName(
                                                                    RoleName.OPERADOR
                                                            )

                                                            .flatMap(role ->

                                                                    userRoleService
                                                                            .assignRole(
                                                                                    updatedUser.getId(),
                                                                                    role.getId()
                                                                            )

                                                                            .thenReturn(role)
                                                            )

                                                            .map(role -> {

                                                                List<String> roles =
                                                                        List.of(
                                                                                "ROLE_" +
                                                                                        role.getName()
                                                                        );

                                                                String token =
                                                                        jwtUtil.generateToken(
                                                                                updatedUser.getEmail(),
                                                                                updatedUser.getId().toString(),
                                                                                roles
                                                                        );

                                                                return ResponseEntity.ok(
                                                                        new AuthResponseDTO(
                                                                                token,
                                                                                role.getName().toString(),
                                                                                UserResponseDTO.from(updatedUser)
                                                                        )
                                                                );
                                                            })
                                            );
                                })
                );
    }

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<Void>> deleteAccount(
            Authentication authentication
    ) {

        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser) authentication.getPrincipal();

        UUID userId = authenticatedUser.id();

        return userService.findById(userId)

                .switchIfEmpty(
                        Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "USER_NOT_FOUND"
                                )
                        )
                )

                .flatMap(user ->

                        userRoleService
                                .deleteByUserId(user.getId())

                                .then(
                                        otpService.deleteByUserId(user.getId())
                                )

                                .then(
                                        userService.deleteById(user.getId())
                                )

                                .then(
                                        Mono.fromSupplier(() -> {

                                            SecurityContextHolder.clearContext();

                                            return ResponseEntity
                                                    .noContent()
                                                    .<Void>build();
                                        })
                                )
                );
    }

    @GetMapping("/protected")
    public Mono<ResponseEntity<String>> protectedEndpoint() {

        return Mono.just(
                ResponseEntity.ok(
                        "You have accessed a protected endpoint!"
                )
        );
    }
}