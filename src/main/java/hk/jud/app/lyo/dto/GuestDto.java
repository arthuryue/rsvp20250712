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
public class GuestDto {

    private UUID id;
    private LocalDateTime lastUpdateTime;
    private String emailAddr;
    private String name;
    private GuestType type;
    private String guestCode;
    private String title;
    private String organization;
}
