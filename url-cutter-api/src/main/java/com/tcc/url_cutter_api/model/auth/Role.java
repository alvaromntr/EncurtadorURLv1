package com.tcc.url_cutter_api.model.auth;

import com.tcc.url_cutter_api.enums.auth.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("roles")
public class Role {

    @Id
    private UUID id;

    @Column("name")
    private RoleName name;
}

