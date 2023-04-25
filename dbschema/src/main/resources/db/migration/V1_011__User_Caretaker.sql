CREATE TABLE "calendly_appointment" (
    "id" SERIAL PRIMARY KEY,
    "public_id" UUID NOT NULL DEFAULT uuid_generate_v4(),
    "created" TIMESTAMP NOT NULL,
    "uri" TEXT NOT NULL,
    "customer_id" INT NOT NULL,
    "event_id" TEXT NOT NULL,
    "invitee_id" TEXT NOT NULL,
    "start_time" TIMESTAMP NOT NULL,
    "end_time" TIMESTAMP NOT NULL,
    "cancel_reason" TEXT,
    "canceled_at" TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES "user" (id) ON DELETE CASCADE
);