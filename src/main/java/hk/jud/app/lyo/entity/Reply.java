package hk.jud.app.lyo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reply")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "last_update_time", nullable = false)
	private LocalDateTime lastUpdateTime;

	@Column(name = "attend_ind", nullable = false) 
	private String attendInd;

	@Column(name = "email_addr")
	private String emailAddr;

	@Column(name = "last_update_id", nullable = false)
	private String lastUpdateId;

	@Column(name = "tel_no")
	private String telNo;
	
	@Column(name = "spouse_ind")
	private String spouseInd;

	@Column(name = "guest_code", nullable = false)
	private String guestCode;

	@ManyToOne
	@JoinColumn(name = "invitation_id", nullable = false)
	private Invitation invitation;

	@OneToOne(mappedBy = "reply", cascade = CascadeType.ALL, orphanRemoval = true)
	private ReplyData replyData;
}