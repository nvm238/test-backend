ALTER TABLE "message" ADD COLUMN translated_message TEXT;

CREATE UNIQUE INDEX idx_message_uuid ON "message"(public_id);