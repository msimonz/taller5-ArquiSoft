package com.taller5.supplier.listener;

import com.taller5.supplier.dto.SupplierNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SupplierKafkaListener {

  @KafkaListener(topicPattern = "supplier-.*", groupId = "supplier-listener-group")
  public void listen(SupplierNotification notification) {
    log.info("========================================");
    log.info("NOTIFICACION RECIBIDA PARA PROVEEDOR");
    log.info("========================================");
    log.info("Proveedor ID: {}", notification.getSupplierId());
    log.info("Proveedor: {}", notification.getSupplierName());
    log.info("Email: {}", notification.getSupplierEmail());
    log.info("Factura ID: {}", notification.getInvoiceId());
    log.info("Cliente ID: {}", notification.getCustomerId());
    log.info("Monto Total: {}", notification.getTotalAmount());
    log.info("Productos vendidos:");
    
    notification.getProducts().forEach(product -> {
      log.info("  - {} x{} @ {} = {}", 
          product.getProductName(),
          product.getQuantity(),
          product.getUnitPrice(),
          product.getSubtotal());
    });
    
    log.info("Timestamp: {}", notification.getTimestamp());
    log.info("========================================");
  }
}
