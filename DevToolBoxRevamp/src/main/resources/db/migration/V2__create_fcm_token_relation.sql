CREATE TABLE  IF NOT EXISTS fcm_token (id serial primary key,
              user_id uuid unique not null,
              device_token VARCHAR(255) NOT NULL,
              is_active boolean not null,
              created_at timestamp not null,
              update_at timestamp not null
           )