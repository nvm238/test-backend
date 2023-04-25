CREATE UNIQUE INDEX idx_calendly_appointment_public_id ON "calendly_appointment"(public_id);
CREATE INDEX idx_calendly_appointment_uri ON "calendly_appointment"(uri);
CREATE INDEX idx_calendly_appointment_salesforce_appointment_id ON "calendly_appointment"(salesforce_appointment_id);