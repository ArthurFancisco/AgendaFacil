CREATE TABLE professional_services (
  professional_id BIGINT NOT NULL REFERENCES professionals(id) ON DELETE CASCADE,
  service_item_id BIGINT NOT NULL REFERENCES service_items(id) ON DELETE CASCADE,
  CONSTRAINT pk_professional_services PRIMARY KEY (professional_id, service_item_id)
);

CREATE OR REPLACE FUNCTION validate_professional_service_establishment()
RETURNS TRIGGER AS $$
DECLARE
  professional_establishment BIGINT;
  service_establishment BIGINT;
BEGIN
  SELECT establishment_id INTO professional_establishment
  FROM professionals
  WHERE id = NEW.professional_id;

  SELECT establishment_id INTO service_establishment
  FROM service_items
  WHERE id = NEW.service_item_id;

  IF professional_establishment IS NULL OR service_establishment IS NULL OR professional_establishment <> service_establishment THEN
    RAISE EXCEPTION 'Professional and service must belong to the same establishment';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_professional_services_same_establishment
BEFORE INSERT OR UPDATE ON professional_services
FOR EACH ROW EXECUTE FUNCTION validate_professional_service_establishment();

UPDATE establishments
SET name = 'AgendaFácil Demo',
    slug = 'agenda-demo',
    description = 'Ambiente de demonstração para testar agendamentos de serviços.'
WHERE id = 1;

UPDATE service_items
SET name = 'Atendimento Rápido',
    description = 'Serviço objetivo para demandas simples.',
    duration_minutes = 30,
    price = 60.00,
    sort_order = 1,
    active = true
WHERE id = 1;

UPDATE service_items
SET name = 'Atendimento Completo',
    description = 'Atendimento com avaliação e execução detalhada.',
    duration_minutes = 60,
    price = 120.00,
    sort_order = 2,
    active = true
WHERE id = 2;

UPDATE service_items
SET name = 'Serviço Premium',
    description = 'Atendimento estendido para casos que exigem mais tempo.',
    duration_minutes = 90,
    price = 180.00,
    sort_order = 3,
    active = true
WHERE id = 3;

UPDATE service_items
SET name = 'Consultoria Expressa',
    description = 'Orientação rápida com horário marcado.',
    duration_minutes = 45,
    price = 90.00,
    sort_order = 4,
    active = true
WHERE id = 4;

UPDATE professionals
SET name = 'Profissional 1',
    bio = 'Realiza atendimentos rápidos e completos.',
    sort_order = 1,
    active = true
WHERE id = 1;

UPDATE professionals
SET name = 'Profissional 2',
    bio = 'Realiza atendimentos completos e premium.',
    sort_order = 2,
    active = true
WHERE id = 2;

INSERT INTO professionals(id, establishment_id, name, bio, whatsapp, active, sort_order)
VALUES (3, 1, 'Profissional 3', 'Realiza consultorias expressas e serviços premium.', '5517999999999', true, 3)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    bio = EXCLUDED.bio,
    whatsapp = EXCLUDED.whatsapp,
    active = EXCLUDED.active,
    sort_order = EXCLUDED.sort_order;

SELECT setval('professionals_id_seq', (SELECT MAX(id) FROM professionals));

UPDATE appointments
SET professional_id = 3
WHERE establishment_id = 1
  AND service_item_id = 4
  AND professional_id = 2;

INSERT INTO professional_services(professional_id, service_item_id) VALUES
(1, 1),
(1, 2),
(2, 2),
(2, 3),
(3, 4),
(3, 3)
ON CONFLICT DO NOTHING;
