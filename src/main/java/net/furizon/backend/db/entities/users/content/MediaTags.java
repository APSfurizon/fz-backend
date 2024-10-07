package net.furizon.backend.db.entities.users.content;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "media_tags")
public class MediaTags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name="media_tag_id", nullable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "media_id")
    @Getter
    private Media media;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    @Getter
    private Tag tag;

}
