package hk.jud.app.lyo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestCreateDto {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String emailAddr;

    @NotBlank(message = "Type is required")
    private String type;
    

    private String title;
    
    private String organization;
    
    private String salutation;
    
    @NotBlank(message = "Guest code is required")
    private String guestCode;
}