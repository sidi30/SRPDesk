-- V012: SBOM components

CREATE TABLE components (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purl        VARCHAR(1000) NOT NULL,
    name        VARCHAR(500) NOT NULL,
    version     VARCHAR(255),
    type        VARCHAR(100) NOT NULL,
    UNIQUE(purl)
);

CREATE TABLE release_components (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    release_id      UUID NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    component_id    UUID NOT NULL REFERENCES components(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(release_id, component_id)
);

CREATE INDEX idx_release_components_release ON release_components(release_id);
CREATE INDEX idx_release_components_component ON release_components(component_id);
