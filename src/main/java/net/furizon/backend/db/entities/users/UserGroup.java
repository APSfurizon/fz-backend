package net.furizon.backend.db.entities.users;

import jakarta.persistence.*;
import net.furizon.backend.db.CompositeKey;

/**
 * Associates users to groups
 * A user might be in multiple groups
 */
@Entity
@Table(name = "user_group")
@IdClass(CompositeKey.class) // Allow partial primary keys
public class UserGroup {
	@Id
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Id
	@ManyToOne
	@JoinColumn(name = "group_id")
	private Group group;
}
