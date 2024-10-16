package net.furizon.backend.db.repositories.users;

import net.furizon.backend.db.entities.users.AuthenticationData;
import org.springframework.data.repository.ListCrudRepository;

public interface AuthenticationDataRepository extends ListCrudRepository<AuthenticationData, Long> {

}
