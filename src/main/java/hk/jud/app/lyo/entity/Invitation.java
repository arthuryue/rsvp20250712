package hk.jud.app.lyo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import hk.jud.app.lyo.entity.enums.InvitationStatus;

@Entity
@Table(name = "invitation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "rsvp_link", unique = true)
	private String rsvpLink;

	@Enumerated(EnumType.STRING)
	@Column(name = "status",nullable = false, length = 45)
	private InvitationStatus status;

	@ManyToOne
	@JoinColumn(name = "guest_id", nullable = false)
	private Guest guest;

	@Column(name = "last_update_time", nullable = false)
	private LocalDateTime lastUpdateTime;

	@Column(name = "last_update_id", nullable = false)
	private String lastUpdateId;
	
	@ManyToOne
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@PrePersist
	protected void onCreate() {
		lastUpdateTime = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		lastUpdateTime = LocalDateTime.now();
	}
}