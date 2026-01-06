-- Core tables for Order Management API

-- USERS
CREATE TABLE IF NOT EXISTS app_user (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- PRODUCT
CREATE TABLE IF NOT EXISTS product (
  id BIGSERIAL PRIMARY KEY,
  sku VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  price NUMERIC(12,2) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- INVENTORY (1:1 with product)
CREATE TABLE IF NOT EXISTS inventory (
  product_id BIGINT PRIMARY KEY,
  available_qty INT NOT NULL,
  reserved_qty INT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
  CONSTRAINT chk_inventory_available_nonneg CHECK (available_qty >= 0),
  CONSTRAINT chk_inventory_reserved_nonneg CHECK (reserved_qty >= 0)
);

-- ORDERS
CREATE TABLE IF NOT EXISTS orders (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  status VARCHAR(30) NOT NULL,
  total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- ORDER ITEMS
CREATE TABLE IF NOT EXISTS order_item (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price NUMERIC(12,2) NOT NULL,
  line_total NUMERIC(12,2) NOT NULL,
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT chk_order_item_qty_pos CHECK (quantity > 0),
  CONSTRAINT chk_order_item_line_total_nonneg CHECK (line_total >= 0)
);

CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_item(order_id);
CREATE INDEX IF NOT EXISTS idx_order_item_product_id ON order_item(product_id);
