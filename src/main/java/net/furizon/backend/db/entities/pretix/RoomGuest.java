package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.furizon.backend.db.entities.users.User;

@Entity
@Table(name = "room_guests")
public class RoomGuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "room_guest_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Getter @Setter
    private User guest;

    @ManyToOne
    @JoinColumn(name = "room_id")
    @Getter @Setter
    private Room room;

}
