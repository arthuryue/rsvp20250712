package hk.jud.app.lyo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class EventDto {

    private final Integer id;
    private final String eventName;
    private final String status;
    private final String lastUpdateId;
    private final LocalDateTime lastUpdateTime;

}