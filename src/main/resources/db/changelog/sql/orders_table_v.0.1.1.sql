CREATE TABLE orders (
    order_id BIGINT PRIMARY KEY DEFAULT nextval('order_sequence'),
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    coupon_code VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);