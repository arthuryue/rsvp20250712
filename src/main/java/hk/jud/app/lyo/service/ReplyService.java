package hk.jud.app.lyo.service;

import hk.jud.app.lyo.entity.Event;
import hk.jud.app.lyo.entity.Reply;
import hk.jud.app.lyo.repository.EventRepository;
import hk.jud.app.lyo.repository.ReplyRepository;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;

    public Reply findById(Integer replyId) {
        return replyRepository.findById(replyId)
    			.orElseThrow(() -> new IllegalArgumentException("Reply not found"));
    }
    
    
}