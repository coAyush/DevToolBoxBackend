CREATE TABLE IF NOT EXISTS refresh_tokens(
   id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
   user_id UUID unique not null,
   token VARCHAR(255) not null unique,
   is_revoked boolean DEFAULT FALSE NOT NULL,
   duration TIMESTAMP NOT NULL,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT fk_refresh_token_user_id FOREIGN KEY(user_id)  REFERENCES users(id)
)
