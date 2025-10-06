CREATE TABLE IF NOT EXISTS product (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  price DECIMAL(12,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Seed opcional
INSERT INTO product (name, price, stock, version) VALUES
('Laptop', 2500.00, 10, 1),
('Mouse', 40.00, 100, 1),
('Teclado', 80.00, 50, 1);
