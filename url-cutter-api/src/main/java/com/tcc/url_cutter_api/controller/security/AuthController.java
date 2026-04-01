package com.tcc.url_cutter_api.controller.security;

import com.tcc.url_cutter_api.controller.UrlController;
import com.tcc.url_cutter_api.dto.security.AuthRequestRecord;
import com.tcc.url_cutter_api.dto.security.AuthResponseDTO;
import com.tcc.url_cutter_api.dto.security.AuthResponseRecord;
import com.tcc.url_cutter_api.dto.security.UserResponseDTO;
import com.tcc.url_cutter_api.enums.RoleName;
import com.tcc.url_cutter_api.enums.UserStatus;
import com.tcc.url_cutter_api.model.auth.User;
import com.tcc.url_cutter_api.service.security.RoleService;
import com.tcc.url_cutter_api.service.security.UserRoleService;
import com.tcc.url_cutter_api.service.security.UserService;
import com.tcc.url_cutter_api.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private JWTUtil jwtUtil;

    private UserService userService;

    private RoleService roleService;

    private UserRoleService userRoleService;

    private BCryptPasswordEncoder encoder;

    private static final Logger logger =
            LoggerFactory.getLogger(UrlController.class);

    public AuthController(
            JWTUtil jwtUtil,
            UserService userService,
            RoleService roleService,
            UserRoleService userRoleService,
            BCryptPasswordEncoder encoder
    ) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponseRecord>> login(
            @RequestBody AuthRequestRecord authRequest
    ) {

        return userService.findByEmail(authRequest.mail())
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")))

                .flatMap(userDetails -> {
                    if (!encoder.matches(authRequest.pw(), userDetails.getPasswordHash())) {
                        return Mono.error(new BadCredentialsException("Invalid username or password"));
                    }

                    return userRoleService.findByUserId(userDetails.getId())   // Flux<UserRole>
                            .flatMap(userRole ->
                                    roleService.findById(userRole.getRoleId())     // Mono<Role>
                            )
                            .map(role ->
                                    "ROLE_" + role.getName()                        // String
                            )
                            .collectList()                                          // Mono<List<String>>
                            .map(roles -> {

                                String token = jwtUtil.generateToken(
                                        authRequest.mail(),
                                        userDetails.getId().toString(),
                                        roles
                                );

                                return ResponseEntity.ok(
                                        new AuthResponseRecord(
                                                token,
                                                roles.get(0).replace("ROLE_", ""),
                                                UserResponseDTO.from(userDetails)
                                        )
                                );
                            });
                });
    }

    @GetMapping("/test-token")
    public Mono<String> testToken(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");

        return Mono.just("Token válido para usuário: " +
                jwtUtil.getClaimAsString(token, "roles"));
    }


    @PostMapping("/signup")
    public Mono<ResponseEntity<AuthResponseDTO>> signup(
            @RequestBody AuthRequestRecord authRequest
    ) {
        logger.info("Signup request received for email: {}", authRequest.mail());

        return userService.existsByEmail(authRequest.mail())
                .flatMap(exists -> {
                    if (exists) {
                        logger.warn("Email {} já existe", authRequest.mail());
                        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
                    }

                    User user = new User();
                    user.setEmail(authRequest.mail());
                    user.setPassword(authRequest.pw()); // ✅ agora criptografa; // ⚠️ criptografar depois
                    user.setStatus(UserStatus.ACTIVE);
                    user.setCreatedAt(Instant.now());

                    return userService.save(user)
                            .doOnNext(savedUser ->
                                    logger.info("User saved with ID: {}", savedUser.getId())
                            )
                            .flatMap(savedUser ->
                                    roleService.findByName(RoleName.OPERADOR)
                                            .doOnNext(role ->
                                                    logger.info("Role found: {} ({})", role.getName(), role.getId())
                                            )
                                            .flatMap(role ->
                                                    userRoleService.assignRole(savedUser.getId(), role.getId())
                                                            .doOnNext(userRole ->
                                                                    logger.info(
                                                                            "UserRole created: userId={} roleId={}",
                                                                            userRole.getUserId(),
                                                                            userRole.getRoleId()
                                                                    )
                                                            )
                                                            .thenReturn(role) // 👈 importante
                                            )
                                            .map(role -> {
                                                List<String> roles = List.of(
                                                        "ROLE_" + role.getName()
                                                );

                                                String token = jwtUtil.generateToken(
                                                        savedUser.getEmail(),
                                                        savedUser.getId().toString(),
                                                        roles
                                                );

                                                logger.info("JWT token generated for user {} with roles {}",
                                                        savedUser.getEmail(), roles);

                                                return ResponseEntity
                                                        .status(HttpStatus.CREATED)
                                                        .body(new AuthResponseDTO(
                                                                token,
                                                                role.getName().toString(),
                                                                UserResponseDTO.from(savedUser)
                                                        ));
                                            })
                            );
                });
    }

    @PostMapping("/signup-admin")
    public Mono<ResponseEntity<AuthResponseDTO>> signupAdmin(
            @RequestBody AuthRequestRecord authRequest
    ) {
        logger.info("Signup request received for email: {}", authRequest.mail());

        return userService.existsByEmail(authRequest.mail())
                .flatMap(exists -> {
                    if (exists) {
                        logger.warn("Email {} já existe", authRequest.mail());
                        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
                    }

                    User user = new User();
                    user.setEmail(authRequest.mail());
                    user.setPassword(authRequest.pw()); // ✅ agora criptografa;
                    user.setStatus(UserStatus.ACTIVE);
                    user.setCreatedAt(Instant.now());

                    return userService.save(user)
                            .doOnNext(savedUser ->
                                    logger.info("User saved with ID: {}", savedUser.getId())
                            )
                            .flatMap(savedUser ->
                                    roleService.findByName(RoleName.ADMIN)
                                            .doOnNext(role ->
                                                    logger.info("Role found: {} ({})", role.getName(), role.getId())
                                            )
                                            .flatMap(role ->
                                                    userRoleService.assignRole(savedUser.getId(), role.getId())
                                                            .doOnNext(userRole ->
                                                                    logger.info(
                                                                            "UserRole created: userId={} roleId={}",
                                                                            userRole.getUserId(),
                                                                            userRole.getRoleId()
                                                                    )
                                                            )
                                                            .thenReturn(role) // 👈 importante
                                            )
                                            .map(role -> {
                                                List<String> roles = List.of(
                                                        "ROLE_" + role.getName()
                                                );

                                                String token = jwtUtil.generateToken(
                                                        savedUser.getEmail(),
                                                        savedUser.getId().toString(),
                                                        roles
                                                );

                                                logger.info("JWT token generated for user {} with roles {}",
                                                        savedUser.getEmail(), roles);

                                                return ResponseEntity
                                                        .status(HttpStatus.CREATED)
                                                        .body(new AuthResponseDTO(
                                                                token,
                                                                role.getName().toString(),
                                                                UserResponseDTO.from(savedUser)
                                                        ));
                                            })
                            );
                });
    }

    @GetMapping("/protected")
    public Mono<ResponseEntity<String>> protectedEndpoint() {
        return Mono.just(ResponseEntity.ok("You have accessed a protected endpoint!"));
    }
}