ALTER TABLE "user" ADD COLUMN "entry_type" TEXT;
ALTER TABLE "user" ADD COLUMN "general_practice" TEXT;
ALTER TABLE "user" ADD COLUMN "general_practice_agb_code" TEXT;
ALTER TABLE "user" ADD COLUMN "general_practice_center" TEXT;
ALTER TABLE "user" ADD COLUMN "general_practice_center_agb_code" TEXT;
ALTER TABLE "user" ADD COLUMN "holiday_destination" TEXT;
ALTER TABLE "user" ADD COLUMN "onboarding_details_added" BOOLEAN DEFAULT FALSE;