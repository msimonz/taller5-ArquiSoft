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
  
  private static final String CUSTOMER_QUEUE = "customer-notifications";

  public void notifyCustomer(CustomerNotification notification) {
    log.info("Enviando notificación al cliente {} (email: {})", 
        notification.getCustomerId(), 
        notification.getCustomerEmail());
    
    try {
      jmsTemplate.convertAndSend(CUSTOMER_QUEUE, notification);
      log.info("Notificación enviada exitosamente al cliente {}", 
          notification.getCustomerId());
    } catch (Exception ex) {
      log.error("Error enviando notificación al cliente {}: {}", 
          notification.getCustomerId(), 
          ex.getMessage(), ex);
    }
  }
}
