package com.taller5.billing.service;

import com.taller5.billing.dto.CustomerNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JmsCustomerNotificationService {

  private final JmsTemplate jmsTemplate;
  
  // Artemis Core Protocol usa directamente el nombre de la cola/address, NO JNDI
  private static final String CUSTOMER_QUEUE = "jms.queue.customerNotifications";

  public void notifyCustomer(CustomerNotification notification) {
    log.info("📧 Enviando notificación al cliente {} (email: {}) a cola: {}", 
        notification.getCustomerId(), 
        notification.getCustomerEmail(),
        CUSTOMER_QUEUE);
    
    try {
      // Debug: ver detalles del ConnectionFactory
      var cf = jmsTemplate.getConnectionFactory();
      log.info("🔍 DEBUG ConnectionFactory class: {}", cf.getClass().getName());
      log.info("🔍 DEBUG ConnectionFactory toString: {}", cf.toString());
      
      jmsTemplate.convertAndSend(CUSTOMER_QUEUE, notification);
      log.info("✅ Notificación JMS enviada exitosamente al cliente {}", 
          notification.getCustomerId());
    } catch (Exception ex) {
      log.error("❌ ERROR FATAL enviando notificación JMS al cliente {}: {}", 
          notification.getCustomerId(), 
          ex.getMessage(), ex);
      throw new RuntimeException("Failed to send JMS notification", ex);
    }
  }
}
