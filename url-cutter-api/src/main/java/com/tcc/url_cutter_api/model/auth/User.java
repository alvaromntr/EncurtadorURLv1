package com.tcc.url_cutter_api.model.auth;

import com.tcc.url_cutter_api.enums.UserStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table("users")
public class User {

    @Id
    private UUID id;

    private String email;
    private String passwordHash;
    private UserStatus status;

    private Instant createdAt;
    private Instant lastLoginAt;

    public void setPassword(String password) {
        this.passwordHash = new BCryptPasswordEncoder().encode(password);
    }
    
}
