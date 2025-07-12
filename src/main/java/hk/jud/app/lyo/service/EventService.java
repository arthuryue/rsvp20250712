package hk.jud.app.lyo.service;

import hk.jud.app.lyo.entity.Event;
import hk.jud.app.lyo.repository.EventRepository;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;



    public Event findById(Integer id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
    }
    public List<Event> findAll() {
        return eventRepository.findAll();
    }
    
    public Page<Event> findAll(String searchQuery, PageRequest pageRequest) {
        //if (searchQuery != null && !searchQuery.trim().isEmpty()) {
        //    return eventRepository.findByEventCodeContainingIgnoreCaseOrEventNameContainingIgnoreCase(searchQuery, searchQuery, pageRequest);
        //}
        return eventRepository.findAll(pageRequest);
    }

    public void deleteById(Integer id) {
        eventRepository.deleteById(id);
    }
}