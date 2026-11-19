CREATE TABLE url_click_batch_persistence (
    id UUID PRIMARY KEY,
    url_id BIGINT NOT NULL,
    batch_id VARCHAR(100) NOT NULL UNIQUE,
    clicks BIGINT NOT NULL,
    last_clicked_at  TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);