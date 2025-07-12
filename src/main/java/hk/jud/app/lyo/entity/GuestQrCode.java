package hk.jud.app.lyo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "guest_qr_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestQrCode {

    @Id
	@GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @ManyToOne
    @JoinColumn(name = "nomination_id")
    private ReplyNomination nomination;

    @Column(name = "last_update_time", nullable = false)
    private LocalDateTime lastUpdateTime;

    @Column(name = "active_ind", nullable = false)
    private String activeInd;

    @Column(name = "last_update_id", nullable = false)
    private String lastUpdateId;

    @Column(name = "qr_code", nullable = false)
    private String qrCode;

    @Column(name = "qr_code2")
    private String qrCode2;
    
    @Column(name = "spouse")
    private Boolean spouse;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}