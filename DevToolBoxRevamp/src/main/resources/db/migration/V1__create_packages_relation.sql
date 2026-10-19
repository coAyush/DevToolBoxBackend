CREATE TABLE IF NOT EXISTS package (
    id SERIAL PRIMARY KEY,
    amount NUMERIC(6,2) NOT NULL,
    role VARCHAR(50) NOT NULL,
    tier VARCHAR(50) NOT NULL,
    duration INT NOT NULL,
    is_active boolean DEFAULT true
);

INSERT INTO package (amount, role, tier, duration)
VALUES
    (0.00,  'USER',      'FREE', 30),
    (100.00,  'USER',      'PRO',  30),
    (0.00, 'DEVELOPER', 'FREE', 30),
    (200.00, 'DEVELOPER', 'PRO',  30),
    (300.00,'DEVELOPER','ULTIMATE',30)

