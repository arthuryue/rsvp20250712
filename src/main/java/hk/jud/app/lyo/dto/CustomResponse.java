package hk.jud.app.lyo.dto;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomResponse<T> {
    private String status;
    private String message;
    private T data;
    
    public static <T> CustomResponse<T> success(String message, T data) {
        return new CustomResponse<>("success", message, data);
    }

    public static <T> CustomResponse<T> error(String message) {
        return new CustomResponse<>("error", message, null);
    }
    
}