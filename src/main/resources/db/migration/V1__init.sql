CREATE TABLE establishments (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  slug VARCHAR(80) NOT NULL UNIQUE,
  whatsapp VARCHAR(20) NOT NULL,
  city VARCHAR(80),
  description TEXT,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users_app (
  id BIGSERIAL PRIMARY KEY,
  establishment_id BIGINT NOT NULL REFERENCES establishments(id),
  name VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(120) NOT NULL,
  role VARCHAR(30) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE customers (
  id BIGSERIAL PRIMARY KEY,
  establishment_id BIGINT NOT NULL REFERENCES establishments(id),
  name VARCHAR(120) NOT NULL,
  phone_normalized VARCHAR(20) NOT NULL,
  no_show_count INTEGER NOT NULL DEFAULT 0,
  blocked BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_customer_est_phone UNIQUE(establishment_id, phone_normalized)
);

CREATE TABLE service_items (
  id BIGSERIAL PRIMARY KEY,
  establishment_id BIGINT NOT NULL REFERENCES establishments(id),
  name VARCHAR(120) NOT NULL,
  description TEXT,
  duration_minutes INTEGER NOT NULL,
  price NUMERIC(10,2),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE professionals (
  id BIGSERIAL PRIMARY KEY,
  establishment_id BIGINT NOT NULL REFERENCES establishments(id),
  name VARCHAR(120) NOT NULL,
  bio TEXT,
  whatsapp VARCHAR(20),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE appointments (
  id BIGSERIAL PRIMARY KEY,
  establishment_id BIGINT NOT NULL REFERENCES establishments(id),
  customer_id BIGINT NOT NULL REFERENCES customers(id),
  service_item_id BIGINT NOT NULL REFERENCES service_items(id),
  professional_id BIGINT NOT NULL REFERENCES professionals(id),
  start_at TIMESTAMP NOT NULL,
  end_at TIMESTAMP NOT NULL,
  status VARCHAR(40) NOT NULL,
  cancellation_reason VARCHAR(255),
  client_ip VARCHAR(80),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  approved_at TIMESTAMP,
  completed_at TIMESTAMP
);
CREATE INDEX idx_appt_est_start ON appointments(establishment_id, start_at);
CREATE INDEX idx_appt_prof_period ON appointments(professional_id, start_at, end_at);

CREATE TABLE booking_attempts (
  id BIGSERIAL PRIMARY KEY,
  establishment_id BIGINT NOT NULL REFERENCES establishments(id),
  phone_normalized VARCHAR(20),
  ip_address VARCHAR(80),
  suspicious BOOLEAN NOT NULL DEFAULT FALSE,
  reason VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_attempt_phone ON booking_attempts(establishment_id, phone_normalized, created_at);
CREATE INDEX idx_attempt_ip ON booking_attempts(establishment_id, ip_address, created_at);

CREATE TABLE time_blocks (
  id BIGSERIAL PRIMARY KEY,
  establishment_id BIGINT NOT NULL REFERENCES establishments(id),
  professional_id BIGINT REFERENCES professionals(id),
  title VARCHAR(120) NOT NULL,
  reason VARCHAR(255),
  start_at TIMESTAMP NOT NULL,
  end_at TIMESTAMP NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO establishments(id,name,slug,whatsapp,city,description,active)
VALUES (1,'Bella Estética Studio','bella-estetica','5517999999999','Fernandópolis-SP','Atendimento com hora marcada, feito com calma e sem fila.',true);

-- Senha local de demonstração: admin123. Trocar antes de usar fora do ambiente local.
INSERT INTO users_app(establishment_id,name,email,password_hash,role,enabled)
VALUES (1,'Administrador Demo','admin@demo.local','$2y$10$SSqTDKeVQzAepUbciVQu0.XRLXRUm2BXy7FY.sZtqoo2l1fLKC3tK','OWNER',true);

INSERT INTO service_items(establishment_id,name,description,duration_minutes,price,active,sort_order) VALUES
(1,'Corte feminino','Corte com acabamento e orientação para finalizar em casa.',60,75.00,true,1),
(1,'Design de sobrancelhas','Modelagem com acabamento natural.',30,38.00,true,2),
(1,'Limpeza de pele','Higienização, extração leve e finalização calmante.',90,140.00,true,3),
(1,'Manicure','Cuidado completo das unhas das mãos.',45,35.00,true,4);

INSERT INTO professionals(establishment_id,name,bio,whatsapp,active,sort_order) VALUES
(1,'Mariana Alves','Atende cortes, sobrancelhas e finalizações.','5517999999999',true,1),
(1,'Camila Rocha','Especialista em estética facial e manicure.','5517999999999',true,2);

INSERT INTO customers(establishment_id,name,phone_normalized,no_show_count,blocked) VALUES
(1,'Ana Clara','17988887777',0,false),
(1,'João Pedro','17977776666',2,false),
(1,'Renata Martins','17966665555',0,false);

INSERT INTO appointments(establishment_id,customer_id,service_item_id,professional_id,start_at,end_at,status,cancellation_reason,client_ip) VALUES
(1,1,1,1,CURRENT_DATE + TIME '09:00',CURRENT_DATE + TIME '10:00','CONFIRMED',NULL,'127.0.0.1'),
(1,2,3,2,CURRENT_DATE + TIME '11:00',CURRENT_DATE + TIME '12:30','PENDING_APPROVAL',NULL,'127.0.0.1'),
(1,3,2,1,CURRENT_DATE + TIME '14:00',CURRENT_DATE + TIME '14:30','COMPLETED',NULL,'127.0.0.1'),
(1,1,4,2,CURRENT_DATE - 1 + TIME '16:00',CURRENT_DATE - 1 + TIME '16:45','NO_SHOW',NULL,'127.0.0.1'),
(1,3,1,1,CURRENT_DATE - 2 + TIME '10:00',CURRENT_DATE - 2 + TIME '11:00','CANCELLED','Cliente pediu remarcação pelo WhatsApp.','127.0.0.1');
