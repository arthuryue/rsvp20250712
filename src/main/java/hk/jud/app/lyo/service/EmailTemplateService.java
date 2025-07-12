package hk.jud.app.lyo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.google.zxing.WriterException;

import hk.jud.app.lyo.config.EmailAttachmentLoader;
import hk.jud.app.lyo.config.GuestTypeLoader;
import hk.jud.app.lyo.entity.Guest;
import hk.jud.app.lyo.entity.Invitation;
import hk.jud.app.lyo.entity.ReplyNomination;
import hk.jud.app.lyo.entity.enums.GuestType;
import hk.jud.app.lyo.util.ChineseDate;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

	private final EmailAttachmentLoader emailAttachmentLoader;
	private final GuestTypeLoader gusetTypeLoader;
	private final ResourceLoader resourceLoader;
	private final GuestQrCodeService guestQrCodeService;


	@Value("${email.template.base-path}")
	private String templateBasePath;



	public List<String> getAvailableAttachments(GuestType guestType, String emailType, String language) {

		String lookupType = guestType.toString().toLowerCase();
		String lookupEmailType = emailType.toLowerCase();
		String lookupLanguage = language.toLowerCase();
		System.out.println("Looking up attachments for type: " + lookupType + ", emailType: " + lookupEmailType
				+ ", language: " + lookupLanguage);

		System.out.println("All loaded attachments config: ");
		// emailAttachmentLoader.printAll();

		return emailAttachmentLoader.getAttachments(lookupType, lookupEmailType, lookupLanguage);

	}

	// public EmailTemplate getTemplateContent(String type, String emailType, String
	// language, String guestName,
	// String rsvpLink, boolean includeNote, String qrCode, List<String>
	// selectedAttachments)

	public EmailTemplate getTemplateContent(Guest guest, String emailType, String language, String rsvpLink,
			String qrCode,String spouseQrCode, ReplyNomination rn) throws WriterException

	{
		String guestType = guest.getType().toString();
		String fileName = String.format("%s%s_%s_%s.html", templateBasePath, guestType.toLowerCase(),
				emailType.toLowerCase(), language.toLowerCase());
		String subjectFileName = String.format("%s%s_%s_%s.subject", templateBasePath + "subject/",
				guestType.toLowerCase(), emailType.toLowerCase(), language.toLowerCase());
		String recipientName = rn != null ? rn.getName() : guest.getName();
		String recipientTitle = rn != null ? null : guest.getTitle();

		try {
			// Load template content

			Resource resource = resourceLoader.getResource(fileName);
			if (!resource.exists()) {
				throw new IllegalArgumentException("Template not found for guest type: " + guestType + ", emailType: "
						+ emailType + ", language: " + language);
			}
			String content = new String(Files.readAllBytes(Paths.get(resource.getURI())));
			
			// Handle Date 
			if (language.equals("tc") )
			{
				content = content.replace("[EMAIL_DATE]", ChineseDate.toChineseDate(new Date()));
			}
			else if (language.equals("bi") )
			{
				content = content.replace("[EMAIL_DATE_TC]", ChineseDate.toChineseDate(new Date()));
				content = content.replace("[EMAIL_DATE]", new SimpleDateFormat("dd MMMM yyyy").format(new Date()));
			}
			else 
			content = content.replace("[EMAIL_DATE]", new SimpleDateFormat("dd MMMM yyyy").format(new Date()));
	
			
			content = content.replace("[GUEST_NAME]", recipientName);

			// Handle RSVP link
			if (emailType.equalsIgnoreCase("INVITATION") && rsvpLink != null && !rsvpLink.isEmpty()) {
					content = content.replace("[RSVP_LINK]", rsvpLink );
		
			} else {
				content = content.replace("[RSVP_LINK]", "");
			}

			// Handle Title
			content = content.replace("[GUEST_TITLE]", recipientTitle == null ? "" : recipientTitle);

			// Load subject
			Resource subjectResource = resourceLoader.getResource(subjectFileName);
			String subject = subjectResource.exists()
					? new String(Files.readAllBytes(Paths.get(subjectResource.getURI()))).trim()
					: (emailType.equalsIgnoreCase("INVITATION") ? "Invitation" : "Confirmation");

			// Load images
			Map<String, String> images = new HashMap<>();
			String[] imageCids = { "event_logo", "header_image", "footer_image", "jud_logo" }; // Define expected CID
																								// placeholders
			for (String cid : imageCids) {
				System.out.println(cid);
				if (content.contains("cid:" + cid)) {
					String imagePath = String.format("classpath:static/images/%s.jpg", cid);
					Resource imageResource = resourceLoader.getResource(imagePath);
					if (imageResource.exists()) {
						try {
							byte[] imageBytes = imageResource.getInputStream().readAllBytes();
							String base64Image = Base64.getEncoder().encodeToString(imageBytes);
							String dataUrl = "data:image/jpeg;base64," + base64Image;
							images.put(cid, dataUrl);
							content = content.replace("cid:" + cid, dataUrl);
						} catch (IOException e) {
							throw new IllegalArgumentException(
									"Error reading image for CID " + cid + ": " + e.getMessage());
						}
					}
				}
			}

	         // Handle QR code
            if (qrCode != null) {
                System.out.println("qrCode: " + qrCode);
                String qrCodeBase64 = guestQrCodeService.generateQRCodeBase64(qrCode);
                content = content.replaceAll("<div class=\"qrcode_container\" ><b>QR Code</b></div>",
                        "<p>QR Code</p><div class=\"qrcode_container\"><img src=\"data:image/png;base64," + qrCodeBase64
                                + "\" alt=\"QR Code\" /></div>");
            }

         // Handle spouse QR code

            if (spouseQrCode != null) {
                String spouseQrCodeBase64 = guestQrCodeService.generateQRCodeBase64(spouseQrCode);
                String spouseQrCodeHtml = "<p>Spouse QR Code:</p><div class=\"qrcode_container\" style=\"margin-top: 10px;\"><img src=\"data:image/png;base64," + spouseQrCodeBase64
                        + "\" alt=\"Spouse QR Code\" /></div>";
                int qrCodeContainerIndex = content.indexOf("<div class=\"qrcode_container\"");
                if (qrCodeContainerIndex != -1) {
                    int closingDivIndex = content.indexOf("</div>", qrCodeContainerIndex) + "</div>".length();
                    content = content.substring(0, closingDivIndex) + spouseQrCodeHtml + content.substring(closingDivIndex);
                } 
            }
            
            

			// Embed QR code if provided
			if (qrCode != null && content.contains("cid:qr-code")) {
				images.put("qr-code", qrCode);
				content = content.replace("cid:qr-code", qrCode);
			}

			return new EmailTemplate(subject, content, images, null);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading template: " + e.getMessage());
		}
	}
	
	@Data
	@RequiredArgsConstructor
	public static class EmailTemplate {
		private final String subject;
		private final String content;
		private final Map<String, String> images;
		private final List<String> attachments;
	}

}