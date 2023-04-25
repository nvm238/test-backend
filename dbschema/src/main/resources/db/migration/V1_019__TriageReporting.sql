CREATE TABLE "reporting_triage"
(
    "id"              INT NOT NULL PRIMARY KEY,
    "label_code"      VARCHAR NOT NULL,
    "customer_id"     UUID NOT NULL,
    "chat_id"         UUID NOT NULL,
    "created"         TIMESTAMP NOT NULL,
    "ended"           TIMESTAMP NULL,
    "medical_area"    VARCHAR NULL,
    "is_self_triage"  BOOLEAN NULL,
    "version_number"  INT NOT NULL,
    "abandoned"       BOOLEAN NOT NULL,
    "end_reason"      VARCHAR NULL,
    "urgency"         VARCHAR NULL
    -- no foreign keys, we want this data to be append-only
);
-- indexes should be used to do incremental requests (ie. all data from this timestamp onwards)
CREATE INDEX idx_reporting_triage_start ON "reporting_triage" (created);
CREATE INDEX idx_reporting_triage_end ON "reporting_triage" (ended);

CREATE TABLE "reporting_triage_answer"
(
    "id"              SERIAL PRIMARY KEY,
    "triage_id"       INT NOT NULL,
    "question_id"     VARCHAR NOT NULL,
    "question_text"   VARCHAR NOT NULL,
    "answer_text"     VARCHAR NOT NULL,
    "divergent"       BOOLEAN NOT NULL
);
-- TODO: do we need timestamps here for incremental querying?
-- index for lookup by triage id (for cleanup)
CREATE INDEX idx_reporting_triage_answer_triage_id ON "reporting_triage_answer" (triage_id);
