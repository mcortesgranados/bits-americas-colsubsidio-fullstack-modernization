-- V1 · Esquema inicial del prototipo (13 tablas)
-- Datos sintéticos. No reproduce la arquitectura ni los datos de Colsubsidio.
-- Diccionario de referencia: docs/11-diccionario-de-datos.md

CREATE TABLE company (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nit           VARCHAR(20)  NOT NULL,
    legal_name    VARCHAR(160) NOT NULL,
    sector        VARCHAR(60)  NULL,
    company_size  VARCHAR(20)  NULL,
    city          VARCHAR(80)  NULL,
    contact_email VARCHAR(120) NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_company_nit (nit)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE affiliate (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    document_type   VARCHAR(10)  NOT NULL,
    document_number VARCHAR(30)  NOT NULL,
    first_name      VARCHAR(80)  NOT NULL,
    last_name       VARCHAR(80)  NOT NULL,
    email           VARCHAR(120) NOT NULL,
    phone           VARCHAR(30)  NULL,
    birth_date      DATE         NULL,
    category        VARCHAR(10)  NULL,
    company_id      BIGINT       NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_affiliate_document (document_number),
    CONSTRAINT fk_affiliate_company FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE beneficiary (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    affiliate_id    BIGINT       NOT NULL,
    full_name       VARCHAR(160) NOT NULL,
    relationship    VARCHAR(20)  NOT NULL,
    document_number VARCHAR(30)  NULL,
    birth_date      DATE         NULL,
    PRIMARY KEY (id),
    KEY idx_beneficiary_affiliate (affiliate_id),
    CONSTRAINT fk_beneficiary_affiliate FOREIGN KEY (affiliate_id) REFERENCES affiliate (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE company_worker (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    company_id      BIGINT       NOT NULL,
    document_type   VARCHAR(10)  NOT NULL,
    document_number VARCHAR(30)  NOT NULL,
    first_name      VARCHAR(80)  NOT NULL,
    last_name       VARCHAR(80)  NOT NULL,
    position        VARCHAR(80)  NULL,
    hired_at        DATE         NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_worker_company_document (company_id, document_number),
    CONSTRAINT fk_worker_company FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE subsidy_request (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    request_number   VARCHAR(30)   NOT NULL,
    affiliate_id     BIGINT        NOT NULL,
    company_id       BIGINT        NULL,
    subsidy_type     VARCHAR(30)   NOT NULL,
    status           VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    requested_amount DECIMAL(14,2) NOT NULL,
    currency         VARCHAR(3)    NOT NULL DEFAULT 'COP',
    purpose          VARCHAR(400)  NULL,
    submitted_at     TIMESTAMP     NULL,
    decided_at       TIMESTAMP     NULL,
    decision_reason  VARCHAR(400)  NULL,
    version          BIGINT        NOT NULL DEFAULT 0,
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_request_number (request_number),
    CONSTRAINT fk_request_affiliate FOREIGN KEY (affiliate_id) REFERENCES affiliate (id),
    CONSTRAINT fk_request_company FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE required_document (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    request_id       BIGINT       NOT NULL,
    document_type    VARCHAR(40)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    file_name        VARCHAR(200) NULL,
    storage_key      VARCHAR(300) NULL,
    content_type     VARCHAR(100) NULL,
    size_bytes       BIGINT       NULL,
    checksum         VARCHAR(64)  NULL,
    uploaded_at      TIMESTAMP    NULL,
    reviewed_at      TIMESTAMP    NULL,
    rejection_reason VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_required_document_request_type (request_id, document_type),
    CONSTRAINT fk_document_request FOREIGN KEY (request_id) REFERENCES subsidy_request (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE request_status_history (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    request_id     BIGINT       NOT NULL,
    from_status    VARCHAR(30)  NULL,
    to_status      VARCHAR(30)  NOT NULL,
    reason         VARCHAR(400) NULL,
    actor          VARCHAR(80)  NOT NULL,
    correlation_id VARCHAR(64)  NULL,
    occurred_at    TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_history_request FOREIGN KEY (request_id) REFERENCES subsidy_request (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE notification (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    affiliate_id BIGINT        NOT NULL,
    request_id   BIGINT        NULL,
    channel      VARCHAR(20)   NOT NULL DEFAULT 'IN_APP',
    subject      VARCHAR(160)  NOT NULL,
    body         VARCHAR(1000) NOT NULL,
    read_flag    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_affiliate FOREIGN KEY (affiliate_id) REFERENCES affiliate (id),
    CONSTRAINT fk_notification_request FOREIGN KEY (request_id) REFERENCES subsidy_request (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE app_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(60)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    affiliate_id  BIGINT       NULL,
    company_id    BIGINT       NULL,
    created_at    TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_username (username),
    CONSTRAINT fk_user_affiliate FOREIGN KEY (affiliate_id) REFERENCES affiliate (id),
    CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE outbox_event (
    id             CHAR(36)     NOT NULL,
    aggregate_type VARCHAR(40)  NOT NULL,
    aggregate_id   VARCHAR(40)  NOT NULL,
    event_type     VARCHAR(80)  NOT NULL,
    payload        VARCHAR(4000) NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    attempts       INT          NOT NULL DEFAULT 0,
    correlation_id VARCHAR(64)  NULL,
    created_at     TIMESTAMP    NOT NULL,
    published_at   TIMESTAMP    NULL,
    last_error     VARCHAR(500) NULL,
    version        BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE processed_message (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    message_id   CHAR(36)    NOT NULL,
    consumer     VARCHAR(80) NOT NULL,
    outcome      VARCHAR(30) NOT NULL,
    processed_at TIMESTAMP   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_processed_message_id_consumer (message_id, consumer)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE failed_message (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    message_id CHAR(36)      NOT NULL,
    consumer   VARCHAR(80)   NOT NULL,
    event_type VARCHAR(80)   NULL,
    payload    VARCHAR(4000) NULL,
    error      VARCHAR(1000) NULL,
    attempts   INT           NOT NULL DEFAULT 0,
    failed_at  TIMESTAMP     NOT NULL,
    resolved   BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE audit_log (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    actor          VARCHAR(80)   NOT NULL,
    action         VARCHAR(60)   NOT NULL,
    entity_type    VARCHAR(50)   NULL,
    entity_id      VARCHAR(50)   NULL,
    correlation_id VARCHAR(64)   NULL,
    details        VARCHAR(1000) NULL,
    ip_address     VARCHAR(45)   NULL,
    occurred_at    TIMESTAMP     NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
