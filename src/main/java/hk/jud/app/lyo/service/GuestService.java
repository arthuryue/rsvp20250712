package hk.jud.app.lyo.service;

import hk.jud.app.lyo.dto.GuestCreateDto;
import hk.jud.app.lyo.dto.GuestDto;
import hk.jud.app.lyo.dto.GuestFormDto;
import hk.jud.app.lyo.dto.GuestQrCodeDto;
import hk.jud.app.lyo.entity.Event;
import hk.jud.app.lyo.entity.Guest;
import hk.jud.app.lyo.entity.GuestQrCode;
import hk.jud.app.lyo.entity.ReplyNomination;
import hk.jud.app.lyo.entity.enums.GuestType;
import hk.jud.app.lyo.repository.ApiuserRepository;
import hk.jud.app.lyo.repository.GuestRepository;
import hk.jud.app.lyo.repository.InvitationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class GuestService {

	private static final Logger logger = LoggerFactory.getLogger(GuestService.class);

	private final GuestRepository guestRepository;
	private final SecurityService securityService;

	public GuestDto getGuestByCode(String code) {
		logger.info("Fetching guest with GuestCode: {}", code);

		Guest guest = guestRepository.findByGuestCode(code)
				.orElseThrow(() -> new EntityNotFoundException("Guest with code " + code + " not found"));
		return mapToDto(guest);
	}

	private GuestDto mapToDto(Guest guest) {
		return GuestDto.builder()
				//.id(guest.getId())
				.lastUpdateTime(guest.getLastUpdateTime())
				.emailAddr(guest.getEmailAddr())
				.name(guest.getName())
				.type(guest.getType())
				.title(guest.getTitle())
				.organization(guest.getOrganization())
				.guestCode(guest.getGuestCode())
						.build();
	}

	public Page<Guest> getAllGuests(PageRequest pageRequest) {
		return guestRepository.findAll(pageRequest);
	}

//	public Page<Guest> getAllGuests(PageRequest pageRequest, String type) {
//		return guestRepository.findAll(pageRequest);
//	}

	public Page<Guest> getAllGuests(PageRequest pageRequest, String type, String searchQuery) {
		System.out.println("searchQuery: " + searchQuery  + type);
		GuestType guestTypeEnum = null;
	    if (!"all".equalsIgnoreCase(type) && type != null ) {
	        try {
	        	guestTypeEnum = GuestType.valueOf(type); 
	        } catch (IllegalArgumentException ex) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid guestType value: " + type);
	        }
	    }
		if (searchQuery != null && !searchQuery.trim().isEmpty()) {
			System.out.println("getAllGuests	1" );
			return guestRepository.findByGuestCodeOrEmailAddrOrNameContainingIgnoreCase(searchQuery.trim(),guestTypeEnum,
					pageRequest);
		}
		if (type == null || type.isEmpty() || type.toLowerCase().equals("all")) {
			System.out.println("getAllGuests	2");
			return guestRepository.findAll(pageRequest);
		}
		
		return guestRepository.findByType(guestTypeEnum, pageRequest);
	}

	public List<Guest> searchGuests(String query) {
		return guestRepository.searchGuests(query);
		
	}
//	public List<Guest> searchGuests(String query, Integer eventId) {
//		//return guestRepository.searchGuests(query);
//		return guestRepository.searchGuestsWithoutEventInvitation(query,eventId);
//		
//	}

	public GuestDto createGuest(GuestCreateDto createDto) {
		Guest guest = Guest.builder()
				// .lastUpdateTime(LocalDateTime.now())
				.lastUpdateId(securityService.getCurrentUsername()) // Default // simplicity
				.name(createDto.getName())
				.emailAddr(createDto.getEmailAddr())
				.title(createDto.getTitle())
				.organization(createDto.getOrganization())
				.salutation(createDto.getSalutation())
				.type( GuestType.fromValue(createDto.getType()))
				.guestCode(createDto.getGuestCode())
				.build();
		Guest savedGuest = guestRepository.save(guest);
		return mapToDto(savedGuest);
	}

	public GuestDto updateGuest(UUID id, GuestFormDto formDto) {
		Guest guest = guestRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Guest with id " + id + " not found"));
		guest.setName(formDto.getName());
		guest.setEmailAddr(formDto.getEmailAddr());
		guest.setType(GuestType.fromValue(formDto.getType()));
		guest.setGuestCode(formDto.getGuestCode());
		guest.setTitle(formDto.getTitle());
		guest.setSalutation(formDto.getSalutation());
		guest.setOrganization(formDto.getOrganization());
		guest.setLastUpdateTime(LocalDateTime.now());
		guest.setLastUpdateId("system");
		Guest updatedGuest = guestRepository.save(guest);
		return mapToDto(updatedGuest);
	}

	public Guest findById(UUID id) {
		return guestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Guest not found: " + id));
	}

	public GuestFormDto getGuestFormById(UUID id) {
		logger.info("Fetching guest with Guest ID {}", id);

		Guest guest = guestRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Guest with id " + id + " not found"));
		return GuestFormDto.builder()
				.name(guest.getName())
				.emailAddr(guest.getEmailAddr())
				.type(guest.getType().toString())
				.organization(guest.getOrganization())
				.salutation(guest.getSalutation())
				.title(guest.getTitle())
				.guestCode(guest.getGuestCode())
				.build();
	}

	public void deleteGuest(UUID id) {
		Guest guest = guestRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Guest with id " + id + " not found"));
		guestRepository.delete(guest);
	}

	public List<Guest> findAllNotIn(Set<UUID> guestIds) {
        if (guestIds.isEmpty()) {
            return guestRepository.findAll();
        }
        return guestRepository.findByIdNotIn(guestIds);
    }

//	public Optional<Guest> findByEventIdAndType(Integer eventId, String type) {
//		System.out.println("Debug assignQrCode1.5");
//		 GuestType guestType = GuestType.fromValue(type); // Convert string to enum
//        return guestRepository.findByEventIdAndType(eventId, guestType);
//        
//    }

	public Optional<Guest> findByType(String type) {
		 GuestType guestType = GuestType.fromValue(type); // Convert string to enum
	     return guestRepository.findByType(guestType);
	}

	
	

}