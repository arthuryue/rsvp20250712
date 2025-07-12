package hk.jud.app.lyo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reply_nomination")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyNomination {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "last_update_time", nullable = false)
    private LocalDateTime lastUpdateTime;

    @Column(name = "email_addr", nullable = false)
    private String emailAddr;

    @Column(name = "last_update_id", nullable = false)
    private String lastUpdateId;

    @Column(nullable = false)
    private String name;

    @Column(name = "nominee_code", nullable = false, unique = true)
    private String nomineeCode;

    @ManyToOne
    @JoinColumn(name = "reply_id", nullable = false)
    private Reply reply;
}