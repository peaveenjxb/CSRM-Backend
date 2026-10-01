package com.csrm.repository;

import com.csrm.model.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("select a from AuditLog a where a.timestamp >= :from and a.timestamp < :to order by a.timestamp desc")
    List<AuditLog> findInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select a from AuditLog a where a.userId = :uid and a.timestamp >= :from and a.timestamp < :to order by a.timestamp desc")
    List<AuditLog> findByUserInRange(@Param("uid") Long uid, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByActionStartingWith(String prefix);
}
