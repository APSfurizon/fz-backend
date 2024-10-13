package net.furizon.backend.db.entities.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Entity
@Table(name = "authentications")
@Data
public class AuthenticationData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "authentication_id", nullable = false)
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User authenticationOwner;

    @Column(name = "authentication_password", nullable = false)
    private String passwordHash;

    @Column(name = "authentication_email", unique = true, nullable = false)
    private String email;

    @Column(name = "authentication_email_verified")
    private boolean emailVerified;

    @Column(name = "authentication_2fa_enabled")
    private boolean is2faEnabled;

    @Column(name = "authentication_token")
    private String totpToken;

    @Column(name = "authentication_from_oauth")
    private boolean isOauthLogin;

    @Column(name = "authentication_disabled")
    private boolean isLoginDisabled;
}
