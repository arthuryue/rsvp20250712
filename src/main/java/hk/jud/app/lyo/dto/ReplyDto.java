package hk.jud.app.lyo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ReplyDto {

	private Integer id;
	private LocalDateTime lastUpdateTime;
	private String attendInd;
	private String emailAddr;
	private String lastUpdateId;
	private String telNo;
	private String guestCode;
	private String spouseInd;
	private InvitationDto invitation;

}