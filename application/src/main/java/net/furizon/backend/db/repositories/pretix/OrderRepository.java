package net.furizon.backend.db.repositories.pretix;

import net.furizon.backend.db.entities.pretix.DeprOrder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface OrderRepository extends ListCrudRepository<DeprOrder, String> {
    @Query(value = "SELECT O FROM DeprOrder O INNER JOIN DeprEvent E on O.orderEvent = E WHERE O.code = ?1 AND E.slug = ?2")
    Optional<DeprOrder> findByCodeAndEvent(String orderCode, String eventSlug);
}
