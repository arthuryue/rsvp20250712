package hk.jud.app.lyo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "apiuser")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apiuser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false, unique = true)
    private String username;
}