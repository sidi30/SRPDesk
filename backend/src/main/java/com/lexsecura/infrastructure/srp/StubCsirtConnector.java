package com.lexsecura.infrastructure.srp;

import com.lexsecura.application.port.CsirtConnector;
import com.lexsecura.domain.model.SrpSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of CSIRT connector.
 * Logs the notification but does not call any external service.
 * Active when app.csirt.enabled is not true.
 */
@Component
public class StubCsirtConnector implements CsirtConnector {

    private static final Logger log = LoggerFactory.getLogger(StubCsirtConnector.class);

    @Override
    public String submit(SrpSubmission submission, byte[] bundleZip, String countryCode) {
        log.info("[STUB CSIRT] Would notify CSIRT ({}) for {} event {} ({} bytes)",
                countryCode, submission.getSubmissionType(), submission.getCraEventId(), bundleZip.length);
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
