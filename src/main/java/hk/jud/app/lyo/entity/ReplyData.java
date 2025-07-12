package hk.jud.app.lyo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reply_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "last_update_time", nullable = false)
    private LocalDateTime lastUpdateTime;

    @Column(name = "last_update_id", nullable = false)
    private String lastUpdateId;

    @Column(columnDefinition = "JSON", nullable = false)
    private String json;

    @OneToOne
    @JoinColumn(name = "reply_id", nullable = false)
    private Reply reply;
    
    
}