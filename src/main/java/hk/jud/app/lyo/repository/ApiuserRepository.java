package hk.jud.app.lyo.repository;

import hk.jud.app.lyo.entity.Apiuser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiuserRepository extends JpaRepository<Apiuser, Long> {
    Optional<Apiuser> findByUsername(String username);
}