CREATE TABLE "conversation" (
    "id" SERIAL PRIMARY KEY,
    "public_id" UUID NOT NULL DEFAULT uuid_generate_v4(),
    "created" TIMESTAMP NOT NULL,
    "label_id" INT NOT NULL,
    "customer_id" INT NOT NULL,
    "delivered_to_customer" TIMESTAMP,
    "delivered_to_employee" TIMESTAMP,
    "read_by_customer" TIMESTAMP,
    "read_by_employee" TIMESTAMP,
    "status" TEXT NOT NULL,
    FOREIGN KEY (label_id) REFERENCES "label" (id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES "user" (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_conversation_uuid ON "conversation"(public_id);

CREATE TABLE "message" (
    "id" SERIAL PRIMARY KEY,
    "created" TIMESTAMP NOT NULL,
    "conversation_id" INT NOT NULL,
    "salesforce_id" TEXT,
    "message" TEXT NOT NULL,
    "user_id" INT NOT NULL,
    "public_id" UUID NOT NULL DEFAULT uuid_generate_v4(),
    FOREIGN KEY (conversation_id) REFERENCES "conversation" (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);