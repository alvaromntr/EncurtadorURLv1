package com.tcc.url_cutter_api.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("click_event")
public class ClickEvent {

    @Id
    private Long id;

    @Column("url_id")
    private Long urlId;

    @Column("clicked_at")
    private LocalDateTime clickedAt;

    @Column("ip_address")
    private String ipAddress;

    @Column("user_agent")
    private String userAgent;

    @Column("referer")
    private String referer;
}