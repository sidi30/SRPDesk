package com.lexsecura.infrastructure.srp;

import com.lexsecura.domain.model.SrpSubmission;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StubCsirtConnectorTest {

    private final StubCsirtConnector connector = new StubCsirtConnector();

    @Test
    void submit_shouldLogAndReturnNull() {
        SrpSubmission sub = new SrpSubmission();
        sub.setSubmissionType("EARLY_WARNING");
        sub.setCraEventId(UUID.randomUUID());

        String result = connector.submit(sub, new byte[100], "FR");

        assertNull(result);
    }

    @Test
    void isAvailable_shouldReturnFalse() {
        assertFalse(connector.isAvailable());
    }
}
