package hk.jud.app.lyo.service;

import hk.jud.app.lyo.entity.EmailLog;
import hk.jud.app.lyo.entity.Guest;
import hk.jud.app.lyo.entity.Invitation;
import hk.jud.app.lyo.entity.Reply;
import hk.jud.app.lyo.entity.ReplyNomination;
import hk.jud.app.lyo.entity.ReplyTransport;
import hk.jud.app.lyo.entity.enums.GuestType;
import hk.jud.app.lyo.entity.enums.InvitationStatus;
import hk.jud.app.lyo.repository.GuestRepository;
import hk.jud.app.lyo.repository.InvitationRepository;
import hk.jud.app.lyo.repository.ReplyNominationRepository;
import hk.jud.app.lyo.repository.ReplyRepository;
import hk.jud.app.lyo.repository.ReplyTransportRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InvitationService {

	private final InvitationRepository invitationRepository;
	private final ReplyRepository replyRepository;
	private final ReplyTransportRepository replyTransportRepository;
	private final ReplyNominationRepository replyNominationRepository;
	private final EventService eventService;
	private final GuestService guestService;

	private final EmailLogService emailLogService;
	private final SecurityService securityService;

	public Page<Invitation> findByEventId(Integer eventId, Pageable pageable) {
		return invitationRepository.findByEventId(eventId, pageable);
	}

	public Invitation findById(Integer id) {
		return invitationRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invitation not found: " + id));
	}

	public Invitation createInvitation(Integer eventId, UUID guestId) {
		
		LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmss");
        String dateTimeString = now.format(formatter);
        
		String rsvpLink = "https://"+eventId.toString()+"-"+dateTimeString+"/"+guestId.toString();

		
		Invitation invitation = Invitation.builder()
				.status(InvitationStatus.NEW)
				.event(eventService.findById(eventId))
				.guest(guestService.findById(guestId))
				.rsvpLink(rsvpLink)
				.lastUpdateId(securityService.getCurrentUsername()).build();
		return invitationRepository.save(invitation);
	}

	public boolean hasInvitation(UUID guestId, Integer eventId) {
		return invitationRepository.existsByGuestIdAndEventId(guestId, eventId);
	}

	public Set<UUID> findAllGuestIdsByEventId(Integer eventId) {
		return invitationRepository.findGuestIdsByEventId(eventId);
	}

	public void createInvitations(Integer eventId, List<UUID> guestIds) {
		for (UUID guestId : guestIds) {
			createInvitation(eventId, guestId);
		}
	}

	public Page<Invitation> findByEventIdAndStatus(Integer eventId, String status, Pageable pageable) {
		return invitationRepository.findByEventIdAndStatus(eventId, status, pageable);
	}

	public Page<Invitation> findByEventIdAndStatusAndSearch(Integer eventId, String status, String search,
			Pageable pageable) {
		if (status != null && status.equals("all")) {
			status = null;
		}
		return invitationRepository.findByEventIdAndStatusAndSearch(eventId, status, search, pageable);
	}


	
	public Page<Invitation> findByEventIdAndStatusAndGuestTypeAndSearch(
	        Integer eventId, 
	        String statusStr, 
	        String guestType, 
	        String search, 
	        PageRequest pageable) {

	    InvitationStatus statusEnum = null;
	    if (statusStr != null && !"all".equalsIgnoreCase(statusStr)) {
	        try {
	        	statusEnum = InvitationStatus.valueOf(statusStr.toUpperCase());
	        } catch (IllegalArgumentException e) {
	            // handle invalid status string gracefully
	        	statusEnum = null;
	        }
	    }
	    GuestType guestTypeEnum = null;
	    if (!"all".equalsIgnoreCase(guestType)) {
	        try {
	        	guestTypeEnum = GuestType.valueOf(guestType); 
	        } catch (IllegalArgumentException ex) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid guestType value: " + guestType);
	        }
	    }
//	    if (guestType != null && guestType.equalsIgnoreCase("all")) {
//	        guestType = null;
//	    }

	    return invitationRepository.findByEventIdAndStatusAndGuestTypeAndSearch(
	            eventId, statusEnum, guestTypeEnum, search, pageable);
	}
	
	
	@Transactional
	public void save(Invitation invitation) {
		invitationRepository.save(invitation);
	}

	public List<EmailLog> findEmailLogsByInvitationId(Integer invitationId) {
		return emailLogService.findByInvitationId(invitationId);
	}

	public List<Reply> findRepliesByInvitationId(Integer invitationId) {
		return replyRepository.findByInvitationId(invitationId, Sort.by(Sort.Direction.DESC, "lastUpdateTime"));
	}
	public List<ReplyNomination> findNominationsByReplyId(Integer replyId) {
        return replyNominationRepository.findByReplyId(replyId);
    }

    public List<ReplyTransport> findTransportByReplyId(Integer replyId) {
        return replyTransportRepository.findByReplyId(replyId);
    }

    public List<Guest> findGuestsByEventId(Integer eventId) {
        return invitationRepository.findGuestsByEventId(eventId);
    }

	public boolean showCofirmationButton(GuestType guestType)
	{
		if (!isRsvp(guestType))
			return true;
		
		return false;
	}
	public boolean showEmailButtons(GuestType guestType, InvitationStatus invitationStatus) {
	    // Guests with RSVP AND status != DECLINED
	    //return isRsvp(guestType) && invitationStatus != InvitationStatus.DECLINED;
		return  invitationStatus != InvitationStatus.DECLINED;
	}
	public boolean showConfirmationButton(GuestType guestType, InvitationStatus invitationStatus) {
	    // Only for RSVP guests who have ACCEPTED the invitation (PENDING)
	    return isRsvp(guestType) && (invitationStatus == InvitationStatus.PENDING || invitationStatus == InvitationStatus.CONFIRMED);
	}

	public static boolean isRsvp(GuestType guestType) {
	    return !(guestType == GuestType.GENERAL_GUEST1b || guestType == GuestType.GENERAL_GUEST2b);
	}
	
	public static String defaultRsvpLink(Integer invitationId, UUID guestId) {
	    return "https://" + invitationId.toString()+ "/" +guestId.toString();
	}

}