ALTER TABLE "reporting_triage" ADD COLUMN triage_state TEXT;

UPDATE reporting_triage
SET triage_state = (SELECT triage_status.status
                    FROM triage_status
                    WHERE triage_status.id = reporting_triage.id)
WHERE reporting_triage.triage_state IS NULL;
