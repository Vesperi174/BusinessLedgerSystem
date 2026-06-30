package com.businessledger.service;

import com.businessledger.entity.AuditLog;
import com.businessledger.repository.AuditLogRepository;
import com.businessledger.util.Constants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(Long orderId, String action, String detail) {
        AuditLog log = new AuditLog(orderId, action, detail);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByOrderId(Long orderId) {
        return auditLogRepository.findByOrderIdOrderByCreateTimeDesc(orderId);
    }

    @Transactional
    public void cleanOldLogs(int keepCount) {
        auditLogRepository.deleteOldLogs(keepCount);
    }

    @Transactional
    public void cleanOldLogs() {
        cleanOldLogs(Constants.DEFAULT_LOG_KEEP_COUNT);
    }

    public long count() {
        return auditLogRepository.count();
    }
}