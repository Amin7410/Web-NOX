-- ==========================================
-- SECTION 2: MULTI-TENANCY (Invitations)
-- ==========================================

CREATE TABLE invitations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    org_id UUID NOT NULL,
    role_id UUID NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    invited_by_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    accepted_at TIMESTAMPTZ,
    resent_count INTEGER DEFAULT 0,
    last_sent_at TIMESTAMPTZ,

    CONSTRAINT fk_invitations_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT fk_invitations_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT, -- Keep invitation even if role changes? Or cascade? Design says: 516 Ref: roles.id < invitations.role_id. Usually RESTRICT.
    CONSTRAINT fk_invitations_inviter FOREIGN KEY (invited_by_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_invitations_token ON invitations(token);

-- Unique index to prevent duplicate pending invitations for the same email in the same org
CREATE UNIQUE INDEX uq_org_email_pending ON invitations(org_id, email) WHERE status = 'PENDING';
