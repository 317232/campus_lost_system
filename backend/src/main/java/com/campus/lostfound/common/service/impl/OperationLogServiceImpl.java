package com.campus.lostfound.common.service.impl;

import com.campus.lostfound.common.service.OperationLogService;
import com.campus.lostfound.domain.entity.OperationLog;
import com.campus.lostfound.mapper.OperationLogMapper;
import com.campus.lostfound.security.SecurityUserUtils;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final SecurityUserUtils securityUserUtils;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper,
                                    SecurityUserUtils securityUserUtils) {
        this.operationLogMapper = operationLogMapper;
        this.securityUserUtils = securityUserUtils;
    }

    @Override
    public void log(String targetType, Long targetId, String action, String fromStatus, String toStatus, String remark) {
        OperationLog logEntry = new OperationLog();
        logEntry.setTargetType(targetType);
        logEntry.setTargetId(targetId);
        logEntry.setAction(action);
        logEntry.setFromStatus(fromStatus);
        logEntry.setToStatus(toStatus);
        logEntry.setRemark(remark);

        Long operatorId = securityUserUtils.getCurrentUserId();
        if (operatorId != null) {
            logEntry.setOperatorId(operatorId);
        }

        operationLogMapper.insert(logEntry);
    }
}