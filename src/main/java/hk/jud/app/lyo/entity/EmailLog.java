package hk.jud.app.lyo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "last_update_time", nullable = false)
    private LocalDateTime lastUpdateTime;

    @Column(name = "sent_date", nullable = false)
    private LocalDateTime sentDate;

    @Column(name = "email_addr", nullable = false)
    private String emailAddr;

    @Column(name = "email_type", nullable = false)
    private String emailType;

    @Column(name = "last_update_id", nullable = false)
    private String lastUpdateId;
    
    @Column(name = "lang")
    private String lang;
 
    @ManyToOne
    @JoinColumn(name = "invitation_id", nullable = false)
    private Invitation invitation;
    
	@PrePersist
	protected void onCreate() {
		lastUpdateTime = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		lastUpdateTime = LocalDateTime.now();
	}
}