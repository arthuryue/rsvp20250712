package hk.jud.app.lyo.repository;

import hk.jud.app.lyo.entity.Guest;
import hk.jud.app.lyo.entity.Invitation;
import hk.jud.app.lyo.entity.enums.GuestType;
import hk.jud.app.lyo.entity.enums.InvitationStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvitationRepository extends JpaRepository<Invitation, Integer> {
	Optional<Invitation> findById(Integer id);
	
	Page<Invitation> findByEventId(Integer eventId, Pageable pageable);

	boolean existsByGuestIdAndEventId(UUID guestId, Integer eventId);

	@Query("SELECT i.guest.id FROM Invitation i WHERE i.event.id = :eventId")
	Set<UUID> findGuestIdsByEventId(Integer eventId);

	Page<Invitation> findByEventIdAndStatus(Integer eventId, String status, Pageable pageable);

	@Query("SELECT i FROM Invitation i WHERE i.event.id = :eventId " +
		       "AND (:status IS NULL OR i.status = :status) " +
		       "AND (:search IS NULL OR " +
		       "LOWER(i.guest.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
		       "LOWER(i.guest.guestCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
		       "LOWER(i.guest.emailAddr) LIKE LOWER(CONCAT('%', :search, '%')))")
		Page<Invitation> findByEventIdAndStatusAndSearch(@Param("eventId") Integer eventId, 
		                                                @Param("status") String status, 
		                                                @Param("search") String search, 
		                                                Pageable pageable);

	//List<Guest> findGuestsByEventId(Integer eventId);
	@Query("SELECT g FROM Guest g JOIN FETCH g.invitations i JOIN FETCH i.event WHERE i.event.id = :eventId")
	List<Guest> findGuestsByEventId(@Param("eventId") Integer eventId);
	
	Invitation findByRsvpLink(String rsvpLink);

	@Query("SELECT i FROM Invitation i WHERE i.event.id = :eventId " +
		       "AND (:status IS NULL OR i.status = :status) " +
		       "AND (:guestType IS NULL OR i.guest.type = :guestType) " +
		       "AND (:search IS NULL OR " +
		       "LOWER(i.guest.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
		       "LOWER(i.guest.guestCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
		       "LOWER(i.guest.emailAddr) LIKE LOWER(CONCAT('%', :search, '%')))")
		Page<Invitation> findByEventIdAndStatusAndGuestTypeAndSearch(@Param("eventId") Integer eventId, 
		                                                @Param("status") String status, 
		                                                @Param("guestType") String guestType, 
		                                                @Param("search") String search, 
		                                                Pageable pageable);

	@Query("SELECT i FROM Invitation i WHERE i.event.id = :eventId " +
		       "AND (:status IS NULL OR i.status = :status) " +
		       "AND (:guestType IS NULL OR i.guest.type = :guestType) " +
		       "AND (:search IS NULL OR " +
		       "LOWER(i.guest.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
		       "LOWER(i.guest.guestCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
		       "LOWER(i.guest.emailAddr) LIKE LOWER(CONCAT('%', :search, '%')))")
	Page<Invitation> findByEventIdAndStatusAndGuestTypeAndSearch(Integer eventId, InvitationStatus status,
			GuestType guestType, String search, PageRequest pageable);
	

}