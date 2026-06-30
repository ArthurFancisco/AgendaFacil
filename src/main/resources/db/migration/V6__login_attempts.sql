CREATE TABLE login_attempts (
  id BIGSERIAL PRIMARY KEY,
  username_normalized VARCHAR(160),
  ip_address VARCHAR(80) NOT NULL,
  success BOOLEAN NOT NULL DEFAULT FALSE,
  attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  blocked_until TIMESTAMP,
  user_agent VARCHAR(255)
);

CREATE INDEX idx_login_attempt_user_time ON login_attempts(username_normalized, attempted_at);
CREATE INDEX idx_login_attempt_ip_time ON login_attempts(ip_address, attempted_at);
CREATE INDEX idx_login_attempt_user_block ON login_attempts(username_normalized, blocked_until);
CREATE INDEX idx_login_attempt_ip_block ON login_attempts(ip_address, blocked_until);
