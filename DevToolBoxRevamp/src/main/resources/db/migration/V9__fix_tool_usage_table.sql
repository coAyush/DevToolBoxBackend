ALTER TABLE tools_usage
ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE tools_usage
DROP CONSTRAINT IF EXISTS tools_usage_user_id_key;

ALTER TABLE refresh_tokens
ALTER COLUMN token TYPE TEXT;
