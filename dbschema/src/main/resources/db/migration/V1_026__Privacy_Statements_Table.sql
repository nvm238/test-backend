-- create new table
CREATE TABLE "privacy_statement"
(
    "id"          SERIAL PRIMARY KEY,
    "accepted_at" TIMESTAMP NOT NULL,
    "user_id"     INT       NOT NULL,
    "version"     TEXT      NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_user_id_version ON "privacy_statement" (user_id, "version");

-- migrate existing privacy statements to new table
INSERT INTO privacy_statement (accepted_at, user_id, "version")
SELECT privacy_version_accepted_at,
       id,
       privacy_version
FROM "user"
WHERE "user".privacy_version IS NOT NULL
  AND "user".privacy_version_accepted_at IS NOT NULL;

-- drop old and now unused columns
-- ALTER TABLE "user" DROP COLUMN "privacy_version";
-- ALTER TABLE "user" DROP COLUMN "privacy_version_accepted_at";
