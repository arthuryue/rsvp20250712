package hk.jud.app.lyo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(nullable = false, length = 1)
    private String status;

    @Column(name = "last_update_id", nullable = false)
    private String lastUpdateId;

    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invitation> invitations = new ArrayList<>();
    
	@PrePersist
	protected void onCreate() {
		lastUpdateTime = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		lastUpdateTime = LocalDateTime.now();
	}
}