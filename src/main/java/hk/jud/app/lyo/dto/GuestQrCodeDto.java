package hk.jud.app.lyo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestQrCodeDto {

	 private String qrCode;
	 private String qrCode2;
	 private String activeInd;
}