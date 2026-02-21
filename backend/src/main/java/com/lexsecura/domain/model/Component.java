package com.lexsecura.domain.model;

import java.util.UUID;

public class Component {

    private UUID id;
    private String purl;
    private String name;
    private String version;
    private String type;

    public Component() {}

    public Component(String purl, String name, String version, String type) {
        this.purl = purl;
        this.name = name;
        this.version = version;
        this.type = type;
    }

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
