CREATE TABLE "user" (
    "id" SERIAL PRIMARY KEY,
    "public_id" UUID NOT NULL DEFAULT uuid_generate_v4(),
    "created" TIMESTAMP NOT NULL,
    "label_id" INT,
    "salesforce_id" TEXT,
    "role" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "gender" TEXT,
    "age" INTEGER,
    "email" TEXT,
    "is_insured" BOOLEAN,
    "device_token" TEXT,
    "sns_endpoint_arn" TEXT,
    FOREIGN KEY (label_id) REFERENCES "label" (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_user_uuid ON "user"(public_id);
CREATE UNIQUE INDEX idx_user_salesforce_id ON "user"(salesforce_id);

CREATE TABLE "api_key" (
    "id" SERIAL PRIMARY KEY,
    "user_id" INTEGER NOT NULL,
    "api_key" TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);
