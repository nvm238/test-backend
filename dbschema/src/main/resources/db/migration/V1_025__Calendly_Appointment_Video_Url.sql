ALTER TABLE "calendly_appointment" ADD COLUMN appointment_type TEXT;
ALTER TABLE "calendly_appointment" ADD COLUMN video_meeting_url TEXT;
ALTER TABLE "calendly_appointment" ADD COLUMN request_fail_reason TEXT;

UPDATE "calendly_appointment" SET appointment_type = 'regular';

ALTER TABLE "calendly_appointment" ALTER COLUMN appointment_type SET NOT NULL ;
