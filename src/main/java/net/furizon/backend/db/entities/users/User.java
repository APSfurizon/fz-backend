package net.furizon.backend.db.entities.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import net.furizon.backend.db.entities.pretix.Order;
import net.furizon.backend.db.entities.users.content.Fursuit;
import net.furizon.backend.db.entities.users.content.Media;

import java.util.List;
import java.util.Set;

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
@Getter
public final class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    // Primitives can't be nullable, nullable = false is rudimentary here?
    private long id;

    // Primitives can't be nullable, nullable = false is rudimentary here?
    @Column(name = "user_secret", nullable = false)
    private long secret;

    @Column(name = "user_first_name")
    // TODO -> nullable?
    private String firstName;

    @Column(name = "user_last_name")
    // TODO -> nullable?
    private String lastName;

    @OneToMany(mappedBy = "orderOwner")
    // TODO -> nullable?
    private Set<Order> orders;

    @OneToOne(mappedBy = "authenticationOwner")
    // TODO -> nullable?
    private AuthenticationData authentication;

    @OneToMany(mappedBy = "user")
    // TODO -> nullable?
    private List<UserGroup> userGroupAssociations;

    @OneToMany(mappedBy = "mediaOwner")
    // TODO -> nullable?
    private List<Media> userMedias;

    @OneToMany(mappedBy = "fursuitOwner")
    // TODO -> nullable?
    private List<Fursuit> userFursuits;
}
