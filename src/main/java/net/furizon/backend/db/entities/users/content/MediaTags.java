package net.furizon.backend.db.entities.users.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

// TODO -> Replace on JOOQ
@Entity
@Table(name = "media_tags")
public class MediaTags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "media_tag_id", nullable = false)
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
