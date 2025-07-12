package hk.jud.app.lyo.controller.admin;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.zxing.WriterException;

import hk.jud.app.lyo.dto.GuestDto;
import hk.jud.app.lyo.dto.ReplyDetailResponse;
import hk.jud.app.lyo.dto.ReplyDto;
import hk.jud.app.lyo.dto.ReplyNominationDto;
import hk.jud.app.lyo.dto.ReplyTransportDto;
import hk.jud.app.lyo.entity.EmailLog;
import hk.jud.app.lyo.entity.Guest;
import hk.jud.app.lyo.entity.GuestQrCode;
import hk.jud.app.lyo.entity.Invitation;
import hk.jud.app.lyo.entity.Reply;
import hk.jud.app.lyo.entity.ReplyNomination;
import hk.jud.app.lyo.entity.ReplyTransport;
import hk.jud.app.lyo.entity.enums.GuestType;
import hk.jud.app.lyo.entity.enums.InvitationStatus;
import hk.jud.app.lyo.repository.GuestQrCodeRepository;
import hk.jud.app.lyo.repository.ReplyNominationRepository;
import hk.jud.app.lyo.service.EmailLogService;
import hk.jud.app.lyo.service.EmailTemplateService;
import hk.jud.app.lyo.service.GuestService;
import hk.jud.app.lyo.service.InvitationService;
import hk.jud.app.lyo.service.ReplyService;
import hk.jud.app.lyo.service.SecurityService;
import hk.jud.app.lyo.util.RsvpLinkValidator;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminInvitationController {

	private final InvitationService invitationService;
	private final EmailLogService emailLogService;
	private final ReplyService replyService;
	private final EmailTemplateService emailTemplateService;
	private final SecurityService securityService;
	
	private final ReplyNominationRepository replyNominationRepository;
	private final GuestQrCodeRepository guestQrCodeRepository;
	private final GuestService guestService;

	@Value("${server.port}")
	private String serverPort;
	
	
	@GetMapping("/events/invitation/detail/{id}")
	public String invitationDetail(@PathVariable Integer id, Model model) {
		try {
			List<Reply> replies = invitationService.findRepliesByInvitationId(id);
			Invitation invitation = invitationService.findById(id);
			String username = securityService.getCurrentUsername();
			GuestType guestType = invitation.getGuest().getType();
			InvitationStatus invitationStatus = invitation.getStatus();
			List<EmailLog> emailLogs = invitationService.findEmailLogsByInvitationId(id);
			model.addAttribute("invitation", invitation);
			model.addAttribute("username", username);
			model.addAttribute("emailLogs", emailLogs);
			model.addAttribute("replies", replies);
			
			model.addAttribute("showEmailButtons", invitationService.showEmailButtons(guestType,invitationStatus));
			model.addAttribute("showConfirmationButton", 
					invitationService.showConfirmationButton(guestType,invitationStatus)
					&& invitationService.showEmailButtons(guestType,invitationStatus)
					);
			
			model.addAttribute("replyNominations", replyNominationRepository.findByReplyInvitationId(id));
			model.addAttribute("guestQrCodes", guestQrCodeRepository
					.findByEventIdAndGuestOrNomination(invitation.getEvent().getId(), invitation.getGuest().getId()));
			return "invitation-detail";
		} catch (NoSuchElementException e) {
			return "redirect:/admin/events?error=Invitation+not+found";
		}
	}

	@PostMapping("/invitation/detail/{id}")
	public String updateInvitation(@PathVariable Integer id, @RequestParam String status,
			@RequestParam String rsvpLink) {
		try {
//			List<String> validStatuses = Arrays.asList("PENDING", "SENT", "ACCEPTED", "DECLINED");
//			if (!validStatuses.contains(status)) {
//				return "redirect:/admin/events/invitation/detail/" + id + "?error=Invalid+status";
//			}
			
			Invitation invitation = invitationService.findById(id);
			invitation.setStatus(InvitationStatus.fromValue(status));
//			if (rsvpLink.isEmpty())
//			{
//				rsvpLink = "https://"+invitation.getId()+"/"+invitation.getGuest().getId();
//			}
//			
//			invitation.setRsvpLink(rsvpLink);
			
			invitationService.save(invitation);
			return "redirect:/admin/events/invitation/detail/" + id + "?success=Invitation+updated";
		} catch (NoSuchElementException e) {
			return "redirect:/admin/events?error=Invitation+not+found";
		}
	}

	@PostMapping("/invitation/{id}/assignqrcode")
	public String assignQrCodeManually(@PathVariable Integer id) {
		Invitation invitation = invitationService.findById(id);
		Guest guest = invitation.getGuest();
		String qrCode = null;

		// if( List.of("VVIP").contains(guestType)) {

		List<GuestQrCode> existingQrCodes = guestQrCodeRepository.findByEventIdAndGuestId(invitation.getEvent().getId(),
				guest.getId());
		if (!existingQrCodes.isEmpty() && "Y".equalsIgnoreCase(existingQrCodes.get(0).getActiveInd())) {
			qrCode = existingQrCodes.get(0).getQrCode();
			return "redirect:/admin/events/invitation/detail/" + id + "?error=Active+QR+code+already+exists";
		} else {
			qrCode = assignQrCode(invitation);
			if (qrCode == null) {
				return "redirect:/admin/events/invitation/detail/" + id + "?error=No+available+QR+code";
			}
			return "redirect:/admin/events/invitation/detail/" + id + "?success=QR+code+assgned";
		}

	}

	@PostMapping("/invitation/qrcode/{qrCodeId}/inactive")
	public ResponseEntity<activationQRResponse> inactiveQrCode(@PathVariable UUID qrCodeId) {
		try {
			GuestQrCode qrCode = guestQrCodeRepository.findById(qrCodeId)
					.orElseThrow(() -> new IllegalArgumentException("QR code not found"));
			qrCode.setActiveInd("N");
			qrCode.setLastUpdateId(securityService.getCurrentUsername());
			qrCode.setLastUpdateTime(java.time.LocalDateTime.now());
			guestQrCodeRepository.save(qrCode);
			return ResponseEntity.ok(new activationQRResponse(true, "QR code deactivated"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new activationQRResponse(false, e.getMessage()));
		}
	}

	@PostMapping("/invitation/qrcode/{qrCodeId}/active")
	public ResponseEntity<activationQRResponse> activeQrCode(@PathVariable UUID qrCodeId) {
		try {
			GuestQrCode qrCode = guestQrCodeRepository.findById(qrCodeId)
					.orElseThrow(() -> new IllegalArgumentException("QR code not found"));
			qrCode.setActiveInd("Y");
			qrCode.setLastUpdateId(securityService.getCurrentUsername());
			qrCode.setLastUpdateTime(java.time.LocalDateTime.now());
			guestQrCodeRepository.save(qrCode);
			return ResponseEntity.ok(new activationQRResponse(true, "QR code activated"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new activationQRResponse(false, e.getMessage()));
		}
	}

	 @PostMapping("/invitation/detail/{id}/email2")
	    @ResponseBody
	    public ResponseEntity<EmailResponse> createEmailLog2(@PathVariable Integer id, @RequestParam String emailType,
	            @RequestParam String emailAddr, @RequestParam String language,
	            @RequestParam(required = false) List<String> selectedAttachments,
	            @RequestParam(required = false) String isNomination) {
	        try {
	            // Fetch invitation and validate
	            Invitation invitation = invitationService.findById(id);
	            ReplyNomination nomination = null;
	            UUID nomineeId = null;
	            if ("1".equals(isNomination) && "CONFIRMATION".equalsIgnoreCase(emailType)) {
	                nomineeId = UUID.fromString(emailAddr);
	                nomination = replyNominationRepository.getById(nomineeId);
	            }
	            System.out.println("nomination" + isNomination);
	            System.out.println("emailType" + emailType);

	            String qrCode = null;
	            String spouseQrCode = null;
	            Guest guest = invitation.getGuest();
	            GuestType guestType = guest.getType();
	            String emailAddr_real = emailAddr;
	            String rsvpLink = invitation.getRsvpLink();
	            rsvpLink = (rsvpLink != null && !rsvpLink.isEmpty()) ? rsvpLink : InvitationService.defaultRsvpLink(id, guest.getId());

	            // Validate RSVP link for INVITATION emails
	            if ("INVITATION".equalsIgnoreCase(emailType) && InvitationService.isRsvp(guestType)) {
	                String rsvpError = RsvpLinkValidator.getValidationError(rsvpLink);
	                if (rsvpError != null) {
	                    System.err.println("RSVP link validation failed: " + rsvpError);
	                    return ResponseEntity.badRequest()
	                            .body(new EmailResponse(400, rsvpError, null));
	                }
	            }

	            // Assign QR codes for CONFIRMATION emails
	            if ("CONFIRMATION".equalsIgnoreCase(emailType)) {
	                if (nomination != null && isReplyAccepted(invitation)) {
	                	
	                    emailAddr_real = nomination.getEmailAddr();
	                    System.out.println("Assign QR codes for CONFIRMATION emails to Nomination :" + nomination.getName());
	                    List<GuestQrCode> existingQrCodes = guestQrCodeRepository
	                            .findByEventIdAndNominationId(invitation.getEvent().getId(), nomineeId);
	                    if (!existingQrCodes.isEmpty() && "Y".equalsIgnoreCase(existingQrCodes.get(0).getActiveInd())) {
	                        qrCode = existingQrCodes.get(0).getQrCode();
	                    } else {
	                        qrCode = assignQrCodetoNomination(invitation, nomination);
	                        if (qrCode == null) {
	                            System.err.println("No available QR code for nomination");
	                            return ResponseEntity.badRequest()
	                                    .body(new EmailResponse(400, "No available QR code for nomination", null));
	                        }
	                    }
	                } else if (isReplyAccepted(invitation)) {
	                	 System.out.println("Assign QR codes for CONFIRMATION emails to Guest");
	                    emailAddr_real = guest.getEmailAddr();
	                    List<GuestQrCode> existingQrCodes = guestQrCodeRepository
	                            .findByEventIdAndGuestId(invitation.getEvent().getId(), guest.getId());
	                    if (!existingQrCodes.isEmpty() && "Y".equalsIgnoreCase(existingQrCodes.get(0).getActiveInd())) {
	                        qrCode = existingQrCodes.get(0).getQrCode();
	                    } else {
	                        qrCode = assignQrCode(invitation);
	                        if (qrCode == null) {
	                            System.err.println("No available QR code for guest");
	                            return ResponseEntity.badRequest()
	                                    .body(new EmailResponse(400, "No available QR code for guest", null));
	                        }
	                        else System.out.println("Assign New QR codes to Guest");
	                    }
	                    // Check for spouse QR code
	                    List<Reply> replies = invitationService.findRepliesByInvitationId(id);
	                    if (replies.stream().anyMatch(reply -> "Y".equalsIgnoreCase(reply.getSpouseInd()))) {
	                        List<GuestQrCode> existingSpouseQrCodes = guestQrCodeRepository
	                                .findByEventIdAndGuestIdAndSpouse(invitation.getEvent().getId(), guest.getId(), true);
	                        if (!existingSpouseQrCodes.isEmpty() && "Y".equalsIgnoreCase(existingSpouseQrCodes.get(0).getActiveInd())) {
	                            spouseQrCode = existingSpouseQrCodes.get(0).getQrCode();
	                        } else {
	                            spouseQrCode = assignSpouseQrCode(invitation);
	                            if (spouseQrCode == null) {
	                                System.err.println("No available QR code for spouse");
	                                return ResponseEntity.badRequest()
	                                        .body(new EmailResponse(400, "No available QR code for spouse", null));
	                            }
	                            else System.out.println("Assign New QR codes to Guest");
	                            
	                        }
	                    }
	                }
	            } else if ("INVITATION".equalsIgnoreCase(emailType) && !InvitationService.isRsvp(guestType)) {
	                List<GuestQrCode> existingQrCodes = guestQrCodeRepository
	                        .findByEventIdAndGuestId(invitation.getEvent().getId(), guest.getId());
	                if (!existingQrCodes.isEmpty() && "Y".equalsIgnoreCase(existingQrCodes.get(0).getActiveInd())) {
	                    qrCode = existingQrCodes.get(0).getQrCode();
	                } else {
	                    qrCode = assignQrCode(invitation);
	                    if (qrCode == null) {
	                        System.err.println("No available QR code for guest");
	                        return ResponseEntity.badRequest()
	                                .body(new EmailResponse(400, "No available QR code for guest", null));
	                    }
	                }
	            }

	            // Generate email template
	            EmailTemplateService.EmailTemplate template;
	            
	            
	            //Temporarly RSVP link for demo
	            rsvpLink = "http://localhost:" + serverPort +"/rsvp-form/sc"+"?rvsplink="+rsvpLink.replace("https://", "");

	            try {
	                template = emailTemplateService.getTemplateContent(guest, emailType, language, rsvpLink, qrCode, spouseQrCode, nomination);
	            } catch (Exception e) {
	                System.err.println("Failed to generate email template: " + e.getMessage());
	                return ResponseEntity.badRequest()
	                        .body(new EmailResponse(400, "Failed to generate email template: " + e.getMessage(), null));
	            }

	            // Send email and log
	            try {
	                System.out.println("Calling emailLogService.createEmailLog for emailAddr: " + emailAddr_real);
	                emailLogService.createEmailLog(invitation, emailType, emailAddr_real, language, selectedAttachments, template);
	                System.out.println("EmailLogService.createEmailLog completed successfully for emailAddr: " + emailAddr_real);
	                return ResponseEntity.ok(new EmailResponse(200, "Email sent successfully", null));
	            } catch (IllegalArgumentException e) {
	                System.err.println("Caught IllegalArgumentException in controller: " + e.getMessage());
	                e.printStackTrace();
	                return ResponseEntity.badRequest()
	                        .body(new EmailResponse(400, e.getMessage(), null));
	            } catch (Exception e) {
	                System.err.println("Caught unexpected exception in controller: " + e.getMessage());
	                e.printStackTrace();
	                return ResponseEntity.badRequest()
	                        .body(new EmailResponse(400, "Unexpected error occurred while sending email: " + e.getMessage(), null));
	            }
	        } catch (NoSuchElementException e) {
	            System.err.println("Invitation not found: " + e.getMessage());
	            return ResponseEntity.badRequest()
	                    .body(new EmailResponse(400, "Invitation not found", null));
	        } catch (Exception e) {
	            System.err.println("Unexpected error in controller: " + e.getMessage());
	            e.printStackTrace();
	            return ResponseEntity.badRequest()
	                    .body(new EmailResponse(400, "Unexpected error occurred while processing email: " + e.getMessage(), null));
	        }
	    }

	    // Other methods (invitationDetail, updateInvitation, etc.) remain unchanged as they are unrelated to email sending.
	    // ... (omitted for brevity, see prior responses for full code)
	

	@PostMapping("/invitation/detail/{id}/email")
	public String createEmailLog(@PathVariable Integer id, @RequestParam String emailType,
			@RequestParam String emailAddr, @RequestParam String language,
			@RequestParam(required = false) List<String> selectedAttachments,
			@RequestParam(required = false) String isNomination) throws WriterException {
		try {
			Invitation invitation = invitationService.findById(id);
			ReplyNomination nomination = null;

			
			UUID nomineeId = null;
			System.out.println("check________isNomination: " + isNomination);
			if (isNomination.equals("1")) {
				nomineeId = UUID.fromString(emailAddr);

				System.out.println("__________nomineeId: " + nomineeId);
				nomination = replyNominationRepository.getById(nomineeId);
			}

			String qrCode = null;
			String spouseQrCode = null;
			Guest guest = invitation.getGuest();
			GuestType guestType = guest.getType();
			String emailAddr_real = emailAddr;
			String rsvpLink = invitation.getRsvpLink();
			
			rsvpLink = (rsvpLink != null && !rsvpLink.isEmpty())?rsvpLink:InvitationService.defaultRsvpLink(id, guest.getId());
			System.out.println("rsvplink: " +rsvpLink );
			
			// Check guest type if RSVP is needed
			if ("INVITATION".equalsIgnoreCase(emailType)
					&& InvitationService.isRsvp(guestType)) {
				String rsvpError = RsvpLinkValidator.getValidationError(invitation.getRsvpLink());
				if (rsvpError != null) {
					//return "redirect:/admin/events/invitation/detail/" + id + "?error=" + rsvpError.replace(" ", "+");
					String encodedError = URLEncoder.encode(rsvpError, StandardCharsets.UTF_8);
                    return "redirect:/admin/events/invitation/detail/" + id + "?error=" + encodedError;
				}
			}

			// Assign QR code for non-RSVP INVITATION or CONFIRMATION emails
			if ("CONFIRMATION".equalsIgnoreCase(emailType)) {

				System.out.println("***** CONFIRMATION *****");

				// if CONFIRMATION, identify if email is sending to nomination or guest
				// ---- check if Guest or nomination already has assigned active QR for this
				// event
				// ----------if yes, no QR will be assigned

				// Sending to Nomination
				if (nomination != null && isReplyAccepted(invitation)) {
					emailAddr_real = nomination.getEmailAddr();
					System.out.println("Sending to Nomination to " + emailAddr_real);

					List<GuestQrCode> existingQrCodes = guestQrCodeRepository
							.findByEventIdAndNominationId(invitation.getEvent().getId(), nomineeId);

					if (!existingQrCodes.isEmpty() && "Y".equalsIgnoreCase(existingQrCodes.get(0).getActiveInd())) {

						qrCode = existingQrCodes.get(0).getQrCode();
						System.out.println("Existing qrCode: " + qrCode);

					} else {
						qrCode = assignQrCodetoNomination(invitation, nomination);

						if (qrCode == null)
							return "redirect:/admin/events/invitation/detail/" + id + "?error=No+available+QR+code";

						System.out.println("Assign new qrCode: " + qrCode);

					}

				}
				// Sending to Guest
				else if (isReplyAccepted(invitation)) {
					emailAddr_real = guest.getEmailAddr();

					System.out.println("Sending to Guest to " + emailAddr_real);

					List<GuestQrCode> existingQrCodes = guestQrCodeRepository
							.findByEventIdAndGuestId(invitation.getEvent().getId(), guest.getId());
					if (!existingQrCodes.isEmpty() && "Y".equalsIgnoreCase(existingQrCodes.get(0).getActiveInd())) {

						qrCode = existingQrCodes.get(0).getQrCode();
						System.out.println("Existing qrCode: " + qrCode);

					} else {

						qrCode = assignQrCode(invitation);

						if (qrCode == null)
							return "redirect:/admin/events/invitation/detail/" + id + "?error=No+available+QR+code";
						System.out.println("Assign new qrCode: " + qrCode);
					}
					
					// Check for spouse QR code if reply.spouseInd = 'Y'
					List<Reply> replies = invitationService.findRepliesByInvitationId(id);
					if (replies.stream().anyMatch(reply -> "Y".equalsIgnoreCase(reply.getSpouseInd()))) {
						List<GuestQrCode> existingSpouseQrCodes = guestQrCodeRepository
								.findByEventIdAndGuestIdAndSpouse(invitation.getEvent().getId(), guest.getId(), true);
						if (!existingSpouseQrCodes.isEmpty() && "Y".equalsIgnoreCase(existingSpouseQrCodes.get(0).getActiveInd())) {
							spouseQrCode = existingSpouseQrCodes.get(0).getQrCode();
							System.out.println("Existing spouse qrCode: " + spouseQrCode);
						} else {
							spouseQrCode = assignSpouseQrCode(invitation);
							if (spouseQrCode == null)
								return "redirect:/admin/events/invitation/detail/" + id + "?error=No+available+spouse+QR+code";
							System.out.println("Assign new spouse qrCode: " + spouseQrCode);
						}
					}
				}
			}
			// assume Invitation only send to Guest 
			// Assign or attach QR code to invitation
			else if ("INVITATION".equalsIgnoreCase(emailType) && !InvitationService.isRsvp(guestType)) {

				List<GuestQrCode> existingQrCodes = guestQrCodeRepository
						.findByEventIdAndGuestId(invitation.getEvent().getId(), guest.getId());
				if (!existingQrCodes.isEmpty() && "Y".equalsIgnoreCase(existingQrCodes.get(0).getActiveInd())) {
					qrCode = existingQrCodes.get(0).getQrCode();
				} else {
					qrCode = assignQrCode(invitation);
					if (qrCode == null) {
						return "redirect:/admin/events/invitation/detail/" + id + "?error=No+available+QR+code";
					}
				}

			}
			EmailTemplateService.EmailTemplate template = emailTemplateService.getTemplateContent(guest, emailType,
					language, rsvpLink, qrCode, spouseQrCode, nomination != null ? nomination : null);

			// Send email and log
            
            System.out.println("Callinig emailLogService.createEmailLog");
            //emailLogService.createEmailLog(invitation, emailType, emailAddr_real, language, selectedAttachments, template);
           
         // Send email and log
            
            try {
                System.out.println("Calling emailLogService.createEmailLog for emailAddr: " + emailAddr_real);
                emailLogService.createEmailLog(invitation, emailType, emailAddr_real, language, selectedAttachments, template);
                System.out.println("EmailLogService.createEmailLog completed successfully for emailAddr: " + emailAddr_real);
                return "redirect:/admin/events/invitation/detail/" + id + "?success=Email+sent";
            } catch (IllegalArgumentException e) {
                System.err.println("Caught IllegalArgumentException in controller: " + e.getMessage());
                e.printStackTrace(); // Detailed stack trace for debugging
                String encodedError = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
                return "redirect:/admin/events/invitation/detail/" + id + "?error=" + encodedError;
            } catch (Exception e) {
                System.err.println("Caught unexpected exception in controller: " + e.getMessage());
                e.printStackTrace(); // Detailed stack trace for debugging
                String encodedError = URLEncoder.encode("Unexpected error occurred while sending email: " + e.getMessage(), StandardCharsets.UTF_8);
                return "redirect:/admin/events/invitation/detail/" + id + "?error=" + encodedError;
            }
//			emailLogService.createEmailLog(invitation, emailType, emailAddr_real, language, selectedAttachments,
//					template);
//			return "redirect:/admin/events/invitation/detail/" + id + "?success=Email+created";
		
        } catch (Exception e) {
            String encodedError = URLEncoder.encode("Unexpected error occurred while sending email.", StandardCharsets.UTF_8);
            return "redirect:/admin/events/invitation/detail/" + id + "?error=" + encodedError;
        }
		
	}

	@GetMapping("/invitation/detail/{id}/attachments")
	@ResponseBody
	public ResponseEntity<List<String>> getAvailableAttachments(@PathVariable Integer id,
			@RequestParam String emailType, @RequestParam String language) {
		try {
			Invitation invitation = invitationService.findById(id);
			Guest guest = invitation.getGuest();
			GuestType guestType = guest.getType();
			List<String> attachments = emailTemplateService.getAvailableAttachments(guestType, emailType, language);
			return ResponseEntity.ok(attachments);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(List.of());
		}
	}

	@GetMapping("/invitation/detail/{id}/preview")
	@ResponseBody
	public ResponseEntity<EmailTemplateResponse> previewEmail(@PathVariable Integer id, @RequestParam String emailType,
			@RequestParam String language, @RequestParam(required = false) String nomineeId

	) throws WriterException {
		try {
			Invitation invitation = invitationService.findById(id);
			Guest guest = invitation.getGuest();
			GuestType guestType = guest.getType();
			ReplyNomination nomination = null;
			String rsvpLink = invitation.getRsvpLink();
			
			rsvpLink = (rsvpLink != null && !rsvpLink.isEmpty())?rsvpLink:InvitationService.defaultRsvpLink(id, guest.getId());
					
					
			// Validate CONFIRMATION email
			if ("CONFIRMATION".equalsIgnoreCase(emailType) && !isReplyAccepted(invitation)) {
				return ResponseEntity.badRequest().body(
						new EmailTemplateResponse("Error", "Reply not accepted for confirmation email", null, null));
			}

			if (nomineeId != null)
				nomination = replyNominationRepository.getById(UUID.fromString(nomineeId));

			List<String> templatesAttachments = emailTemplateService.getAvailableAttachments(guestType, emailType,
					language);

			EmailTemplateService.EmailTemplate template = emailTemplateService.getTemplateContent(guest, emailType,
					language, rsvpLink, null, null, nomination != null ? nomination : null);

			EmailTemplateResponse response = new EmailTemplateResponse(template.getSubject(), template.getContent(),
					templatesAttachments, nomineeId);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new EmailTemplateResponse("Error", e.getMessage(), null, null));
		}
	}

	private boolean isReplyAccepted(Invitation invitation) {
		List<Reply> replies = invitationService.findRepliesByInvitationId(invitation.getId());
		return replies.stream().anyMatch(reply -> "Y".equalsIgnoreCase(reply.getAttendInd()));
	}

	private String assignQrCodetoNomination(Invitation invitation, ReplyNomination nomination) {
//		Guest adminGuest = guestService.findByEventIdAndType(invitation.getEvent().getId(), "System").orElse(null);
//		if (adminGuest == null) {
//			return null;
//		}
		
		Guest adminGuest = guestService.findByType("SYSTEM").orElse(null);
		if (adminGuest == null) {
			return null;
		}
		List<GuestQrCode> availableQrCodes = guestQrCodeRepository.findByGuestIdAndActiveInd(adminGuest.getId(), "Y");
		if (availableQrCodes.isEmpty()) {
			return null;
		}
		GuestQrCode qrCode = availableQrCodes.get(0);
		if (nomination != null) {
			qrCode.setNomination(nomination);
			qrCode.setGuest(null);
		} else {
			qrCode.setGuest(invitation.getGuest());
		}

		qrCode.setLastUpdateId(securityService.getCurrentUsername());
		qrCode.setLastUpdateTime(java.time.LocalDateTime.now());
		guestQrCodeRepository.save(qrCode);
		return qrCode.getQrCode();
	}

	private String assignQrCode(Invitation invitation) {
		System.out.println("Debug assignQrCode1");
		Guest adminGuest = guestService.findByType("SYSTEM").orElse(null);
		if (adminGuest == null) {
			return null;
		}
		
//		Guest adminGuest = guestService.findByEventIdAndType(invitation.getEvent().getId(), "system").orElse(null);
//		if (adminGuest == null) {
//			return null;
//		}
		System.out.println("Debug assignQrCode2");
		List<GuestQrCode> availableQrCodes = guestQrCodeRepository.findByGuestIdAndActiveInd(adminGuest.getId(), "Y");
		if (availableQrCodes.isEmpty()) {
			return null;
		}
		GuestQrCode qrCode = availableQrCodes.get(0);
		qrCode.setGuest(invitation.getGuest());
		qrCode.setActiveInd("Y");
		qrCode.setLastUpdateId(securityService.getCurrentUsername());
		qrCode.setLastUpdateTime(java.time.LocalDateTime.now());
		guestQrCodeRepository.save(qrCode);
		return qrCode.getQrCode();
	}

	private String assignSpouseQrCode(Invitation invitation) {
		Guest adminGuest = guestService.findByType("SYSTEM").orElse(null);
		if (adminGuest == null) {
			return null;
		}
		
//		Guest adminGuest = guestService.findByEventIdAndType(invitation.getEvent().getId(), "system").orElse(null);
//		if (adminGuest == null) {
//			return null;
//		}
		List<GuestQrCode> availableQrCodes = guestQrCodeRepository.findByGuestIdAndActiveInd(adminGuest.getId(), "Y");
		if (availableQrCodes.isEmpty()) {
			return null;
		}
		GuestQrCode qrCode = availableQrCodes.get(0);
		qrCode.setGuest(invitation.getGuest());
		qrCode.setSpouse(true);
		qrCode.setActiveInd("Y");
		qrCode.setLastUpdateId(securityService.getCurrentUsername());
		qrCode.setLastUpdateTime(java.time.LocalDateTime.now());
		guestQrCodeRepository.save(qrCode);
		return qrCode.getQrCode();
	}
	
//	private String getAvailableQrCode(Invitation invitation) {
//		Guest adminGuest = guestService.findByEventIdAndType(invitation.getEvent().getId(), "System").orElse(null);
//		if (adminGuest == null) {
//			return null;
//		}
//		List<GuestQrCode> availableQrCodes = guestQrCodeRepository.findByGuestIdAndActiveInd(adminGuest.getId(), "Y");
//		return availableQrCodes.isEmpty() ? null : availableQrCodes.get(0).getQrCode();
//	}

	@GetMapping("/invitation/detail/reply/{replyId}")
	@ResponseBody
	public ResponseEntity<ReplyDetailResponse> getReplyDetails(@PathVariable Integer replyId) {
		try {
			Reply reply = replyService.findById(replyId);
			List<ReplyNomination> nominations = invitationService.findNominationsByReplyId(replyId);
			List<ReplyTransport> transports = invitationService.findTransportByReplyId(replyId);

			Guest guest = reply.getInvitation().getGuest();
			GuestDto guestDto = GuestDto.builder().id(guest.getId()).lastUpdateTime(guest.getLastUpdateTime())
					.emailAddr(guest.getEmailAddr()).name(guest.getName()).type(guest.getType())
					.guestCode(guest.getGuestCode()).build();

			ReplyDto replyDto = ReplyDto.builder().id(reply.getId()).lastUpdateTime(reply.getLastUpdateTime())
					.attendInd(reply.getAttendInd()).emailAddr(reply.getEmailAddr()).spouseInd(reply.getSpouseInd())
					.lastUpdateId(reply.getLastUpdateId()).telNo(reply.getTelNo()).guestCode(reply.getGuestCode())
					.build();

			List<ReplyNominationDto> nominationDtos = nominations.stream()
					.map(n -> ReplyNominationDto.builder().id(n.getId()).lastUpdateTime(n.getLastUpdateTime())
							.emailAddr(n.getEmailAddr()).lastUpdateId(n.getLastUpdateId()).name(n.getName())
							.nomineeCode(n.getNomineeCode()).build())
					.collect(Collectors.toList());

			List<ReplyTransportDto> transportDtos = transports.stream()
					.map(t -> ReplyTransportDto.builder().id(t.getId()).lastUpdateTime(t.getLastUpdateTime())
							.awayOpt(t.getAwayOpt()).carRegistrationNo(t.getCarRegistrationNo()).fromOpt(t.getFromOpt())
							.lastUpdateId(t.getLastUpdateId()).ownArrangement(t.getOwnArrangement()).build())
					.collect(Collectors.toList());

			ReplyDetailResponse response = ReplyDetailResponse.builder().reply(replyDto).nominations(nominationDtos)
					.transports(transportDtos).build();

			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(ReplyDetailResponse.builder().error(e.getMessage()).build());
		}
	}

	@GetMapping("/invitation/detail/{id}/nominees")
	@ResponseBody
	public ResponseEntity<NomineeEmailsResponse> getNomineeEmails(@PathVariable Integer id) {
		try {
			Invitation invitation = invitationService.findById(id);
			List<Reply> replies = invitationService.findRepliesByInvitationId(id);
			List<NomineeInfo> nominees = new ArrayList<>();
			for (Reply reply : replies) {
				List<ReplyNomination> nominations = invitationService.findNominationsByReplyId(reply.getId());
				nominees.addAll(nominations.stream().map(n -> new NomineeInfo(n.getId(), n.getEmailAddr(), n.getName()))
						.collect(Collectors.toList()));
			}
			NomineeEmailsResponse response = new NomineeEmailsResponse(
					invitation.getGuest().getId(),
					invitation.getGuest().getEmailAddr(),
					invitation.getGuest().getName(),
					nominees, null);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
					.body(new NomineeEmailsResponse(null, null, null, Collections.emptyList(), e.getMessage()));
		}
	}

	@Data
	@RequiredArgsConstructor
	public static class EmailTemplateResponse {
		private final String subject;
		private final String content;
		private final List<String> attachments;
		private final String reciplientId;

	}

	@Data
	@RequiredArgsConstructor
	public static class NomineeEmailsResponse {

		private final UUID guestId;
		private final String guestEmail;
		private final String guestName;
		private final List<NomineeInfo> nominees;
		private final String error;
	}

	@Data
	@RequiredArgsConstructor
	public static class NomineeInfo {
		private final UUID id;
		private final String emailAddr;
		private final String name;
	}

	@Data
	@RequiredArgsConstructor
	public static class activationQRResponse {
		private final boolean success;
		private final String message;
	}
	
    @Data
    public static class EmailResponse {
        private final int status;
        private final String message;
        private final Object result;
    }
    
}