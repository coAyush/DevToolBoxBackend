CREATE TABLE payment (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL,
    package_id     INT NOT NULL,
    order_id       VARCHAR(64) NOT NULL UNIQUE,   -- Razorpay ka order_xxx
    payment_id     VARCHAR(64),
    signature      VARCHAR(256),
    amount_paise   BIGINT NOT NULL,
    currency       VARCHAR(10) NOT NULL DEFAULT 'INR',
    status         VARCHAR(20) NOT NULL DEFAULT 'CREATED',   -- CREATED | PAID
    failure_reason VARCHAR(500),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_user    FOREIGN KEY (user_id)    REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_package FOREIGN KEY (package_id) REFERENCES package(id)
);
CREATE INDEX idx_payment_user ON payment(user_id);