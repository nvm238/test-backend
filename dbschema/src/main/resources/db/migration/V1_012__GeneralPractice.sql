CREATE TABLE "general_practice_center" (
    "id" UUID PRIMARY KEY,
    "label_id" UUID NOT NULL,
    "created" TIMESTAMP NOT NULL,
    "name" TEXT NOT NULL,
    FOREIGN KEY (label_id) REFERENCES "label" (public_id) ON DELETE CASCADE
);

CREATE TABLE "general_practice" (
    "id" UUID PRIMARY KEY,
    "label_id" UUID NOT NULL,
    "created" TIMESTAMP NOT NULL,
    "name" TEXT NOT NULL,
    "can_contact_office" BOOL NOT NULL,
    "contact_office_text" TEXT,
    "contact_office_button_text" TEXT,
    FOREIGN KEY (label_id) REFERENCES "label" (public_id) ON DELETE CASCADE
);

CREATE TABLE "general_practice_practitioner" (
    "id" UUID PRIMARY KEY,
    "label_id" UUID NOT NULL,
    "created" TIMESTAMP NOT NULL,
    "general_practice_id" UUID NOT NULL,
    "name" TEXT NOT NULL,
    FOREIGN KEY (general_practice_id) REFERENCES "general_practice" (id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES "label" (public_id) ON DELETE CASCADE
);

ALTER TABLE "user" ADD COLUMN birthdate TIMESTAMP;