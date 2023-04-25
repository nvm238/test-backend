CREATE TABLE "message_attachment" (
    "id" SERIAL PRIMARY KEY,
    "public_id" UUID NOT NULL DEFAULT uuid_generate_v4(),
    "created" TIMESTAMP NOT NULL,
    "customer_id" INT NOT NULL,
    "message_id" INT NOT NULL,
    "content_type" TEXT NOT NULL,
    "attachment_type" TEXT NOT NULL,
    "s3_key" TEXT,
    FOREIGN KEY (message_id) REFERENCES "message" (id) ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES "user" (id) ON DELETE RESTRICT
);
