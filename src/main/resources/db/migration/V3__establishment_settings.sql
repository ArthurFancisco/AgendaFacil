CREATE TABLE establishment_settings (
  id BIGSERIAL PRIMARY KEY,
  establishment_id BIGINT NOT NULL UNIQUE REFERENCES establishments(id),
  new_client_requires_approval BOOLEAN NOT NULL DEFAULT TRUE,
  pending_expiration_minutes INTEGER NOT NULL DEFAULT 60,
  max_future_appointments_per_phone INTEGER NOT NULL DEFAULT 3,
  max_attempts_per_phone_hour INTEGER NOT NULL DEFAULT 3,
  max_attempts_per_ip_hour INTEGER NOT NULL DEFAULT 10,
  no_show_count_for_manual_approval INTEGER NOT NULL DEFAULT 2,
  no_show_count_for_block INTEGER NOT NULL DEFAULT 3,
  min_hours_before_client_cancel INTEGER NOT NULL DEFAULT 24,
  long_service_manual_approval_minutes INTEGER NOT NULL DEFAULT 120,
  show_prices_on_public_page BOOLEAN NOT NULL DEFAULT TRUE,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO establishment_settings(establishment_id)
SELECT id FROM establishments
ON CONFLICT DO NOTHING;
