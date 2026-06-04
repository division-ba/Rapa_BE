package io.eddie.unitybe.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, length = 512)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public RefreshToken(String refreshToken, LocalDateTime expiresAt, User user) {
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.user = user;
    }


}
