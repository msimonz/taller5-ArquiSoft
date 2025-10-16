-- Tabla de proveedores
CREATE TABLE IF NOT EXISTS supplier (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  email VARCHAR(200) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS product (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  price DECIMAL(12,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  supplier_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (supplier_id) REFERENCES supplier(id)
);

-- Seed proveedores
INSERT INTO supplier (name, email) VALUES
('TechSupplier Inc', 'orders@techsupplier.com'),
('Electronics Co', 'sales@electronicsco.com'),
('Hardware Store', 'contact@hardwarestore.com');

-- Seed productos con proveedores
INSERT INTO product (name, price, stock, supplier_id) VALUES
('Laptop', 2500.00, 10, 1),
('Mouse', 40.00, 100, 2),
('Teclado', 80.00, 50, 2),
('Monitor', 350.00, 25, 1),
('Webcam', 120.00, 30, 3);
