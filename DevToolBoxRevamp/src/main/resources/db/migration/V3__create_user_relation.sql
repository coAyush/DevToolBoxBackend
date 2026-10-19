CREATE TABLE  IF NOT EXISTS users(
 id uuid primary key,
 name varchar(100) not null,
 email varchar(100) unique not null,
 phone varchar(15) unique ,
 password varchar(255) ,
 role varchar(50) not null,
 is_verified boolean default false not null,
 account_type varchar(100) not null,
 image_url varchar(255),
 created_at timestamp DEFAULT CURRENT_TIMESTAMP,
 updated_at timestamp DEFAULT CURRENT_TIMESTAMP,
 api_key varchar(255) unique
)