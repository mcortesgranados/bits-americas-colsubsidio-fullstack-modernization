-- V2 · Índices de consulta del portal y del publicador
-- Complementan las claves únicas creadas en V1. Justificación: docs/11-diccionario-de-datos.md §11.5

-- Listado del afiliado y regla "una solicitud abierta por tipo"
CREATE INDEX idx_request_affiliate_status ON subsidy_request (affiliate_id, status);

-- Bandeja de la empresa
CREATE INDEX idx_request_company_status ON subsidy_request (company_id, status);

-- Reporte administrativo por periodo
CREATE INDEX idx_request_submitted_at ON subsidy_request (submitted_at);

-- Búsqueda de afiliados por empresa y estado
CREATE INDEX idx_affiliate_company_status ON affiliate (company_id, status);

-- Línea de tiempo de la solicitud
CREATE INDEX idx_history_request_occurred ON request_status_history (request_id, occurred_at);

-- Contador de no leídas
CREATE INDEX idx_notification_affiliate_read ON notification (affiliate_id, read_flag);

-- Publicador del outbox: PENDING más antiguos primero
CREATE INDEX idx_outbox_status_created ON outbox_event (status, created_at);

-- Cola de fallidos pendientes de revisión
CREATE INDEX idx_failed_resolved_at ON failed_message (resolved, failed_at);

-- Auditoría por fecha, entidad y acción
CREATE INDEX idx_audit_occurred ON audit_log (occurred_at);
CREATE INDEX idx_audit_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_action ON audit_log (action);
