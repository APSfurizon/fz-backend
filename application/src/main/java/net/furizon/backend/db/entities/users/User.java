package net.furizon.backend.db.entities.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.furizon.backend.db.entities.pretix.Order;

import java.util.Set;

// TODO -> Replace on JOOQ

/**
 * The user class represent a past, present or future attendee at the convention,
 * it may be the author of published images, owner of a room or a roomie,
 * owns one or more membership cards, either expired or not.
 * May be linked to any orders placed in pretix.
 * <br><br>
 * <b>TODO: handle nickname and fursona name problem</b>
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
public final class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private long id;

    @Column(name = "user_secret", nullable = false)
    private String secret;

    @Setter
    @Column(name = "user_first_name")
    // TODO -> nullable?
    private String firstName;

    @Setter
    @Column(name = "user_last_name")
    // TODO -> nullable?
    private String lastName;

    @Setter
    @Column(name = "user_locale")
    // TODO -> nullable?
    private String locale = "en";

    @OneToMany(mappedBy = "orderOwner")
    // TODO -> nullable?
    private Set<Order> orders;

    public User(String secret) {
        this.secret = secret;
    }
}
