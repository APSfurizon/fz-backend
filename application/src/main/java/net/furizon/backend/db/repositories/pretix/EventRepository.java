package net.furizon.backend.db.repositories.pretix;

import net.furizon.backend.db.entities.pretix.Event;
import org.springframework.data.repository.ListCrudRepository;

public interface EventRepository extends ListCrudRepository<Event, String> {
}