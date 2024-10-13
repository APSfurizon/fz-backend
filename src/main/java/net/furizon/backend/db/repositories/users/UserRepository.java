package net.furizon.backend.db.repositories.users;

import net.furizon.backend.db.entities.users.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends ListCrudRepository<User, Long> {
    @Query(value = "SELECT U FROM User U WHERE U.firstName LIKE '%?1%' OR U.lastName LIKE '%?2%'")
    List<User> findByName(@NotNull String firstName, @NotNull String lastName);

    @Query(value = "SELECT U FROM User U WHERE U.authentication.email = ?1")
    Optional<User> findByEmail(@NotNull String email);
}
