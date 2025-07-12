package hk.jud.app.lyo.entity;

// Entity: guest

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;

import hk.jud.app.lyo.entity.enums.GuestType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

//Entity for guest table
@Entity
@Table(name = "guest")
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Guest {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "last_update_time", nullable = false)
	private LocalDateTime lastUpdateTime;

	@Column(name = "email_addr")
	private String emailAddr;
	
	@Column(name = "organization")
	private String organization;

	@Column(name = "title")
	private String title;
	
	@Column(name = "salutation")
	private String salutation;

	@Column(name = "last_update_id", nullable = false)
	private String lastUpdateId;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private GuestType type;

	@Column(name = "guest_code", nullable = false, unique = true)
	private String guestCode;

	@JsonIgnore
	@OneToMany(mappedBy = "guest")
    private List<Invitation> invitations;
	
	@PrePersist
	protected void onCreate() {
		lastUpdateTime = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		lastUpdateTime = LocalDateTime.now();
	}


}
