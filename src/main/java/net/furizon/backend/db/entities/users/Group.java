package net.furizon.backend.db.entities.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "groups")
public class Group {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Getter
	@Column(name="group_id", nullable = false)
	private long id;

	@Getter @Setter
	@Column(name = "group_name", nullable = false)
	private String name;

	@OneToMany(mappedBy = "group")
	@Getter
	private List<UserGroup> userGroupAssociations;

}
