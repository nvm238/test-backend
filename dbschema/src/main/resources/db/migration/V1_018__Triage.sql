CREATE TABLE "triage_status"
(
    "id"              SERIAL PRIMARY KEY,
    "created"         TIMESTAMP NOT NULL,
    "ended"           TIMESTAMP NULL,
    "status"          VARCHAR   NOT NULL,
    "user_id"         INT       NOT NULL,
    "conversation_id" INT       NOT NULL,
    "active"          BOOLEAN   NOT NULL DEFAULT TRUE,
    "schema_version"  INT       NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    FOREIGN KEY (conversation_id) REFERENCES "conversation" (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_single_triage_active ON "triage_status" (conversation_id, user_id, active);
CREATE INDEX idx_triage_status_schema_version ON "triage_status" (schema_version);

CREATE TABLE "triage_answer"
(
    "id"               SERIAL PRIMARY KEY,
    "created"          TIMESTAMP NOT NULL,
    "question_id"      TEXT      NOT NULL,
    "answer"           JSON      NOT NULL,
    "triage_status_id" INT       NOT NULL,
    "raw_answer"       TEXT      NOT NULL,
    FOREIGN KEY (triage_status_id) REFERENCES "triage_status" (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_triage_answer_id_fk ON "triage_answer" (triage_status_id, question_id);
