package net.furizon.backend.db.entities.users.content;

import jakarta.persistence.*;
import lombok.Getter;
import net.furizon.backend.db.entities.users.User;

import java.util.List;

@Entity
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name="media_id", nullable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Getter
    private User mediaOwner;

    @Getter
    @Column(name = "media_type")
    private String type;

    @Getter
    @Column(name = "media_path")
    private String path;

    @OneToMany(mappedBy = "media", fetch = FetchType.EAGER)
    @Getter
    private List<MediaTags> mediaTags;

    @OneToOne(mappedBy = "media", fetch = FetchType.EAGER)
    @Getter
    private Fursuit fursuitOwner;
}
