CREATE TABLE "app_self_test" (
    "id" SERIAL PRIMARY KEY,
    "label_id" INT NOT NULL,
    "data" JSONB NOT NULL,
    FOREIGN KEY (label_id) REFERENCES "label" (id) ON DELETE CASCADE
);