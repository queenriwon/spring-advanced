package org.example.expert.domain.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.expert.domain.auth.enums.TokenStatus;
import org.example.expert.domain.common.entity.Timestamped;

import java.util.UUID;

import static org.example.expert.domain.auth.enums.TokenStatus.VALID;

@Getter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String token;

    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    public RefreshToken () {
    }

    public RefreshToken (Long userId) {
        this.userId = userId;
        this.token = UUID.randomUUID().toString();
        this.status = VALID;
    }

    public void updateStatus(TokenStatus status) {
        this.status = status;
    }

}
