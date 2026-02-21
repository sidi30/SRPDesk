package com.lexsecura.application.dto;

import java.time.Instant;

public record SlaResponse(
        SlaDeadline earlyWarning,
        SlaDeadline notification,
        SlaDeadline finalReport
) {
    public record SlaDeadline(
            Instant dueAt,
            long remainingSeconds,
            boolean overdue
    ) {}
}
