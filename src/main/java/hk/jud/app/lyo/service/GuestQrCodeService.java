package hk.jud.app.lyo.service;

import hk.jud.app.lyo.entity.GuestQrCode;
import hk.jud.app.lyo.repository.GuestQrCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.imageio.ImageIO;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

@Service
public class GuestQrCodeService {

    @Autowired
    private GuestQrCodeRepository repository;

    @Value("${email.qrcode.width:200}")
    private int qrCodeWidth;

    @Value("${email.qrcode.height:200}")
    private int qrCodeHeight;

    public Page<GuestQrCode> findAll(String searchQuery, String activeInd, Pageable pageable) {
        return repository.findAll((Specification<GuestQrCode>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchQuery != null && !searchQuery.isEmpty()) {
                predicates.add(cb.or(
                    cb.like(root.get("qrCode"), "%" + searchQuery + "%"),
                    cb.like(root.join("guest", JoinType.LEFT).get("name"), "%" + searchQuery + "%"),
                    cb.like(root.join("nomination", JoinType.LEFT).get("name"), "%" + searchQuery + "%")
                ));
            }
            if (activeInd != null && !activeInd.isEmpty()) {
                predicates.add(cb.equal(root.get("activeInd"), activeInd));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }


    public Optional<GuestQrCode> findById(UUID id) {
        return repository.findById(id);
    }

    public GuestQrCode save(GuestQrCode qrCode) {
        return repository.save(qrCode);
    }
    
  
    public String generateQRCodeBase64(String qrCodeData) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();	
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1); // Configurable margin, e.g., 1 for a thin border
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, qrCodeWidth, qrCodeHeight,hints);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "JPEG", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
        
     
    }
    
    public byte[] generateQRCodeBytes(String qrCodeData) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1); // Configurable margin, e.g., 1 for a thin border
        BitMatrix bitMatrix = qrCodeWriter.encode(
                qrCodeData, 
                BarcodeFormat.QR_CODE, 
                qrCodeWidth, 
                qrCodeHeight, 
                hints
            );

            // 🎨 Customize colors here
            // Format: 0xAARRGGBB (Alpha, Red, Green, Blue)
        int brownColor = new Color(165, 42, 42).getRGB();
            int foregroundColor = 0xFF006400; // or use 0xFF000000 for black
            int backgroundColor = Color.WHITE.getRGB(); // or use 0xFFFFFFFF for white

            MatrixToImageConfig config = new MatrixToImageConfig(foregroundColor, backgroundColor);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "JPEG", baos, config);
            return baos.toByteArray();
    }


    public boolean existsByQrCode(String qrCode) {
        if (qrCode == null || qrCode.trim().isEmpty()) {
            return false;
        }
        return repository.existsByQrCode(qrCode);
    }
}