package hk.jud.app.lyo.controller.admin;

import hk.jud.app.lyo.entity.Event;
import hk.jud.app.lyo.entity.Guest;
import hk.jud.app.lyo.entity.Invitation;
import hk.jud.app.lyo.entity.enums.InvitationStatus;
import hk.jud.app.lyo.service.EventService;
import hk.jud.app.lyo.service.GuestService;
import hk.jud.app.lyo.service.InvitationService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/events")

public class AdminEventController {

    private final EventService eventService;
    private final GuestService guestService;
    private final InvitationService invitationService;


	@GetMapping
	public String listEvents(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			// @RequestParam(defaultValue = "eventCode,asc") String sort,
			@RequestParam(required = false) String code, Model model) {
		String sort = "eventName,asc";
		String[] sortParams = sort.split(",");
		String sortField = sortParams[0];
		Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
				? Sort.Direction.DESC
				: Sort.Direction.ASC;

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
		Page<Event> eventPage = eventService.findAll(code, pageRequest);

		model.addAttribute("events", eventPage.getContent());
		model.addAttribute("currentPage", eventPage.getNumber());
		model.addAttribute("totalPages", eventPage.getTotalPages());
		model.addAttribute("pageSize", size);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDirection", sortDirection.name().toLowerCase());
		model.addAttribute("searchQuery", code);

		return "events";
	}

	
	@PostMapping("/delete/{id}")
	public String deleteEvent(@PathVariable Integer id) {
		eventService.deleteById(id);
		return "redirect:/events/success";
	}

	@PostMapping("/detail/{id}/invitation")
	public String createInvitation(@PathVariable Integer id, @RequestParam UUID guestId) {
		invitationService.createInvitation(id, guestId);
		return "redirect:/admin/events/detail/" + id;
	}
	@PostMapping("/detail/{id}/invitations")
    public String createInvitations(@PathVariable Integer eventId,
                                   @RequestParam("guestIds") List<UUID> guestIds) {
        invitationService.createInvitations(eventId, guestIds);
        return "redirect:/admin/events/detail/" + eventId;
    }
	
	@GetMapping("/detail/{id}")
    public String eventDetail(@PathVariable Integer id,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "lastUpdateTime,desc") String sort,
                             @RequestParam(defaultValue = "all") String statusFilter,
                             @RequestParam(defaultValue = "all") String guestTypeFilter,
                             @RequestParam(required = false) String search,
                             Model model) {
			

		    
        Event event = eventService.findById(id);
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        //String jpaSortField = sortField.contains("guest.") ? sortField.replace("guest.", "guest_") : sortField;
        String jpaSortField =  sortField;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, jpaSortField));

        Page<Invitation> invitationPage;
        if (search == null && statusFilter.equalsIgnoreCase("all")  && guestTypeFilter.equalsIgnoreCase("all")) {
        
        	invitationPage =  invitationService.findByEventId(id, pageRequest);
        	
        }
        else
        {

            
  
        	invitationPage = invitationService.findByEventIdAndStatusAndGuestTypeAndSearch(id, statusFilter, guestTypeFilter, search, pageRequest);
        	
        }

        // Get IDs of all guests invited to this event
        Set<UUID> invitedGuestIds = invitationService.findAllGuestIdsByEventId(id);

        // Fetch guests not in the invitation list
        List<Guest> availableGuests = guestService.findAllNotIn(invitedGuestIds);
        

        
        model.addAttribute("event", event);
        model.addAttribute("invitations", invitationPage.getContent());
        model.addAttribute("currentPage", invitationPage.getNumber());
        model.addAttribute("totalPages", invitationPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection.name().toLowerCase());
        model.addAttribute("guests", availableGuests);
        model.addAttribute("guestTypeFilter", guestTypeFilter);
         
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchQuery", search);
        model.addAttribute("invitationCount", invitationPage.getTotalElements());


        return "event-detail";
    }
}