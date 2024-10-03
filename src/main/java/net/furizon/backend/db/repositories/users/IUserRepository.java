package net.furizon.backend.db.repositories.users;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import net.furizon.backend.db.entities.users.User;

import java.util.List;

public interface IUserRepository extends ListCrudRepository<User, Long> {

	@Query(value = "SELECT U FROM User U WHERE U.firstName LIKE '%?1%' OR U.lastName LIKE '%?2%'")
	List<User> findByName(String firstName, String lastName);

	@Query(value = "SELECT U FROM User U WHERE U.authentication.email = ?1")
	User findByEmail(String email);

}