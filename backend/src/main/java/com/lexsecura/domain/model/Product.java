package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Product {

    private UUID id;
    private UUID orgId;
    private String name;
    private String type;
    private String criticality;
    private List<Map<String, String>> contacts;
    private Instant createdAt;
    private Instant updatedAt;

    public Product() {
    }

    public Product(UUID orgId, String name, String type, String criticality,
                   List<Map<String, String>> contacts) {
        this.orgId = orgId;
        this.name = name;
        this.type = type != null ? type : "SOFTWARE";
        this.criticality = criticality != null ? criticality : "STANDARD";
        this.contacts = contacts != null ? contacts : List.of();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCriticality() { return criticality; }
    public void setCriticality(String criticality) { this.criticality = criticality; }

    public List<Map<String, String>> getContacts() { return contacts; }
    public void setContacts(List<Map<String, String>> contacts) { this.contacts = contacts; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
