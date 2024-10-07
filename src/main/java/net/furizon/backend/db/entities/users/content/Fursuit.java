package net.furizon.backend.db.entities.users.content;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.furizon.backend.db.entities.users.User;

@Entity
@Table(name = "fursuits")
public class Fursuit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name="fursuit_id", nullable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Getter
    private User fursuitOwner;

    @Getter @Setter
    @Column(name = "fursuit_name")
    private String name;

    @Getter @Setter
    @Column(name = "fursuit_species")
    private String species;

    @Getter @Setter
    @OneToOne
    @JoinColumn(name = "media_id")
    private Media media;
}
