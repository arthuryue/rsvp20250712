package hk.jud.app.lyo.repository;

import hk.jud.app.lyo.entity.ReplyTransport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyTransportRepository extends JpaRepository<ReplyTransport, Integer> {
    List<ReplyTransport> findByReplyId(Integer replyId);
}