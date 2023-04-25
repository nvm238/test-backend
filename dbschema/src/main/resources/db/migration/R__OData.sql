DROP VIEW IF EXISTS "odata_conversation_view";
DROP VIEW IF EXISTS "odata_customer_view";
DROP VIEW IF EXISTS "odata_message_view";

CREATE VIEW "odata_customer_view" AS
    SELECT
    CAST(conversation.public_id AS varchar(50)) AS conversation_id,
    CAST("user".public_id AS varchar(50)) AS customer_id,
    "user".created as registered_at,
    "user".age AS age,
    "user".gender as gender,
    "user".is_insured as is_insured,
    latest_acc_policy.version as privacy_version,
    latest_acc_policy.accepted_at as privacy_version_accepted_at,
    label.code AS label_code,
    "user".entry_type as entry_type,
    "user".general_practice as general_practice,
    "user".general_practice_agb_code as general_practice_agb_code,
    "user".general_practice_center as general_practice_center,
    "user".general_practice_center_agb_code as general_practice_center_agb_code,
    "user".holiday_destination as holiday_destination,
    "user".shelter_location_id as shelter_location_id,
    "user".shelter_location_name as shelter_location_name
    FROM "user"
    LEFT JOIN label ON "user".label_id = label.id
    LEFT JOIN "conversation" ON "user".id = "conversation".customer_id
        -- join with the latest accepted privacy statement
    LEFT JOIN (SELECT user_id,
                      "version",
                      accepted_at,
                      ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY accepted_at DESC) as rank
               FROM privacy_statement) as latest_acc_policy ON "user".id = latest_acc_policy.user_id AND rank = 1
    WHERE "user".role = 'customer'
;

CREATE VIEW "odata_message_view" AS
    SELECT
    CAST("message".public_id AS varchar(50)) AS id,
    CAST(conversation.public_id AS varchar(50)) AS conversation_id,
    CAST("user".public_id AS varchar(50)) AS sender_id,
    (CASE WHEN ("user".id = conversation.customer_id) THEN TRUE ELSE FALSE END) AS sender_is_customer,
    (CASE WHEN ("user".role = 'customer') THEN
        CASE WHEN (message.created <= conversation.delivered_to_employee) THEN TRUE
        ELSE FALSE END
    ELSE
         CASE WHEN (message.created <= conversation.delivered_to_customer) THEN TRUE
                ELSE FALSE END
    END
    ) AS received_by_receiver,
    (CASE WHEN ("user".role = 'customer') THEN
        CASE WHEN (message.created <= conversation.read_by_employee) THEN TRUE
        ELSE FALSE END
    ELSE
         CASE WHEN (message.created <= conversation.read_by_customer) THEN TRUE
                ELSE FALSE END
    END
    ) AS read_by_receiver,
    (CASE WHEN (message_attachment.id IS NOT NULL) THEN true ELSE FALSE END) AS contains_attachment,
    message.created as send_at,
    message.message as message,
    message.translated_message as translated_message,
    (CASE WHEN ("user".role = 'admin') THEN TRUE ELSE FALSE END) AS is_system_message
    FROM "message"
    LEFT JOIN conversation ON "conversation".id = "message".conversation_id
    LEFT JOIN "user" ON "user".id = "message".user_id
    LEFT JOIN message_attachment on message_attachment.message_id = message.id
;