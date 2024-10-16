package net.furizon.backend.db.entities.pretix;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "room_id")
    private long id;

    @Column(name = "room_name")
    @Getter
    @Setter
    private String name;

    @Column(name = "room_confirmed")
    @Getter
    @Setter
    private Boolean isConfirmed;

    @OneToOne
    @JoinColumn(name = "order_id")
    @Getter
    @Setter
    private Order roomOrder;

    @OneToMany(mappedBy = "room")
    @Getter
    private List<RoomGuest> roomGuests;
}
