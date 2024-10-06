package net.furizon.backend.db.entities.users;

import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;

/**
 * Associates users to groups
 * A user might be in multiple groups
 */
@Entity
@Table(name = "user_group")
public class UserGroup {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Getter
	@Column(name="user_group_id", nullable = false)
	private long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "group_id")
	private Group group;
}
