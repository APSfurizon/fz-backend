package net.furizon.backend.db.repositories.pretix;

import net.furizon.backend.db.entities.pretix.Order;
import org.springframework.data.repository.ListCrudRepository;

public interface IOrderRepository extends ListCrudRepository<Order, Long> {
}
