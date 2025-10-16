package com.taller5.billing.service;

import com.taller5.billing.dto.SupplierNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaSupplierNotificationService {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static final String TOPIC_PREFIX = "supplier-";

  public void notifySupplier(SupplierNotification notification) {
    String topic = TOPIC_PREFIX + notification.getSupplierId();
    
    log.info("Enviando notificación al proveedor {} (topic: {})", 
        notification.getSupplierName(), topic);
    
    kafkaTemplate.send(topic, String.valueOf(notification.getInvoiceId()), notification)
        .whenComplete((result, ex) -> {
          if (ex == null) {
            log.info("Notificación enviada exitosamente al proveedor {} - offset: {}", 
                notification.getSupplierName(), 
                result.getRecordMetadata().offset());
          } else {
            log.error("Error enviando notificación al proveedor {}: {}", 
                notification.getSupplierName(), 
                ex.getMessage());
          }
        });
  }
}
