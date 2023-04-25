-- index is useful as we are searching through the stale active triages to close them
-- only small amount of total records have active=true status
CREATE INDEX active_triage_index ON triage_status(active);
