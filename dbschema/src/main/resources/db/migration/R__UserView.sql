DROP VIEW IF EXISTS "user_view";

CREATE VIEW "user_view" AS
    SELECT "user".id,
           "user".public_id,
           "user".created,
           "user".label_id,
           "user".salesforce_id,
           "user".role,
           "user".name,
           "user".gender,
           "user".age,
           "user".email,
           "user".is_insured,
           "user".device_token,
           "user".sns_endpoint_arn,
           "latest_acc_policy".version AS privacy_version ,
           "latest_acc_policy".accepted_at AS privacy_version_accepted_at,
           "user".birthdate,
           "user".migrated_from,
           "label".public_id AS label_public_id,
           "user".phone_number,
           "user".postal_code,
           "user".house_number,
           "user".entry_type,
           "user".general_practice,
           "user".general_practice_agb_code,
           "user".general_practice_center,
           "user".general_practice_center_agb_code,
           "user".holiday_destination,
           "user".shelter_location_id,
           "user".shelter_location_name,
           "user".onboarding_details_added
    FROM "user"
    LEFT JOIN label ON "user".label_id = label.id
        -- join with the latest accepted privacy statement
    LEFT JOIN (SELECT user_id,
                      "version",
                      accepted_at,
                      ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY accepted_at DESC) as rank
               FROM privacy_statement) as latest_acc_policy ON "user".id = latest_acc_policy.user_id AND rank = 1
;
