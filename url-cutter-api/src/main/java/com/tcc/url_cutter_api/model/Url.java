package com.tcc.url_cutter_api.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("short_url")
public class Url {

    @Id
    private Long id;

    @Column("hash")
    private Long hash;

    @Column("original_url")
    private String originalUrl;

    @Column("short_code")
    private String shortCode;

    @Column("click_count")
    private Long clickCount;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("user_id")
    private UUID userId; // 👈 relacionamento
}