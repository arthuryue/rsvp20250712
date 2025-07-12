package hk.jud.app.lyo.controller.publicapi;

import hk.jud.app.lyo.entity.*;
import hk.jud.app.lyo.entity.enums.InvitationStatus;
import hk.jud.app.lyo.repository.*;
import hk.jud.app.lyo.service.EmailLogService;
import hk.jud.app.lyo.service.EmailTemplateService;
import hk.jud.app.lyo.service.InvitationService;
import hk.jud.app.lyo.service.ReplyService;
import hk.jud.app.lyo.service.SecurityService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/public/reply")
@RequiredArgsConstructor
public class ReplyController {

    private final InvitationRepository invitationRepository;
    private final ReplyRepository replyRepository;
    private final ReplyDataRepository replyDataRepository;
    private final GuestQrCodeRepository guestQrCodeRepository;
    private final ReplyNominationRepository replyNominationRepository;
    private final ReplyTransportRepository replyTransportRepository;
    private final ObjectMapper objectMapper;

    

    @Data
    public static class ReplyRequest {
        private String rsvpLink; // To match Invitation
        private String attendInd;
        private String emailAddr;
        private String telNo;
        private String guestCode;
        private String lastUpdateId;
        private String guestQrCode; // QR code for guest
        private String spouseInd;
        private String spouseQrCode; // QR code for guest
        private List<NominationData> nominations; // For ReplyNomination
        private TransportData transport; // For ReplyTransport
    }

    @Data
    public static class NominationData {
        private String emailAddr;
        private String name;
        private String nomineeQrCode; // QR code for this nominee
    }

    @Data
    public static class TransportData {
        private String awayOpt;
        private String carRegistrationNo;
        private String fromOpt;
        private String postAddress;
        private String remarks;
        private String ownArrangement;
    }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<String> createReply(@Valid @RequestBody ReplyRequest request) {
    	Logger logger = LoggerFactory.getLogger(this.getClass());
    	
        try {
            // Find Invitation by rsvpLink
            Invitation invitation = invitationRepository.findByRsvpLink(request.getRsvpLink());
            if (invitation == null) {
                return ResponseEntity.badRequest().body("Invalid RSVP link");
            }

            // Check if a reply already exists for this invitation
            if (replyRepository.existsByInvitation(invitation)) {
            	 logger.warn("Reply already exists for invitation ID: {}", invitation.getId());
                return ResponseEntity.badRequest().body("A reply already exists for this invitation");
            }
           
            // Update Invitation status based on attendInd
            if ("Y".equals(request.getAttendInd())) {
                invitation.setStatus(InvitationStatus.PENDING);
            } else if ("N".equals(request.getAttendInd())) {
                invitation.setStatus(InvitationStatus.DECLINED);
            }
            invitation.setLastUpdateTime(LocalDateTime.now());
            invitationRepository.save(invitation);
            
            
            // Create Reply
            Reply reply = Reply.builder()
                    .attendInd(request.getAttendInd())
                    .emailAddr(request.getEmailAddr())
                    .telNo(request.getTelNo())
                    //.guestCode(request.getGuestCode())
                    .guestCode(invitation.getGuest().getGuestCode())
                    .spouseInd(request.getSpouseInd())
                    .lastUpdateId("system")
                    .lastUpdateTime(LocalDateTime.now())
                    .invitation(invitation)
                    .build();

            replyRepository.save(reply);

           
            // Serialize the entire request to JSON for ReplyData
            String requestJson;
            try {
                requestJson = objectMapper.writeValueAsString(request);
            } catch (Exception e) {
                logger.error("Failed to serialize request to JSON: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to serialize request to JSON", e);
            }
            
            

            // Create ReplyData
            ReplyData replyData = ReplyData.builder()
                    .json(requestJson)
                    .lastUpdateId("system")
                    .lastUpdateTime(LocalDateTime.now())
                    .reply(reply)
                    .build();

            replyDataRepository.save(replyData);
            reply.setReplyData(replyData);
            replyRepository.save(reply); // Update reply with replyData

            // Create GuestQrCode for Guest
            if (request.getGuestQrCode() != null && !request.getGuestQrCode().isEmpty()) {

            	GuestQrCode guestQrCode = GuestQrCode.builder()
                    .guest(invitation.getGuest())
                    .event(invitation.getEvent())
                    .qrCode(request.getGuestQrCode())
                    .activeInd("Y") // Active by default
                    .lastUpdateId("system")
                    .lastUpdateTime(LocalDateTime.now())
                    .build();

            	guestQrCodeRepository.save(guestQrCode);
            }
            // Create GuestQrCode for Spouse if spouse = "Y" and spouseQrCode is provided
            if ("Y".equals(request.getSpouseInd()) && request.getSpouseQrCode() != null && !request.getSpouseQrCode().isEmpty()) {
                GuestQrCode spouseQrCode = GuestQrCode.builder()
                        .guest(invitation.getGuest())
                        .event(invitation.getEvent())
                        .qrCode(request.getSpouseQrCode())
                        .spouse(true) // Set spouse = true
                        .activeInd("Y")
                        .lastUpdateId("system")
                        .lastUpdateTime(LocalDateTime.now())
                        .build();
                guestQrCodeRepository.save(spouseQrCode);
            }
            
            // Create ReplyNomination and GuestQrCode for Nominees (if provided)
            if (request.getNominations() != null && !request.getNominations().isEmpty()) {
                int nomineeIndex = 1; // Start index for nominee codes
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                String formattedDateTime = now.format(formatter);
                
                for (NominationData nominationData : request.getNominations()) {
                	 if (nominationData.getName() == null || nominationData.getEmailAddr() == null) {
                         logger.error("Invalid nomination data: name or email is null");
                         throw new IllegalArgumentException("Nomination data must include name and email");
                     }
   
                	 // Generate temporary nomineeCode: {guestCode}_nominee_{index}
                    String nomineeCode = String.format("nominee_%s_%d",  formattedDateTime, nomineeIndex++);

                    ReplyNomination nomination = ReplyNomination.builder()
                    		//.id(UUID.randomUUID())
                            .emailAddr(nominationData.getEmailAddr())
                            .name(nominationData.getName())
                            .nomineeCode(nomineeCode)
                            .lastUpdateId("system")
                            .lastUpdateTime(LocalDateTime.now())
                            .reply(reply)
                            .build();

                    replyNominationRepository.save(nomination);
                    	
                    // Create GuestQrCode for this nominee if nomineeQrCode is provided
                    if (nominationData.getNomineeQrCode() != null && !nominationData.getNomineeQrCode().isEmpty()) {
                        GuestQrCode nomineeQrCode = GuestQrCode.builder()
                               //.id(UUID.randomUUID())
                               .nomination(nomination)
                               .event(invitation.getEvent())
                               .qrCode(nominationData.getNomineeQrCode())
                               .activeInd("Y") // Active by default
                               .spouse(false) // Set spouse = true
                               .lastUpdateId("system")
                               .lastUpdateTime(LocalDateTime.now())
                               .build();

                        guestQrCodeRepository.save(nomineeQrCode);
                    }
                   
                }
            }

            // Create ReplyTransport (if provided)
            if (request.getTransport() != null) {
                ReplyTransport transport = ReplyTransport.builder()
                        .id(reply.getId()) // Assuming ID is shared with Reply
                        .awayOpt(request.getTransport().getAwayOpt())
                        .carRegistrationNo(request.getTransport().getCarRegistrationNo())
                        .fromOpt(request.getTransport().getFromOpt())
                        .ownArrangement(request.getTransport().getOwnArrangement())
                        .postAddress(request.getTransport().getPostAddress())
                        .remarks(request.getTransport().getRemarks())
                        .lastUpdateId("system")
                        .lastUpdateTime(LocalDateTime.now())
                        .reply(reply)
                        .build();

                replyTransportRepository.save(transport);
            }

            return ResponseEntity.ok("Reply, nominations, transport, and QR codes created successfully");

//        } catch (IllegalArgumentException e) {
//            logger.error("Validation error: {}", e.getMessage(), e);
//            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (DataAccessException e) {
            logger.error("Database error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Unexpected error: " + e.getMessage());
        }
    }
}