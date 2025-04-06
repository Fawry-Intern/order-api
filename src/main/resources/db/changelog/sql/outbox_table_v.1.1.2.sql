CREATE TABLE outbox (
    outbox_id SERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    saga_event_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_contact VARCHAR(20),
    governorate VARCHAR(100),
    city VARCHAR(100),
    address VARCHAR(255),
    payment_amount NUMERIC(19, 2),
    number VARCHAR(20),
    cvv VARCHAR(10),
    expiry VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN NOT NULL
);
