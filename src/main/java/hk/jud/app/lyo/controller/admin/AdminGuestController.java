package hk.jud.app.lyo.controller.admin;

import hk.jud.app.lyo.dto.GuestCreateDto;
import hk.jud.app.lyo.dto.GuestFormDto;
import hk.jud.app.lyo.entity.Guest;
import hk.jud.app.lyo.service.GuestService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminGuestController {
	private static final Logger logger = LoggerFactory.getLogger(AdminGuestController.class);
    private final GuestService guestService;
   

    
    
    @GetMapping("/guests/search")
    public String searchGuests(@RequestParam String query, Model model) {
        model.addAttribute("guests", guestService.searchGuests(query));
        model.addAttribute("query", query);
        return "guests";
    }
    @GetMapping("/guests")
    public String listGuests(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "name,asc") String sort,
                             @RequestParam(defaultValue = "all", required = false) String type,
                             @RequestParam(required = false, name = "code") String searchQuery,
                             HttpServletRequest request,
                             Model model) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Guest> guestPage = guestService.getAllGuests(pageRequest, type, searchQuery);
        model.addAttribute("guests", guestPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", guestPage.getTotalPages());
        model.addAttribute("totalItems", guestPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection.name().toLowerCase());
        model.addAttribute("type", type != null ? type : "");
        model.addAttribute("searchQuery", searchQuery != null ? searchQuery : "");
        String uri = request.getRequestURI();
        if (request.getAttribute("jakarta.servlet.forward.request_uri") != null) {
            uri = (String) request.getAttribute("jakarta.servlet.forward.request_uri");
        }
        model.addAttribute("requestURI", uri);
        return "guests";
    }
    @GetMapping("/guests/new")
    public String showCreateGuestForm(Model model) {
        model.addAttribute("guestCreateDto", new GuestCreateDto());
        return "create-guest";
    }

    
    @PostMapping("/guests/create")
    public String createGuest(@Valid @ModelAttribute("guestCreateDto") GuestCreateDto guestCreateDto,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "create-guest";
        }
        if (guestCreateDto.getGuestCode()==null)
        {
        	LocalDateTime now = LocalDateTime.now();
            String dateTimeString = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            byte[] hashBytes = digest.digest(dateTimeString.getBytes(StandardCharsets.UTF_8));
            
            String hashString = HexFormat.of().formatHex(hashBytes);
            
        	guestCreateDto.setGuestCode(hashString);
        }
        
        
        guestService.createGuest(guestCreateDto);
        return "redirect:/admin/guests/create/success";
    }
    @GetMapping("/guests/create/success")
    public String showGuestCreateSuccess() {
        return "create-success";
    }
	@GetMapping("/guests/update/success")
	public String showGuestUpdateSuccess() {
	    return "update-success";
	}
	@GetMapping("/guests/delete/success")
	public String showGuestDeleteSuccess() {
	    return "delete-success";
	}
    @GetMapping("/guests/edit/{id}")
    public String showEditGuestForm(@PathVariable UUID id, Model model) {
        try {
        	GuestFormDto guestFormDto = guestService.getGuestFormById(id);
            model.addAttribute("guestFormDto", guestFormDto);
            model.addAttribute("id", id);
            return "guest-edit";
        } catch (EntityNotFoundException e) {
            return "redirect:/admin/guest/edit/{id}";
        }
    }

    @PostMapping("/guests/edit/{id}")
    public String updateGuest(@PathVariable UUID id,
                             @Valid @ModelAttribute("guestFormDto") GuestFormDto guestFormDto,
                             BindingResult result, Model model
                             , RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("guestFormDto", guestFormDto);
            model.addAttribute("id", id);
            
            return "edit-guest";
        }
        try {
            guestService.updateGuest(id, guestFormDto);
            redirectAttributes.addAttribute("success", "Updated guest successfully");
	
            return "redirect:/admin/guests/edit/{id}";
        } catch (EntityNotFoundException e) {
            return "error";
        }
    }
    @PostMapping("/guests/delete/{id}")
    public String deleteGuest(@PathVariable UUID id) {
        try {
            guestService.deleteGuest(id);
            return "redirect:/admin/guests/delete/success";
        } catch (EntityNotFoundException e) {
            return "redirect:/admin/guests";
        }
    }
    
	

   
}