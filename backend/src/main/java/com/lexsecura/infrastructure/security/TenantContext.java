package com.lexsecura.infrastructure.security;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_ORG = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_USER = new ThreadLocal<>();

    private TenantContext() {
    }

    public static UUID getOrgId() {
        UUID orgId = CURRENT_ORG.get();
        if (orgId == null) {
            throw new IllegalStateException("No org_id in tenant context");
        }
        return orgId;
    }

    public static UUID getUserId() {
        UUID userId = CURRENT_USER.get();
        if (userId == null) {
            throw new IllegalStateException("No user_id in tenant context");
        }
        return userId;
    }

    public static void setOrgId(UUID orgId) {
        CURRENT_ORG.set(orgId);
    }

    public static void setUserId(UUID userId) {
        CURRENT_USER.set(userId);
    }

    public static void clear() {
        CURRENT_ORG.remove();
        CURRENT_USER.remove();
    }
}
