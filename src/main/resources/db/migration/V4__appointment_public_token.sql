ALTER TABLE appointments
  ADD COLUMN public_token VARCHAR(64);

UPDATE appointments
SET public_token = md5(id::text || clock_timestamp()::text || random()::text)
WHERE public_token IS NULL;

ALTER TABLE appointments
  ALTER COLUMN public_token SET NOT NULL;

ALTER TABLE appointments
  ADD CONSTRAINT uk_appointments_public_token UNIQUE(public_token);
