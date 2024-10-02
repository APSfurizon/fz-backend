package net.furizon.backend.db.entities.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.furizon.backend.db.entities.pretix.Order;
import net.furizon.backend.db.entities.users.security.AuthenticationData;

import java.util.Set;

/**
 * The user class represent a past, present or future attendee at the convention,
 * it may be the author of published images, owner of a room or a roomie,
 * owns one or more membership cards, either expired or not.
 * May be linked to any orders placed in pretix.
 *<br><br>
 * <b>TODO: handle nickname and fursona name problem</b>
 */
@Entity
@Table(name = "users")
public final class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Column(name="user_id", nullable = false)
    private long id;

    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter @Column(name="user_secret", nullable = false)
    private long secret;

    @Getter @Column(name = "user_first_name")
    private String firstName;

    @OneToMany(mappedBy = "orderOwner")
    private Set<Order> orders;

    @OneToOne(mappedBy = "authentication")
    @Getter
    private User authenticationOwner;

}
