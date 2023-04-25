-- During migrations, we may have dropped+recreated some tables or views.
-- When dropping a view, also the GRANTS as revoked.
-- This script makes sure we re-apply the grants after migrations have finished.
--
-- It only works if there is a powerbi-gateway user, which is currently a manual step in
-- deployment.

DO $$
BEGIN
    IF EXISTS (SELECT * FROM pg_user WHERE usename = 'powerbi-gateway') THEN
        RAISE NOTICE 'Granting SELECT rights for reporting user';
        GRANT SELECT ON odata_customer_view TO "powerbi-gateway";
        GRANT SELECT ON odata_message_view TO "powerbi-gateway";
        GRANT SELECT ON reporting_triage TO "powerbi-gateway";
        GRANT SELECT ON reporting_triage_answer TO "powerbi-gateway";
    ELSE
        -- Maybe role wasn't created yet - new environment?
        -- allow the server to start up without errors.
        RAISE NOTICE 'No reporting user found. Not granting rights';
    END IF;

END
$$;
