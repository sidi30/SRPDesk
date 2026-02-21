package com.lexsecura.application.port;

import com.lexsecura.domain.model.SrpSubmission;

/**
 * Port for connecting to the ENISA SRP (Single Reporting Platform).
 * V1: stub implementation. Future: real HTTP connector.
 */
public interface SrpConnector {

    /**
     * Submit a report to the SRP.
     * @return an external reference string if submission succeeds
     */
    String submit(SrpSubmission submission, byte[] bundleZip);

    /**
     * Check if the connector is available (feature flag / config).
     */
    boolean isAvailable();
}
