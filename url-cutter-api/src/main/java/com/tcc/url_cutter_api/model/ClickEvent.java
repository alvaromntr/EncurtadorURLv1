package com.tcc.url_cutter_api.model;

import lombok.*;
import org.springframework.data.annotation.Id;
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

    private Long UrlId;

    private LocalDateTime clickedAt;

    private String ipAddress;

    private String userAgent;

    private String referer;
}