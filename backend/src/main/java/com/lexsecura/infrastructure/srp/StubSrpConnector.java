package com.lexsecura.infrastructure.srp;

import com.lexsecura.application.port.SrpConnector;
import com.lexsecura.domain.model.SrpSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of SRP connector.
 * Logs the submission but does not call any external service.
 * To be replaced with real ENISA SRP connector when their API is available.
 */
@Component
public class StubSrpConnector implements SrpConnector {

    private static final Logger log = LoggerFactory.getLogger(StubSrpConnector.class);

    @Override
    public String submit(SrpSubmission submission, byte[] bundleZip) {
        log.info("[STUB SRP] Would submit {} for event {} ({} bytes)",
                submission.getSubmissionType(), submission.getCraEventId(), bundleZip.length);
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
