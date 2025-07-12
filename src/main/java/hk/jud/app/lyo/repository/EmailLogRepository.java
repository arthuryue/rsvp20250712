package hk.jud.app.lyo.repository;

import hk.jud.app.lyo.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailLogRepository extends JpaRepository<EmailLog, Integer> {
    List<EmailLog> findByInvitationId(Integer invitationId, org.springframework.data.domain.Sort sort);
}