-- V1 baseline schema. Source of truth: docs/entity-attributes.md
-- Excludes WishlistItem and AgeVerification (marked deferred/v2 in that doc).
-- Note: entity-attributes.md specifies uuid PKs; this schema uses BIGSERIAL ids instead (deliberate deviation, v1).

CREATE TABLE customer (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    full_name           VARCHAR(255) NOT NULL,
    phone               VARCHAR(50),
    preferred_language  VARCHAR(10) NOT NULL DEFAULT 'VI' CHECK (preferred_language IN ('EN', 'VI')),
    status              VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED' CHECK (status IN ('UNVERIFIED', 'ACTIVE', 'DISABLED')),
    email_verified_at   TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE pharmacy_admin (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20) NOT NULL DEFAULT 'admin' CHECK (role IN ('admin')),
    status        VARCHAR(20) NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'disabled')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE address (
    id             BIGSERIAL PRIMARY KEY,
    customer_id    BIGINT NOT NULL REFERENCES customer(id),
    recipient_name VARCHAR(255) NOT NULL,
    street_line1   VARCHAR(255) NOT NULL,
    street_line2   VARCHAR(255),
    city           VARCHAR(120) NOT NULL,
    state          VARCHAR(120) NOT NULL,
    zip            VARCHAR(20) NOT NULL,
    phone          VARCHAR(50),
    is_default     BOOLEAN NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_address_customer_id ON address(customer_id);

CREATE TABLE payment_method (
    id            BIGSERIAL PRIMARY KEY,
    customer_id   BIGINT NOT NULL REFERENCES customer(id),
    gateway_token VARCHAR(255) NOT NULL,
    type          VARCHAR(20) NOT NULL CHECK (type IN ('card', 'paypal', 'apple_pay')),
    display_label VARCHAR(255) NOT NULL,
    is_default    BOOLEAN NOT NULL DEFAULT false,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payment_method_customer_id ON payment_method(customer_id);

CREATE TABLE category (
    id                  BIGSERIAL PRIMARY KEY,
    name_en             VARCHAR(255) NOT NULL,
    name_vi             VARCHAR(255),
    parent_category_id  BIGINT REFERENCES category(id),
    sort_order          INT
);
CREATE INDEX idx_category_parent_category_id ON category(parent_category_id);

CREATE TABLE product (
    id              BIGSERIAL PRIMARY KEY,
    name_en         VARCHAR(255) NOT NULL,
    name_vi         VARCHAR(255),
    description_en  TEXT,
    description_vi  TEXT,
    category_id     BIGINT NOT NULL REFERENCES category(id),
    price           NUMERIC(10,2) NOT NULL,
    image_ref       VARCHAR(500),
    stock_quantity  INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    status          VARCHAR(20) NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'unpublished')),
    is_medication   BOOLEAN NOT NULL DEFAULT false,
    is_restricted   BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_product_category_id ON product(category_id);

CREATE TABLE cart (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customer(id),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cart_item (
    id         BIGSERIAL PRIMARY KEY,
    cart_id    BIGINT NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id),
    quantity   INT NOT NULL CHECK (quantity >= 1),
    added_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (cart_id, product_id)
);
CREATE INDEX idx_cart_item_product_id ON cart_item(product_id);

CREATE TABLE cust_order (
    id               BIGSERIAL PRIMARY KEY,
    order_number     VARCHAR(50) NOT NULL UNIQUE,
    customer_id      BIGINT NOT NULL REFERENCES customer(id),
    status           VARCHAR(30) NOT NULL DEFAULT 'placed' CHECK (status IN
                        ('placed', 'packed', 'ready_for_pickup', 'out_for_delivery', 'delivered', 'completed', 'canceled', 'refunded')),
    fulfillment_type VARCHAR(20) NOT NULL CHECK (fulfillment_type IN ('pickup', 'delivery')),
    address_id       BIGINT REFERENCES address(id),
    subtotal         NUMERIC(10,2) NOT NULL,
    discount_total   NUMERIC(10,2) NOT NULL DEFAULT 0,
    total            NUMERIC(10,2) NOT NULL,
    placed_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (fulfillment_type <> 'delivery' OR address_id IS NOT NULL)
);
CREATE INDEX idx_cust_order_customer_id ON cust_order(customer_id);
CREATE INDEX idx_cust_order_address_id ON cust_order(address_id);

CREATE TABLE order_item (
    id                       BIGSERIAL PRIMARY KEY,
    order_id                 BIGINT NOT NULL REFERENCES cust_order(id),
    product_id               BIGINT NOT NULL REFERENCES product(id),
    product_name_snapshot    VARCHAR(255) NOT NULL,
    quantity                 INT NOT NULL CHECK (quantity >= 1),
    unit_price_at_purchase   NUMERIC(10,2) NOT NULL,
    line_total               NUMERIC(10,2) NOT NULL,
    is_medication_snapshot   BOOLEAN NOT NULL
);
CREATE INDEX idx_order_item_order_id ON order_item(order_id);
CREATE INDEX idx_order_item_product_id ON order_item(product_id);

CREATE TABLE discount (
    id         BIGSERIAL PRIMARY KEY,
    scope      VARCHAR(20) NOT NULL CHECK (scope IN ('product', 'category')),
    target_id  BIGINT NOT NULL,
    type       VARCHAR(20) NOT NULL CHECK (type IN ('percent', 'amount')),
    value      NUMERIC(10,2) NOT NULL,
    start_date TIMESTAMPTZ NOT NULL,
    end_date   TIMESTAMPTZ NOT NULL,
    is_active  BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (end_date > start_date)
);
CREATE INDEX idx_discount_target_id ON discount(target_id);

CREATE TABLE payment (
    id                     BIGSERIAL PRIMARY KEY,
    order_id               BIGINT NOT NULL REFERENCES cust_order(id),
    gateway_transaction_id VARCHAR(255) NOT NULL UNIQUE,
    amount                 NUMERIC(10,2) NOT NULL,
    method_type            VARCHAR(20) NOT NULL CHECK (method_type IN ('card', 'paypal', 'apple_pay')),
    status                 VARCHAR(30) NOT NULL CHECK (status IN
                              ('authorized', 'captured', 'declined', 'voided', 'refunded', 'partially_refunded')),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payment_order_id ON payment(order_id);

CREATE TABLE return_request (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT NOT NULL REFERENCES cust_order(id),
    customer_id    BIGINT NOT NULL REFERENCES customer(id),
    reason         VARCHAR(500) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'denied')),
    requested_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    decided_at     TIMESTAMPTZ,
    decided_by     BIGINT REFERENCES pharmacy_admin(id)
);
CREATE INDEX idx_return_request_order_id ON return_request(order_id);
CREATE INDEX idx_return_request_customer_id ON return_request(customer_id);

CREATE TABLE return_item (
    id                 BIGSERIAL PRIMARY KEY,
    return_request_id  BIGINT NOT NULL REFERENCES return_request(id) ON DELETE CASCADE,
    order_item_id      BIGINT NOT NULL REFERENCES order_item(id),
    quantity           INT NOT NULL CHECK (quantity >= 1)
);
CREATE INDEX idx_return_item_order_item_id ON return_item(order_item_id);

CREATE TABLE refund (
    id                 BIGSERIAL PRIMARY KEY,
    payment_id         BIGINT NOT NULL REFERENCES payment(id),
    return_request_id  BIGINT REFERENCES return_request(id),
    amount             NUMERIC(10,2) NOT NULL,
    gateway_refund_id  VARCHAR(255) NOT NULL UNIQUE,
    reason             VARCHAR(500),
    status             VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'completed', 'failed')),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refund_payment_id ON refund(payment_id);
CREATE INDEX idx_refund_return_request_id ON refund(return_request_id);

CREATE TABLE delivery (
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT NOT NULL UNIQUE REFERENCES cust_order(id),
    drop_off_method   VARCHAR(20) NOT NULL CHECK (drop_off_method IN ('door', 'mailbox', 'hand_over')),
    instructions      TEXT,
    courier_reference VARCHAR(255),
    tracking_number   VARCHAR(255),
    status            VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN
                         ('pending', 'picked_up', 'en_route', 'delivered', 'failed')),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE proof_photo (
    id           BIGSERIAL PRIMARY KEY,
    delivery_id  BIGINT NOT NULL REFERENCES delivery(id) ON DELETE CASCADE,
    type         VARCHAR(20) NOT NULL CHECK (type IN ('pickup', 'drop_off')),
    image_ref    VARCHAR(500) NOT NULL,
    captured_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_proof_photo_delivery_id ON proof_photo(delivery_id);
