-- V017: Add org_id to releases for direct multi-tenancy filtering
ALTER TABLE releases ADD COLUMN org_id UUID;
UPDATE releases SET org_id = p.org_id FROM products p WHERE releases.product_id = p.id;
ALTER TABLE releases ALTER COLUMN org_id SET NOT NULL;
CREATE INDEX idx_releases_org_id ON releases(org_id);
