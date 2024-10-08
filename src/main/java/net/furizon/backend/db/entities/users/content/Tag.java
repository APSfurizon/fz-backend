package net.furizon.backend.db.entities.users.content;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name="tag_id", nullable = false)
    private long id;

    @Column(name = "tag_code", unique = true)
    @Getter @Setter
    private String tagCode;

    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    @Getter
    private List<MediaTags> mediaTags;

}
