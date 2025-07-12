package hk.jud.app.lyo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ReplyNominationDto {

	private UUID id;
	private LocalDateTime lastUpdateTime;
	private String emailAddr;
	private String lastUpdateId;
	private String name;
	private String nomineeCode;

}