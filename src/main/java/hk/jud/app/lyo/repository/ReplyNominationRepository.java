package hk.jud.app.lyo.repository;

import hk.jud.app.lyo.entity.ReplyNomination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReplyNominationRepository extends JpaRepository<ReplyNomination, UUID> {
    List<ReplyNomination> findByReplyId(Integer replyId);

    @Query("SELECT rn FROM ReplyNomination rn WHERE rn.reply.invitation.id = :invitationId")
    List<ReplyNomination> findByReplyInvitationId(@Param("invitationId") Integer invitationId);
	
	
}