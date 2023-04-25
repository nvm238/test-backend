CREATE TABLE "user_app_self_test_result" (
    "id" SERIAL PRIMARY KEY,
    "label_id" INT NOT NULL,
    "customer_id" INT NOT NULL,
    "data" JSONB NOT NULL,
    FOREIGN KEY (label_id) REFERENCES "label" (id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES "user" (id) ON DELETE CASCADE
);