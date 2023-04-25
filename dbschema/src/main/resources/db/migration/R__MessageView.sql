DROP VIEW IF EXISTS "message_view";

CREATE VIEW "message_view" AS
    SELECT "message".id,
           "message".public_id,
           "message".created,
           "message".conversation_id,
           "message".message,
           "message".translated_message,
           "message".action_type,
           "message".action_context,
           "user".public_id as user_public_id,
           "user".name as user_name,
           "user".role as user_role,
           "message_attachment".public_id as attachment_id,
           "message_attachment".attachment_type as attachment_content_type,
           "conversation".public_id as conversation_public_id
    FROM "message"
    LEFT JOIN "user" ON message.user_id = "user".id
    LEFT JOIN message_attachment ON message.id = message_attachment.message_id
    LEFT JOIN conversation ON conversation.id = message.conversation_id
;
