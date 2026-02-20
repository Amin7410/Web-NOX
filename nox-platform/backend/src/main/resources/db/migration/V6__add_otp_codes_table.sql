CREATE TABLE otp_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    code VARCHAR(6) NOT NULL,
    type VARCHAR(20) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_otp_codes_user FOREIGN KEY (user_id) REFERENCES users(id) -- Soft delete handled at app layer
);

CREATE INDEX idx_otp_codes_code_type ON otp_codes(code, type);
CREATE INDEX idx_otp_codes_user_id ON otp_codes(user_id);
