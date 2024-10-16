package net.furizon.backend.db.entities.users.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// TODO -> Replace on JOOQ

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "tag_id", nullable = false)
    private long id;

    @Column(name = "tag_code", unique = true)
    @Getter
    @Setter
    private String tagCode;

    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    @Getter
    private List<MediaTags> mediaTags;
}
