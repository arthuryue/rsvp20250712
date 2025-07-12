package hk.jud.app.lyo.service;

import hk.jud.app.lyo.entity.EmailLog;
import hk.jud.app.lyo.entity.Invitation;
import hk.jud.app.lyo.entity.enums.GuestType;
import hk.jud.app.lyo.entity.enums.InvitationStatus;
import hk.jud.app.lyo.repository.EmailLogRepository;
import hk.jud.app.lyo.repository.InvitationRepository;
import hk.jud.app.lyo.service.EmailTemplateService.EmailTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.eclipse.angus.mail.util.MailConnectException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailLogService {

	private final InvitationRepository invitationRepository;
    private final EmailLogRepository emailLogRepository;
    
    private final EmailTemplateService emailTemplateService;
    private final SecurityService securityService;
    private final JavaMailSender mailSender;
	private final ResourceLoader resourceLoader;

	@Value("${spring.mail.from}")
    private String fromEmail;
	
	@Value("${spring.mail.from.name}")
    private String fromName;
	
    public List<EmailLog> findByInvitationId(Integer invitationId) {
        return emailLogRepository.findByInvitationId(invitationId, Sort.by(Sort.Direction.DESC, "lastUpdateTime"));
    }

    @Transactional
    public void createEmailLog(Invitation invitation, String emailType, String emailAddr, String language, List<String> selectedAttachments, EmailTemplate template) {
        if (emailType == null || emailType.trim().isEmpty() || emailType.length() > 255) {
            throw new IllegalArgumentException("Email type must be non-empty and up to 255 characters");
        }
        if (!Arrays.asList("INVITATION", "REMINDER", "CONFIRMATION").contains(emailType)) {
            throw new IllegalArgumentException("Invalid email type");
        }
        if (emailAddr == null || emailAddr.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address is missing");
        }

    
        try {
            // Create MIME message for SMTP
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set email properties
            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(emailAddr);
            helper.setSubject(template.getSubject());
            helper.setText(template.getContent(), true); // HTML content



            // Attach selected files
            if (selectedAttachments != null) {
                for (String attachment : selectedAttachments) {
                    Resource resource = resourceLoader.getResource("classpath:/static/attachments/" + attachment);
                    if (resource.exists()) {
                        helper.addAttachment(attachment, resource);
                    }
                }
            }

            // Send email
            mailSender.send(message);
           
            // Log email after successful send
            EmailLog emailLog = EmailLog.builder()
                    .invitation(invitation)
                    .emailType(emailType)
                    .emailAddr(emailAddr)
                    .lang(language)
                    .sentDate(LocalDateTime.now())
                    .lastUpdateId(securityService.getCurrentUsername())
                    .lastUpdateTime(LocalDateTime.now())
                    .build();
            emailLogRepository.save(emailLog);
            
            
            GuestType guestType = invitation.getGuest().getType();
            Boolean isRvsp = hk.jud.app.lyo.service.InvitationService.isRsvp(guestType);
            
            if (emailType.equals("INVITATION") && invitation.getStatus() == InvitationStatus.NEW  && isRvsp)
            	invitation.setStatus(InvitationStatus.INVITED); 
            else if (emailType.equals("INVITATION") &&  !isRvsp)
            		invitation.setStatus(InvitationStatus.CONFIRMED); 
            if (emailType.equals("CONFIRMATION"))
            	invitation.setStatus(InvitationStatus.CONFIRMED); 
            
            invitationRepository.save(invitation);
         

        } catch (MailConnectException e) {
        	System.err.println("Email sending failed 1: " + e.getMessage());
            throw new IllegalArgumentException("Failed to connect to email server. Please check your network or email server settings.");
        } catch (MessagingException e) {
        	System.err.println("Email sending failed 2: " + e.getMessage());
            throw new IllegalArgumentException("Error sending email: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
        	System.err.println("Email sending failed 3: " + e.getMessage());
            throw new IllegalArgumentException("Error encoding email address: " + e.getMessage());
        } catch (Exception e) {
        	 System.err.println("Email sending failed 4: " + e.getMessage());
            throw new IllegalArgumentException("Unexpected error while sending email: " + e.getMessage());
        }
    }
    
    @Transactional
    public void createEmailLog(Invitation invitation, String emailType, String emailAddr) {
        if (emailType == null || emailType.trim().isEmpty() || emailType.length() > 255) {
            throw new IllegalArgumentException("Email type must be non-empty and up to 255 characters");
        }
        if (!Arrays.asList("INVITATION", "REMINDER", "CONFIRMATION").contains(emailType)) {
            throw new IllegalArgumentException("Invalid email type");
        }
        if (emailAddr == null || emailAddr.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address is missing");
        }
        


        EmailLog emailLog = EmailLog.builder()
                .invitation(invitation)
                .emailType(emailType)
                .emailAddr(emailAddr)
                .sentDate(LocalDateTime.now())
                .lastUpdateId(securityService.getCurrentUsername()) // Default // simplicity
                .lastUpdateTime(LocalDateTime.now())
                .build();
        emailLogRepository.save(emailLog);
    }

}