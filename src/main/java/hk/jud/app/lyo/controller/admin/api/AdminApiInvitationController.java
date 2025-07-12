package hk.jud.app.lyo.controller.admin.api;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hk.jud.app.lyo.dto.GuestInvitationDto;
import hk.jud.app.lyo.entity.Guest;
import hk.jud.app.lyo.entity.Invitation;
import hk.jud.app.lyo.service.GuestService;
import hk.jud.app.lyo.service.InvitationService;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminApiInvitationController {
	private final GuestService guestService;
	private final InvitationService invitationService;


	@GetMapping("/invitation/guests/search")
	public List<GuestInvitationDto> searchGuests(@RequestParam String q, @RequestParam Integer eventId) {
        List<Guest> guests = guestService.searchGuests(q);
        return guests.stream().map(guest -> new GuestInvitationDto(
            guest.getId(),
            guest.getGuestCode(),
            guest.getName(),
            guest.getEmailAddr(),
            guest.getType(),
            guest.getType().getLabel(),
            invitationService.hasInvitation(guest.getId(), eventId)
        )).collect(Collectors.toList());
    }

	

}