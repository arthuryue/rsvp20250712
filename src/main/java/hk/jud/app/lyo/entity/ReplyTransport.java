package hk.jud.app.lyo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reply_transport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyTransport {

    @Id
    private Integer id;

    @Column(name = "last_update_time", nullable = false)
    private LocalDateTime lastUpdateTime;

    @Column(name = "away_opt")
    private String awayOpt;

    @Column(name = "car_registration_no")
    private String carRegistrationNo;

    @Column(name = "from_opt")
    private String fromOpt;

    @Column(name = "last_update_id", nullable = false)
    private String lastUpdateId;

    @Column(name = "own_arrangement")
    private String ownArrangement;
    
    @Column(name = "post_address")
    private String postAddress;
    
    @Column(name = "remarks")
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "reply_id", nullable = false)
    private Reply reply;
}