CREATE TABLE IF NOT EXISTS invoice (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,  -- invoice id
  payment_id BIGINT UNSIGNED NOT NULL,         -- id del pago
  PRIMARY KEY (id),
  UNIQUE KEY uq_payment (payment_id)           -- 1 factura por pago
);
