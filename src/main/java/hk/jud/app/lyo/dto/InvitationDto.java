package hk.jud.app.lyo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class InvitationDto {

	private Integer id;
	private String rsvpLink;
	private String status;
	private GuestDto guest;
	// private EventDto event;
	private LocalDateTime lastUpdateTime;
	private String lastUpdateId;

}