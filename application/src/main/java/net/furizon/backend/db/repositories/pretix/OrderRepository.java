package net.furizon.backend.db.repositories.pretix;

import net.furizon.backend.db.entities.pretix.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface OrderRepository extends ListCrudRepository<Order, String> {
    @Query(value = "SELECT O FROM Order O INNER JOIN Event E on O.orderEvent = E WHERE O.code = ?1 AND E.slug = ?2")
    Optional<Order> findByCodeAndEvent(String orderCode, String eventSlug);
}
