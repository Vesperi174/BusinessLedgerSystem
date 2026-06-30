package com.businessledger.repository;

import com.businessledger.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByOrderIdOrderByCreateTimeDesc(Long orderId);

    @Modifying
    @Query(value = "DELETE FROM audit_log WHERE id NOT IN (SELECT id FROM audit_log ORDER BY create_time DESC LIMIT :keepCount)", nativeQuery = true)
    int deleteOldLogs(@Param("keepCount") int keepCount);
}