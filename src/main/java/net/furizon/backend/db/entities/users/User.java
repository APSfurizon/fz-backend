package net.furizon.backend.db.entities.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.furizon.backend.db.entities.pretix.Order;
import net.furizon.backend.db.entities.pretix.Room;
import net.furizon.backend.db.entities.pretix.RoomGuest;
import net.furizon.backend.db.entities.users.content.Fursuit;
import net.furizon.backend.db.entities.users.content.Media;
import net.furizon.backend.service.users.UserService;

import java.util.List;
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

    @Getter @Column(name="user_secret", nullable = false)
    private String secret;

    @Getter @Setter @Column(name = "user_first_name")
    private String firstName;

    @Getter @Setter @Column(name = "user_last_name")
    private String lastName;

    @OneToMany(mappedBy = "orderOwner")
    @Getter
    private Set<Order> orders;

    @OneToOne(mappedBy = "authenticationOwner")
    @Getter @Setter
    private AuthenticationData authentication;

    @OneToMany(mappedBy = "user")
    @Getter
    private List<UserGroup> userGroupAssociations;

    @OneToMany(mappedBy = "mediaOwner")
    @Getter
    private List<Media> userMedias;

    @OneToMany(mappedBy = "fursuitOwner")
    @Getter
    private List<Fursuit> userFursuits;

    @OneToMany(mappedBy = "guest")
    @Getter
    private List<RoomGuest> userAsGuestList;

    public User() {}

    public User(String secret) {
        this.secret = secret;
    }

}
