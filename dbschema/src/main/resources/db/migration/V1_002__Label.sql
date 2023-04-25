CREATE TABLE "label" (
    "id" SERIAL PRIMARY KEY,
    "public_id" UUID NOT NULL DEFAULT uuid_generate_v4(),
    "created" TIMESTAMP NOT NULL,
    "code" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "active" BOOLEAN NOT NULL DEFAULT TRUE,
    "fcm_api_key" TEXT,
    "sns_application_arn" TEXT
);
CREATE UNIQUE INDEX idx_label_uuid ON "label"(public_id);
CREATE UNIQUE INDEX idx_label_code ON "label"(LOWER(code));
