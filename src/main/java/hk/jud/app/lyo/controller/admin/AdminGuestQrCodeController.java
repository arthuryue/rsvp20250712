package hk.jud.app.lyo.controller.admin;

import hk.jud.app.lyo.entity.Guest;
import hk.jud.app.lyo.entity.GuestQrCode;
import hk.jud.app.lyo.repository.GuestQrCodeRepository;
import hk.jud.app.lyo.service.EventService;
import hk.jud.app.lyo.service.GuestQrCodeService;
import hk.jud.app.lyo.service.GuestService;
import hk.jud.app.lyo.service.InvitationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.zxing.WriterException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/qrcodes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGuestQrCodeController {

	@Autowired
	private GuestQrCodeService service;

	@Autowired
	private EventService eventService;

	@Autowired
	private InvitationService invitationService;

	@Autowired
	 private  GuestQrCodeRepository guestQrCodeRepository;
	
	@Autowired
	private  GuestQrCodeService guestQrCodeService;

	    
	    
	@GetMapping
	public String list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "qrCode,asc") String sort, @RequestParam(required = false) String searchQuery,
			@RequestParam(required = false) String activeInd, Model model) {
		try {
			String[] sortParams = sort.split(",");
			String sortField = sortParams[0];
			Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
			Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
			Page<GuestQrCode> qrcodes = service.findAll(searchQuery, activeInd, pageable);
			System.out.println("QR Codes Page: " + qrcodes.getNumber() + ", Total: " + qrcodes.getTotalElements());
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", qrcodes.getTotalPages());
			model.addAttribute("totalItems", qrcodes.getTotalElements());
			model.addAttribute("pageSize", size);
			model.addAttribute("activeInd", activeInd != null ? activeInd : "");

			model.addAttribute("qrcodes", qrcodes);
			model.addAttribute("sortField", sortField);
			model.addAttribute("sortDirection", sortDirection.toString().toLowerCase());
		} catch (Exception e) {
			System.err.println("Error in list: " + e.getMessage());
			model.addAttribute("qrcodes", null);
			model.addAttribute("error", "Failed to load QR Codes: " + e.getMessage());
		}
		return "qrcodes";
	}

	@GetMapping("/detail/{id}")
	public String detail(@PathVariable UUID id, Model model) {
		GuestQrCode qrCode = service.findById(id).orElseThrow(() -> new IllegalArgumentException("QR Code not found"));
		model.addAttribute("qrcode", qrCode);
		return "qrcode-detail";
	}

	@GetMapping("/update/{id}")
	public String updateForm(@PathVariable UUID id, Model model) {
		GuestQrCode qrCode = service.findById(id).orElseThrow(() -> new IllegalArgumentException("QR Code not found"));
		model.addAttribute("qrcode", qrCode);
		return "qrcode-update";
	}

//	@PostMapping("/update/{id}")
//	public String update(@PathVariable UUID id, GuestQrCode qrCode, RedirectAttributes redirectAttributes) {
//		try {
//			GuestQrCode existing = service.findById(id)
//					.orElseThrow(() -> new IllegalArgumentException("QR Code not found"));
//			existing.setQrCode(qrCode.getQrCode());
//			existing.setQrCode2(qrCode.getQrCode2());
//			existing.setActiveInd(qrCode.getActiveInd());
//			existing.setLastUpdateId("admin");
//			existing.setLastUpdateTime(LocalDateTime.now());
//			service.save(existing);
//			redirectAttributes.addAttribute("success", "QR Code updated");
//			return "redirect:/admin/qrcodes/detail/" + id;
//		} catch (Exception e) {
//			redirectAttributes.addAttribute("error", "Failed to update QR Code: " + e.getMessage());
//			return "redirect:/admin/qrcodes/detial/" + id;
//		}
//	}

	@GetMapping("/new")
	public String addForm(Model model) {
		model.addAttribute("qrcode", new GuestQrCode());
		model.addAttribute("events", eventService.findAll());
		return "qrcode-add";
	}

	@GetMapping("/guests-by-event")
	@ResponseBody
	public List<Guest> getGuestsByEvent(@RequestParam Integer eventId) {
		try {
			List<Guest> guests = invitationService.findGuestsByEventId(eventId);
			System.out.println("Guests for Event ID=" + eventId + ": " + guests.size());
			return guests;
		} catch (Exception e) {
			System.err.println("Error fetching guests for event " + eventId + ": " + e.getMessage());
			return List.of();
		}
	}

	@PostMapping("/add")
	public String add(GuestQrCode qrCode, RedirectAttributes redirectAttributes) {
		try {
			// Check for duplicate QR code
	        if (service.existsByQrCode(qrCode.getQrCode())) {
	            redirectAttributes.addAttribute("error", "QR Code already exists: " + qrCode.getQrCode());
	            return "redirect:/admin/qrcodes/new";
	        }
	        
			qrCode.setLastUpdateId("admin");
			qrCode.setLastUpdateTime(LocalDateTime.now());
			service.save(qrCode);
			redirectAttributes.addAttribute("success", "QR Code created");
			return "redirect:/admin/qrcodes";
		} catch (Exception e) {
			redirectAttributes.addAttribute("error", "Failed to create QR Code: " + e.getMessage());
			return "redirect:/admin/qrcodes/new";
		}
	}
	
	 @GetMapping(value = "/view/{id}/", produces = MediaType.IMAGE_JPEG_VALUE)
	    public ResponseEntity<byte[]> viewQrCode(@PathVariable String id) throws WriterException, IOException {
	        GuestQrCode guestQrCode = guestQrCodeRepository.getById(UUID.fromString(id));
	        byte[] qrCodeImage = guestQrCodeService.generateQRCodeBytes(guestQrCode.getQrCode());
	        return ResponseEntity.ok()
	                .contentType(MediaType.IMAGE_JPEG)
	                .body(qrCodeImage);
	    }
}