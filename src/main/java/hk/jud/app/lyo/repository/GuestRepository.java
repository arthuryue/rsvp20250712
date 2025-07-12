package hk.jud.app.lyo.repository;

import hk.jud.app.lyo.entity.Guest;
import hk.jud.app.lyo.entity.enums.GuestType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface GuestRepository extends JpaRepository<Guest, UUID> {
	Optional<Guest> findById(UUID id);

	Optional<Guest> findByGuestCode(String code);

	@Query("SELECT g FROM Guest g WHERE LOWER(g.guestCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(g.emailAddr) LIKE LOWER(CONCAT('%', :query, '%'))")
	List<Guest> searchGuests(@Param("query") String query);

	
	
	
	//Page<Guest> findByType(String type, PageRequest pageRequest);

	@Query("SELECT g FROM Guest g WHERE "
			+ "(:type IS NULL OR :type = '' OR :type = 'ALL' OR g.type = :type) AND "
			+ "(LOWER(g.guestCode) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR "
			+ "LOWER(g.emailAddr) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR "
			+ "LOWER(g.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
	Page<Guest> findByGuestCodeOrEmailAddrOrNameContainingIgnoreCase(@Param("searchQuery") String searchQuery,
			@Param("type") GuestType guestType, PageRequest pageRequest);

	@Query("SELECT g FROM Guest g WHERE LOWER(g.guestCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))")
	List<Guest> findByGuestCodeContainingIgnoreCaseOrNameContainingIgnoreCase(@Param("query") String query);

	List<Guest> findByIdNotIn(Set<UUID> ids);

//	@Query("SELECT g FROM Guest g JOIN g.invitations i WHERE i.event.id = :eventId AND g.type = :type")
//	Optional<Guest> findByEventIdAndType(@Param("eventId") Integer eventId, @Param("type") GuestType type);

	Optional<Guest> findByType(GuestType type);
	
	Page<Guest> findByType(GuestType type, PageRequest pageRequest);

}