-- 3. WAREHOUSE & ASSETS

-- Table: warehouses
CREATE TABLE warehouses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    owner_type VARCHAR(20) NOT NULL CHECK (owner_type IN ('USER', 'ORG')),
    name VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_warehouses_owner ON warehouses (owner_id, owner_type) WHERE deleted_at IS NULL;

-- Table: asset_collections
CREATE TABLE asset_collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    name VARCHAR(255) NOT NULL,
    parent_collection_id UUID REFERENCES asset_collections(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_asset_collections_warehouse_name ON asset_collections (warehouse_id, name);
CREATE INDEX idx_asset_collections_parent ON asset_collections (parent_collection_id);

-- Table: assets_block_templates
CREATE TABLE assets_block_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    collection_id UUID REFERENCES asset_collections(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_url TEXT,
    structure_data JSONB NOT NULL,
    version VARCHAR(20) DEFAULT '1.0.0',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_assets_block_templates_warehouse_created ON assets_block_templates (warehouse_id, created_at);
CREATE INDEX idx_assets_block_templates_collection ON assets_block_templates (collection_id);

-- Table: assets_invader_definitions
CREATE TABLE assets_invader_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    collection_id UUID REFERENCES asset_collections(id) ON DELETE SET NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    config_schema JSONB NOT NULL,
    compiler_hooks JSONB NOT NULL,
    version VARCHAR(20) DEFAULT '1.0.0',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_assets_invader_definitions_warehouse_created ON assets_invader_definitions (warehouse_id, created_at);
CREATE INDEX idx_assets_invader_definitions_collection ON assets_invader_definitions (collection_id);
