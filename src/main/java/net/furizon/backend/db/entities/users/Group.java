package net.furizon.backend.db.entities.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "groups")
@Getter
public class Group {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="group_id", nullable = false)
	private long id;

	@Setter
	@Column(name = "group_name", nullable = false)
	private String name;

	@OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
	private List<UserGroup> userGroupAssociations;

}
