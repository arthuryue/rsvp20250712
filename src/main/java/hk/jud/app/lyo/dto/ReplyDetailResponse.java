package hk.jud.app.lyo.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class ReplyDetailResponse {

    private  ReplyDto reply;
    private  List<ReplyNominationDto> nominations;
    private  List<ReplyTransportDto> transports;
    private  String error;

}