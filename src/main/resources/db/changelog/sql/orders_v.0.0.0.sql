CREATE TABLE orders (
    order_id BIGSERIAL PRIMARY KEY,
    order_amount NUMERIC(10,2) NOT NULL CHECK (order_amount >= 0),
    coupon_code INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
