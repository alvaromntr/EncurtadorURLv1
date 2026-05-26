package com.tcc.url_cutter_api.service;

import com.tcc.url_cutter_api.dto.security.*;
import com.tcc.url_cutter_api.enums.auth.RoleName;
import com.tcc.url_cutter_api.enums.auth.UserStatus;
import com.tcc.url_cutter_api.model.auth.Role;
import com.tcc.url_cutter_api.model.auth.User;
import com.tcc.url_cutter_api.model.auth.UserRole;
import com.tcc.url_cutter_api.controller.security.AuthController;
import com.tcc.url_cutter_api.service.security.OtpService;
import com.tcc.url_cutter_api.service.security.RoleService;
import com.tcc.url_cutter_api.service.security.UserRoleService;
import com.tcc.url_cutter_api.service.security.UserService;
import com.tcc.url_cutter_api.utils.JWTUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Mock private JWTUtil jwtUtil;
    @Mock private UserService userService;
    @Mock private RoleService roleService;
    @Mock private UserRoleService userRoleService;
    @Mock private OtpService otpService;
    @Mock private EmailService emailService;
    @Mock private BCryptPasswordEncoder encoder;

    @InjectMocks
    private AuthController authController;

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private static final UUID   USER_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID   ROLE_ID  = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String EMAIL    = "user@example.com";
    private static final String PASSWORD = "secret123";
    private static final String OTP_CODE = "123456";
    private static final String JWT      = "eyJhbGciOiJIUzI1NiJ9.test.token";

    private User activeUser() {
        User u = new User();
        u.setId(USER_ID);
        u.setEmail(EMAIL);
        u.setPasswordHash(PASSWORD);
        u.setStatus(UserStatus.ACTIVE);
        u.setFirstName("John");
        u.setLastName("Doe");
        u.setCreatedAt(Instant.now());
        return u;
    }

    private User inactiveUser() {
        User u = activeUser();
        u.setStatus(UserStatus.INACTIVE);
        return u;
    }

    private Role operadorRole() {
        Role r = new Role();
        r.setId(ROLE_ID);
        r.setName(RoleName.OPERADOR);
        return r;
    }

    private UserRole userRole() {
        UserRole ur = new UserRole();
        ur.setUserId(USER_ID);
        ur.setRoleId(ROLE_ID);
        return ur;
    }

    private AuthRequestRecord authRequest() {
        return new AuthRequestRecord("John", "Doe", EMAIL, PASSWORD);
    }

    private Verify2FARequest verify2FARequest() {
        return new Verify2FARequest(EMAIL, OTP_CODE);
    }

    // =======================================================================
    // POST /auth/login
    // =======================================================================

    @Nested
    @DisplayName("POST /login")
    class Login {

        @Test
        @DisplayName("deve enviar OTP e retornar 2FA_REQUIRED quando credenciais são válidas")
        void shouldSendOtpAndReturn2faRequired() {
            when(userService.findByEmail(EMAIL)).thenReturn(Mono.just(activeUser()));
            when(encoder.matches(PASSWORD, PASSWORD)).thenReturn(true);
            when(otpService.generateCode()).thenReturn(OTP_CODE);
            when(otpService.saveCode(USER_ID, OTP_CODE)).thenReturn(Mono.empty());
            when(emailService.sendCode(EMAIL, OTP_CODE)).thenReturn(Mono.empty());

            StepVerifier.create(authController.login(authRequest()))
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isEqualTo("2FA_REQUIRED");
                    })
                    .verifyComplete();

            verify(otpService).saveCode(USER_ID, OTP_CODE);
            verify(emailService).sendCode(EMAIL, OTP_CODE);
        }

        @Test
        @DisplayName("deve retornar 401 quando e-mail não existe")
        void shouldReturn401WhenEmailNotFound() {
            when(userService.findByEmail(EMAIL)).thenReturn(Mono.empty());

            StepVerifier.create(authController.login(authRequest()))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(ResponseStatusException.class);
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED);
                        assertThat(((ResponseStatusException) ex).getReason())
                                .isEqualTo("INVALID_CREDENTIALS");
                    })
                    .verify();
        }

        @Test
        @DisplayName("deve retornar 403 quando conta está inativa (e-mail não verificado)")
        void shouldReturn403WhenUserIsInactive() {
            when(userService.findByEmail(EMAIL)).thenReturn(Mono.just(inactiveUser()));

            StepVerifier.create(authController.login(authRequest()))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(ResponseStatusException.class);
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(((ResponseStatusException) ex).getReason())
                                .isEqualTo("EMAIL_NOT_VERIFIED");
                    })
                    .verify();
        }

        @Test
        @DisplayName("deve retornar 401 quando senha está incorreta")
        void shouldReturn401WhenPasswordIsWrong() {
            when(userService.findByEmail(EMAIL)).thenReturn(Mono.just(activeUser()));
            when(encoder.matches(PASSWORD, PASSWORD)).thenReturn(false);

            StepVerifier.create(authController.login(authRequest()))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(ResponseStatusException.class);
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED);
                    })
                    .verify();

            verify(otpService, never()).saveCode(any(), any());
            verify(emailService, never()).sendCode(any(), any());
        }

        @Test
        @DisplayName("deve propagar erro do EmailService")
        void shouldPropagateEmailServiceError() {
            when(userService.findByEmail(EMAIL)).thenReturn(Mono.just(activeUser()));
            when(encoder.matches(PASSWORD, PASSWORD)).thenReturn(true);
            when(otpService.generateCode()).thenReturn(OTP_CODE);
            when(otpService.saveCode(USER_ID, OTP_CODE)).thenReturn(Mono.empty());
            when(emailService.sendCode(EMAIL, OTP_CODE))
                    .thenReturn(Mono.error(new RuntimeException("SMTP indisponível")));

            StepVerifier.create(authController.login(authRequest()))
                    .expectErrorMessage("SMTP indisponível")
                    .verify();
        }
    }

    // =======================================================================
    // POST /auth/verify-2fa
    // =======================================================================

    @Nested
    @DisplayName("POST /verify-2fa")
    class Verify2FA {

        @Test
        @DisplayName("deve retornar JWT e dados do usuário quando OTP é válido")
        void shouldReturnJwtWhenOtpIsValid() {
            when(userService.findByEmail(EMAIL)).thenReturn(Mono.just(activeUser()));
            when(otpService.validate(USER_ID, OTP_CODE)).thenReturn(Mono.just(true));
            when(userRoleService.findByUserId(USER_ID)).thenReturn(Flux.just(userRole()));
            when(roleService.findById(ROLE_ID)).thenReturn(Mono.just(operadorRole()));
            when(jwtUtil.generateToken(eq(EMAIL), eq(USER_ID.toString()), anyList()))
                    .thenReturn(JWT);

            StepVerifier.create(authController.verify2FA(verify2FARequest()))
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().token()).isEqualTo(JWT);
                        assertThat(response.getBody().role()).isEqualTo("ROLE_OPERADOR");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar 404 quando usuário não encontrado")
        void shouldReturn404WhenUserNotFound() {
            when(userService.findByEmail(EMAIL)).thenReturn(Mono.empty());

            StepVerifier.create(authController.verify2FA(verify2FARequest()))
                    .expectErrorSatisfies(ex -> {
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(((ResponseStatusException) ex).getReason())
                                .isEqualTo("USER_NOT_FOUND");
                    })
                    .verify();
        }

        @Test
        @DisplayName("deve retornar 400 quando OTP é inválido")
        void shouldReturn400WhenOtpIsInvalid() {
            when(userService.findByEmail(EMAIL)).thenReturn(Mono.just(activeUser()));
            when(otpService.validate(USER_ID, OTP_CODE)).thenReturn(Mono.just(false));

            StepVerifier.create(authController.verify2FA(verify2FARequest()))
                    .expectErrorSatisfies(ex -> {
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(((ResponseStatusException) ex).getReason())
                                .isEqualTo("INVALID_CODE");
                    })
                    .verify();

            verify(jwtUtil, never()).generateToken(any(), any(), any());
        }
    }

    // =======================================================================
    // POST /auth/signup
    // =======================================================================

    @Nested
    @DisplayName("POST /signup")
    class Signup {

        @Test
        @DisplayName("deve criar usuário, enviar OTP e retornar 201 VERIFY_EMAIL_REQUIRED")
        void shouldCreateUserAndSendOtp() {
            when(userService.existsByEmail(EMAIL)).thenReturn(Mono.just(false));
            when(userService.save(any(User.class))).thenReturn(Mono.just(activeUser()));
            when(otpService.generateCode()).thenReturn(OTP_CODE);
            when(otpService.saveCode(USER_ID, OTP_CODE)).thenReturn(Mono.empty());
            when(emailService.sendCode(EMAIL, OTP_CODE)).thenReturn(Mono.empty());

            StepVerifier.create(authController.signup(authRequest()))
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                        assertThat(response.getBody()).isEqualTo("VERIFY_EMAIL_REQUIRED");
                    })
                    .verifyComplete();

            verify(userService).save(any(User.class));
            verify(emailService).sendCode(EMAIL, OTP_CODE);
        }

        @Test
        @DisplayName("deve retornar 409 CONFLICT quando e-mail já está cadastrado")
        void shouldReturn409WhenEmailAlreadyExists() {
            when(userService.existsByEmail(EMAIL)).thenReturn(Mono.just(true));

            StepVerifier.create(authController.signup(authRequest()))
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(response.getBody()).isEqualTo("EMAIL_ALREADY_EXISTS");
                    })
                    .verifyComplete();

            verify(userService, never()).save(any());
        }

        @Test
        @DisplayName("deve construir usuário com status INACTIVE e dados da requisição")
        void shouldBuildUserWithInactiveStatus() {
            when(userService.existsByEmail(EMAIL)).thenReturn(Mono.just(false));
            when(userService.save(any(User.class)))
                    .thenAnswer(inv -> {
                        User u = inv.getArgument(0);
                        u.setId(USER_ID); // simula o ID gerado pelo banco
                        return Mono.just(u);
                    });
            when(otpService.generateCode()).thenReturn(OTP_CODE);
            when(otpService.saveCode(USER_ID, OTP_CODE)).thenReturn(Mono.empty());
            when(emailService.sendCode(EMAIL, OTP_CODE)).thenReturn(Mono.empty());

            authController.signup(authRequest()).block();

            verify(userService).save(argThat(user ->
                    EMAIL.equals(user.getEmail()) &&
                            UserStatus.INACTIVE.equals(user.getStatus()) &&
                            user.getCreatedAt() != null
            ));
        }

        @Test
        @DisplayName("deve retornar 201 mesmo quando envio de e-mail falha (onErrorResume)")
        void shouldReturn201EvenWhenEmailFails() {
            when(userService.existsByEmail(EMAIL)).thenReturn(Mono.just(false));
            when(userService.save(any(User.class))).thenReturn(Mono.just(activeUser()));
            when(otpService.generateCode()).thenReturn(OTP_CODE);
            when(otpService.saveCode(USER_ID, OTP_CODE)).thenReturn(Mono.empty());
            when(emailService.sendCode(EMAIL, OTP_CODE))
                    .thenReturn(Mono.error(new RuntimeException("SMTP fora do ar")));

            StepVerifier.create(authController.signup(authRequest()))
                    .assertNext(response ->
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED))
                    .verifyComplete();
        }
    }

    // =======================================================================
    // POST /auth/verify-signup
    // =======================================================================

    @Nested
    @DisplayName("POST /verify-signup")
    class VerifySignup {

        @Test
        @DisplayName("deve ativar usuário, atribuir role e retornar JWT")
        void shouldActivateUserAndReturnJwt() {
            User user = inactiveUser();
            User savedUser = activeUser();

            when(userService.findByEmail(EMAIL)).thenReturn(Mono.just(user));
            when(otpService.validate(USER_ID, OTP_CODE)).thenReturn(Mono.just(true));
            when(userService.save(any(User.class))).thenReturn(Mono.just(savedUser));
            when(roleService.findByName(RoleName.OPERADOR)).thenReturn(Mono.just(operadorRole()));
            when(userRoleService.assignRole(USER_ID, ROLE_ID)).thenReturn(Mono.empty());
            when(jwtUtil.generateToken(eq(EMAIL), eq(USER_ID.toString()), anyList()))
                    .thenReturn(JWT);

            StepVerifier.create(authController.verifySignup(verify2FARequest()))
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody()).isNotNull();
                        assertThat(response.getBody().token()).isEqualTo(JWT);
                    })
                    .verifyComplete();

            verify(userService).save(argThat(u -> UserStatus.ACTIVE.equals(u.getStatus())));
            verify(userRoleService).assignRole(USER_ID, ROLE_ID);
        }

        @Test
        @DisplayName("deve retornar 404 quando usuário não encontrado")
        void shouldReturn404WhenUserNotFound() {
            when(userService.findByEmail(EMAIL)).thenReturn(Mono.empty());

            StepVerifier.create(authController.verifySignup(verify2FARequest()))
                    .expectErrorSatisfies(ex -> {
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(((ResponseStatusException) ex).getReason())
                                .isEqualTo("USER_NOT_FOUND");
                    })
                    .verify();
        }

        @Test
        @DisplayName("deve retornar 400 quando OTP é inválido")
        void shouldReturn400WhenOtpIsInvalid() {
            when(userService.findByEmail(EMAIL)).thenReturn(Mono.just(inactiveUser()));
            when(otpService.validate(USER_ID, OTP_CODE)).thenReturn(Mono.just(false));

            StepVerifier.create(authController.verifySignup(verify2FARequest()))
                    .expectErrorSatisfies(ex -> {
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(((ResponseStatusException) ex).getReason())
                                .isEqualTo("INVALID_CODE");
                    })
                    .verify();

            verify(userService, never()).save(any());
        }
    }

    // =======================================================================
    // DELETE /auth/delete
    // =======================================================================

    @Nested
    @DisplayName("DELETE /delete")
    class DeleteAccount {

        private Authentication mockAuthentication() {
            AuthenticatedUser principal = new AuthenticatedUser(USER_ID, EMAIL);
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(principal);
            return auth;
        }

        @Test
        @DisplayName("deve deletar usuário, roles e OTPs e retornar 204 No Content")
        void shouldDeleteAllUserDataAndReturn204() {
            when(userService.findById(USER_ID)).thenReturn(Mono.just(activeUser()));
            when(userRoleService.deleteByUserId(USER_ID)).thenReturn(Mono.empty());
            when(otpService.deleteByUserId(USER_ID)).thenReturn(Mono.empty());
            when(userService.deleteById(USER_ID)).thenReturn(Mono.empty());

            StepVerifier.create(authController.deleteAccount(mockAuthentication()))
                    .assertNext(response ->
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                    .verifyComplete();

            verify(userRoleService).deleteByUserId(USER_ID);
            verify(otpService).deleteByUserId(USER_ID);
            verify(userService).deleteById(USER_ID);
        }

        @Test
        @DisplayName("deve retornar 404 quando usuário não encontrado")
        void shouldReturn404WhenUserNotFound() {
            when(userService.findById(USER_ID)).thenReturn(Mono.empty());

            StepVerifier.create(authController.deleteAccount(mockAuthentication()))
                    .expectErrorSatisfies(ex -> {
                        assertThat(((ResponseStatusException) ex).getStatusCode())
                                .isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(((ResponseStatusException) ex).getReason())
                                .isEqualTo("USER_NOT_FOUND");
                    })
                    .verify();

            verify(userRoleService, never()).deleteByUserId(any());
            verify(userService, never()).deleteById(any());
        }

        @Test
        @DisplayName("deve propagar erro ao deletar roles")
        void shouldPropagateErrorOnRoleDeletion() {
            when(userService.findById(USER_ID)).thenReturn(Mono.just(activeUser()));
            when(userRoleService.deleteByUserId(USER_ID))
                    .thenReturn(Mono.error(new RuntimeException("Falha ao deletar roles")));
            // .then(otpService.deleteByUserId(...)) e .then(userService.deleteById(...))
            // sao avaliados de forma eager na montagem da cadeia reativa — os mocks
            // precisam existir mesmo que esses passos nao sejam executados em runtime
            when(otpService.deleteByUserId(USER_ID)).thenReturn(Mono.empty());
            when(userService.deleteById(USER_ID)).thenReturn(Mono.empty());

            StepVerifier.create(authController.deleteAccount(mockAuthentication()))
                    .expectErrorMessage("Falha ao deletar roles")
                    .verify();
        }
    }

    // =======================================================================
    // GET /auth/protected
    // =======================================================================

    @Nested
    @DisplayName("GET /protected")
    class Protected {

        @Test
        @DisplayName("deve retornar 200 com mensagem de acesso autorizado")
        void shouldReturn200WithMessage() {
            StepVerifier.create(authController.protectedEndpoint())
                    .assertNext(response -> {
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.getBody())
                                .isEqualTo("You have accessed a protected endpoint!");
                    })
                    .verifyComplete();
        }
    }
}