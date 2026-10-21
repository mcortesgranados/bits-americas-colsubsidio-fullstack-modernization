-- V3 · Datos sintéticos de demostración
-- Personas y empresas ficticias. Los correos usan el dominio reservado example.test.
-- No se siembran usuarios en app_user: la contraseña debe ser un hash BCrypt real, y ese
-- sembrado corresponde al DemoUserInitializer (pendiente, ver docs/07 §B4).

INSERT INTO company (id, nit, legal_name, sector, company_size, city, contact_email, status, created_at, updated_at) VALUES
(1, '900123456-7', 'Empresa Demo Andina S.A.S.', 'MANUFACTURA', 'MEDIUM', 'Bogotá D.C.', 'contacto@demo-andina.example.test', 'ACTIVE', '2026-09-01 08:00:00', '2026-09-01 08:00:00'),
(2, '900987654-3', 'Comercializadora Demo Sabana Ltda', 'COMERCIO', 'SMALL', 'Chía', 'contacto@demo-sabana.example.test', 'ACTIVE', '2026-09-02 08:00:00', '2026-09-02 08:00:00');

INSERT INTO affiliate (id, document_type, document_number, first_name, last_name, email, phone, birth_date, category, company_id, status, created_at, updated_at) VALUES
(1, 'CC', '1020304050', 'Ana María', 'Rojas Peña', 'ana.rojas@example.test', '3001234567', '1990-04-12', 'A', 1, 'ACTIVE', '2026-09-03 09:00:00', '2026-09-03 09:00:00'),
(2, 'CC', '79876543', 'Carlos Andrés', 'Mejía Torres', 'carlos.mejia@example.test', '3109876543', '1985-11-03', 'B', 2, 'ACTIVE', '2026-09-03 09:10:00', '2026-09-03 09:10:00'),
(3, 'CC', '1015987654', 'Luisa Fernanda', 'Gómez Díaz', 'luisa.gomez@example.test', '3205557788', '1998-07-25', 'A', 1, 'ACTIVE', '2026-09-03 09:20:00', '2026-09-03 09:20:00');

INSERT INTO beneficiary (id, affiliate_id, full_name, relationship, document_number, birth_date) VALUES
(1, 1, 'Sofía Rojas Rojas', 'HIJO', '1015203040', '2015-02-18'),
(2, 1, 'Julián Rojas Rojas', 'HIJO', '1025304050', '2018-09-30'),
(3, 2, 'Marta Torres de Mejía', 'CONYUGE', '41678901', '1987-01-22'),
(4, 3, 'Emilia Gómez Díaz', 'HIJO', '1045607080', '2021-06-14');

INSERT INTO company_worker (id, company_id, document_type, document_number, first_name, last_name, position, hired_at, status, created_at, updated_at) VALUES
(1, 1, 'CC', '1020304050', 'Ana María', 'Rojas Peña', 'Analista de calidad', '2019-03-01', 'ACTIVE', '2026-09-04 10:00:00', '2026-09-04 10:00:00'),
(2, 1, 'CC', '1015987654', 'Luisa Fernanda', 'Gómez Díaz', 'Auxiliar administrativa', '2021-08-16', 'ACTIVE', '2026-09-04 10:05:00', '2026-09-04 10:05:00'),
(3, 2, 'CC', '79876543', 'Carlos Andrés', 'Mejía Torres', 'Jefe de bodega', '2017-05-22', 'ACTIVE', '2026-09-04 10:10:00', '2026-09-04 10:10:00');

INSERT INTO subsidy_request (id, request_number, affiliate_id, company_id, subsidy_type, status, requested_amount, currency, purpose, submitted_at, decided_at, decision_reason, version, created_at, updated_at) VALUES
(1, 'SOL-2026-0001', 1, 1, 'EDUCACION', 'IN_REVIEW', 2500000.00, 'COP', 'Matrícula del segundo semestre de 2026', '2026-10-02 14:00:00', NULL, NULL, 2, '2026-10-02 13:58:00', '2026-10-02 14:05:00'),
(2, 'SOL-2026-0002', 2, 2, 'VIVIENDA', 'PENDING_DOCUMENTS', 8000000.00, 'COP', 'Cuota inicial de vivienda de interés social', '2026-10-05 09:30:00', NULL, NULL, 1, '2026-10-05 09:28:00', '2026-10-05 09:35:00'),
(3, 'SOL-2026-0003', 3, 1, 'RECREACION', 'SUBMITTED', 450000.00, 'COP', 'Plan de vacaciones familiares', '2026-10-08 16:45:00', NULL, NULL, 0, '2026-10-08 16:44:00', '2026-10-08 16:45:00');

INSERT INTO required_document (id, request_id, document_type, status, file_name, storage_key, content_type, size_bytes, checksum, uploaded_at, reviewed_at, rejection_reason) VALUES
(1, 1, 'ID_DOCUMENT', 'VALIDATED', 'cedula-ana-rojas.pdf', '1/9f1c2d3e-4b5a-4c6d-8e7f-0a1b2c3d4e5f.pdf', 'application/pdf', 184320, '3b1f9c7a5e2d4b8f6a0c1d3e5f7a9b1c2d4e6f8a0b1c3d5e7f9a1b3c5d7e9f1a', '2026-10-02 14:10:00', '2026-10-02 15:00:00', NULL),
(2, 1, 'ENROLLMENT_CERTIFICATE', 'RECEIVED', 'certificado-matricula.pdf', '1/8e2d3c4b-5a6f-7e8d-9c0b-1a2b3c4d5e6f.pdf', 'application/pdf', 240128, '7c2e4a6b8d0f1a3c5e7b9d1f3a5c7e9b1d3f5a7c9e1b3d5f7a9c1e3b5d7f9a1c', '2026-10-02 14:12:00', NULL, NULL),
(3, 2, 'BANK_CERTIFICATE', 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 2, 'INCOME_CERTIFICATE', 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(5, 3, 'ID_DOCUMENT', 'PENDING', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO request_status_history (id, request_id, from_status, to_status, reason, actor, correlation_id, occurred_at) VALUES
(1, 1, 'DRAFT', 'SUBMITTED', 'Radicación del afiliado', 'ana.rojas', 'b1e2f3a4-5c6d-7e8f-9a0b-1c2d3e4f5a6b', '2026-10-02 14:00:00'),
(2, 1, 'SUBMITTED', 'IN_REVIEW', 'Evidencia completa', 'system:subsidy-consumer', 'b1e2f3a4-5c6d-7e8f-9a0b-1c2d3e4f5a6b', '2026-10-02 14:05:00'),
(3, 2, 'DRAFT', 'SUBMITTED', 'Radicación del afiliado', 'carlos.mejia', 'c2f3a4b5-6d7e-8f9a-0b1c-2d3e4f5a6b7c', '2026-10-05 09:30:00'),
(4, 2, 'SUBMITTED', 'PENDING_DOCUMENTS', 'Faltan certificado bancario y de ingresos', 'system:subsidy-consumer', 'c2f3a4b5-6d7e-8f9a-0b1c-2d3e4f5a6b7c', '2026-10-05 09:35:00'),
(5, 3, 'DRAFT', 'SUBMITTED', 'Radicación del afiliado', 'luisa.gomez', 'd3a4b5c6-7e8f-9a0b-1c2d-3e4f5a6b7c8d', '2026-10-08 16:45:00');

INSERT INTO notification (id, affiliate_id, request_id, channel, subject, body, read_flag, created_at) VALUES
(1, 1, 1, 'IN_APP', 'Tu solicitud está en revisión', 'La solicitud SOL-2026-0001 pasó a revisión. Te avisaremos cuando haya una decisión.', FALSE, '2026-10-02 14:05:00'),
(2, 2, 2, 'IN_APP', 'Faltan documentos', 'Para continuar con SOL-2026-0002 necesitamos el certificado bancario y el de ingresos.', FALSE, '2026-10-05 09:35:00'),
(3, 3, 3, 'IN_APP', 'Solicitud radicada', 'Recibimos tu solicitud SOL-2026-0003. El número de radicado es tu referencia.', TRUE, '2026-10-08 16:45:00');

INSERT INTO outbox_event (id, aggregate_type, aggregate_id, event_type, payload, status, attempts, correlation_id, created_at, published_at, last_error, version) VALUES
('9f1c2d3e-4b5a-4c6d-8e7f-0a1b2c3d4e5f', 'SubsidyRequest', '1', 'subsidy.request.submitted', '{"messageId":"9f1c2d3e-4b5a-4c6d-8e7f-0a1b2c3d4e5f","eventType":"subsidy.request.submitted","aggregateId":"1"}', 'PUBLISHED', 1, 'b1e2f3a4-5c6d-7e8f-9a0b-1c2d3e4f5a6b', '2026-10-02 14:00:00', '2026-10-02 14:00:05', NULL, 1),
('a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d', 'SubsidyRequest', '2', 'subsidy.request.submitted', '{"messageId":"a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d","eventType":"subsidy.request.submitted","aggregateId":"2"}', 'PUBLISHED', 1, 'c2f3a4b5-6d7e-8f9a-0b1c-2d3e4f5a6b7c', '2026-10-05 09:30:00', '2026-10-05 09:30:04', NULL, 1);

INSERT INTO processed_message (id, message_id, consumer, outcome, processed_at) VALUES
(1, '9f1c2d3e-4b5a-4c6d-8e7f-0a1b2c3d4e5f', 'subsidy-consumer', 'PROCESSED', '2026-10-02 14:05:00'),
(2, 'a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d', 'subsidy-consumer', 'PROCESSED', '2026-10-05 09:35:00');

INSERT INTO failed_message (id, message_id, consumer, event_type, payload, error, attempts, failed_at, resolved) VALUES
(1, 'e4b5c6d7-8f9a-0b1c-2d3e-4f5a6b7c8d9e', 'subsidy-consumer', 'subsidy.document.uploaded', '{"messageId":"e4b5c6d7-8f9a-0b1c-2d3e-4f5a6b7c8d9e","aggregateId":"1"}', 'Transient database timeout after 3 attempts', 3, '2026-10-06 11:22:00', FALSE);

INSERT INTO audit_log (id, actor, action, entity_type, entity_id, correlation_id, details, ip_address, occurred_at) VALUES
(1, 'ana.rojas', 'PII_ACCESS', 'Affiliate', '1', 'b1e2f3a4-5c6d-7e8f-9a0b-1c2d3e4f5a6b', 'Consulta del perfil propio', '190.0.0.10', '2026-10-02 13:55:00'),
(2, 'system:subsidy-consumer', 'STATUS_CHANGE', 'SubsidyRequest', '1', 'b1e2f3a4-5c6d-7e8f-9a0b-1c2d3e4f5a6b', 'SUBMITTED -> IN_REVIEW', NULL, '2026-10-02 14:05:00'),
(3, 'auditor.demo', 'REPORT_VIEW', 'Company', '1', 'f5c6d7e8-9a0b-1c2d-3e4f-5a6b7c8d9e0f', 'Consulta del reporte agregado', '190.0.0.11', '2026-10-09 08:10:00');
