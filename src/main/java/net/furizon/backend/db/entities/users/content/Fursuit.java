package net.furizon.backend.db.entities.users.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.furizon.backend.db.entities.users.User;

@Entity
@Table(name = "fursuits")
@Data
public class Fursuit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fursuit_id", nullable = false)
    @Setter(AccessLevel.NONE)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User fursuitOwner;

    @Column(name = "fursuit_name")
    private String name;

    @Column(name = "fursuit_species")
    private String species;

    @OneToOne
    @JoinColumn(name = "media_id")
    private Media media;
}
