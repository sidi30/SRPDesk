package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "components")
public class ComponentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 1000)
    private String purl;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 255)
    private String version;

    @Column(nullable = false, length = 100)
    private String type;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPurl() { return purl; }
    public void setPurl(String purl) { this.purl = purl; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
