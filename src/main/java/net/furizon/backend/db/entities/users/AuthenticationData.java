package net.furizon.backend.db.entities.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "authentications")
public class  AuthenticationData {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name="authentication_id", nullable = false)
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @Getter @Setter
    private User authenticationOwner;

    @Column(name="authentication_password", nullable = false)
    @Getter @Setter
    private String passwordHash;

    @Column(name = "authentication_email", unique = true, nullable = false)
    @Getter @Setter
    private String email;

    @Column(name = "authentication_email_verified")
    @Getter @Setter
    private boolean emailVerified;

    @Column(name = "authentication_2fa_enabled")
    @Getter @Setter
    private boolean is2faEnabled;

    @Column(name = "authentication_token")
    @Getter @Setter
    private String totpToken;

    @Column(name = "authentication_from_oauth")
    @Getter @Setter
    private boolean isOauthLogin;

    @Column(name = "authentication_disabled")
    @Getter @Setter
    private boolean isLoginDisabled;
}
