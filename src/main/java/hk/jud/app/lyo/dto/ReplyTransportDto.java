package hk.jud.app.lyo.dto;

import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ReplyTransportDto {

    private  Integer id;
    private  LocalDateTime lastUpdateTime;
    private  String awayOpt;
    private  String carRegistrationNo;
    private  String fromOpt;
    private  String lastUpdateId;
    private  String ownArrangement;
    private  String remarks;

}