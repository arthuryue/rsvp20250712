package hk.jud.app.lyo.repository;

import java.util.List;

// Repository for Reply

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hk.jud.app.lyo.entity.Invitation;
import hk.jud.app.lyo.entity.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
	 List<Reply> findByInvitationId(Integer invitationId, org.springframework.data.domain.Sort sort);

	 boolean existsByInvitation(Invitation invitation);
}
