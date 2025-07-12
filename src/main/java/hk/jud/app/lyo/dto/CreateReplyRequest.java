package hk.jud.app.lyo.dto;

// DTO for creating Reply and ReplyData

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public
class CreateReplyRequest {
    @NotNull
    private String uid;
    @NotNull
    private JsonNode jsonData;
}
