package net.furizon.backend.db.repositories.pretix;

import net.furizon.backend.db.entities.pretix.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface IOrderRepository extends ListCrudRepository<Order, Long> {
    @Query(value = "SELECT O FROM Order O WHERE O.code = ?1")
    Optional<Order> findByCode(String code);
}
