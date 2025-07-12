package hk.jud.app.lyo.repository;

import hk.jud.app.lyo.entity.GuestQrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GuestQrCodeRepository extends JpaRepository<GuestQrCode, UUID>, JpaSpecificationExecutor<GuestQrCode> {

	@Query("SELECT q FROM GuestQrCode q " 
		+ "WHERE q.event.id = :eventId AND " + "(q.guest.id = :guestId OR "		
		+ "q.nomination IN (SELECT rn FROM ReplyNomination rn WHERE rn.reply.invitation.guest.id = :guestId))")
	List<GuestQrCode> findByEventIdAndGuestOrNomination(@Param("eventId") Integer eventId, @Param("guestId") UUID uuid);

	List<GuestQrCode> findByGuestIdAndActiveInd(UUID id, String string);

	@Query("SELECT q FROM GuestQrCode q " 
	        + "WHERE q.event.id = :eventId AND q.guest.id = :guestId AND q.activeInd = 'Y'")
	    List<GuestQrCode> findByEventIdAndGuestId(@Param("eventId") Integer eventId, @Param("guestId") UUID uuid);

	    @Query("SELECT q FROM GuestQrCode q " 
	        + "WHERE q.event.id = :eventId " 
	        + "AND  q.nomination.id = :nominationId AND q.activeInd = 'Y'")
	    List<GuestQrCode> findByEventIdAndNominationId(@Param("eventId") Integer eventId, @Param("nominationId") UUID uuid);

		boolean existsByQrCode(String qrCode);

		List<GuestQrCode> findByEventIdAndGuestIdAndSpouse(Integer id, UUID id2, boolean b);

	}


