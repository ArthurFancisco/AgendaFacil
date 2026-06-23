ALTER TABLE appointments
  ADD COLUMN internal_note VARCHAR(500);

CREATE TABLE appointment_audits (
  id BIGSERIAL PRIMARY KEY,
  establishment_id BIGINT NOT NULL REFERENCES establishments(id),
  appointment_id BIGINT NOT NULL REFERENCES appointments(id),
  user_id BIGINT REFERENCES users_app(id),
  action VARCHAR(40) NOT NULL,
  details VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_est_created ON appointment_audits(establishment_id, created_at);
CREATE INDEX idx_audit_appointment ON appointment_audits(appointment_id);
