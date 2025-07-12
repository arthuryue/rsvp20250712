package hk.jud.app.lyo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import hk.jud.app.lyo.entity.enums.GuestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestInvitationDto {

    private UUID id;
    private String guestCode;
    private String name;
    private String emailAddr;

    private GuestType type;
    private String typeLabel;
    private boolean invited;
}
