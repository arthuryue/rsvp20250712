package hk.jud.app.lyo.repository;


// Repository for ReplyData

import org.springframework.data.jpa.repository.JpaRepository;

import hk.jud.app.lyo.entity.ReplyData;

public interface ReplyDataRepository extends JpaRepository<ReplyData, Long> {
}