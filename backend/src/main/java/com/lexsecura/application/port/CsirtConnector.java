package com.lexsecura.application.port;

import com.lexsecura.domain.model.SrpSubmission;

/**
 * Port for connecting to a national CSIRT (Computer Security Incident Response Team).
 * CRA Art. 14(1): Manufacturers shall notify BOTH the ENISA SRP AND the relevant
 * CSIRT designated under Directive (EU) 2022/2555 (NIS2) in parallel.
 */
public interface CsirtConnector {

    /**
     * Submit a report to the national CSIRT.
     * @param submission the SRP submission
     * @param bundleZip the export bundle
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return an external reference string if submission succeeds
     */
    String submit(SrpSubmission submission, byte[] bundleZip, String countryCode);

    /**
     * Check if the CSIRT connector is available.
     */
    boolean isAvailable();
}
