package hk.jud.app.lyo.repository;

import hk.jud.app.lyo.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Integer> {
    //Page<Event> findByEventCodeContainingIgnoreCaseOrEventNameContainingIgnoreCase(String eventCode, String eventName, Pageable pageable);
}