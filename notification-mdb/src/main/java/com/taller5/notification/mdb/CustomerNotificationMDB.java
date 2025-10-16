package com.taller5.notification.mdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller5.notification.dto.CustomerNotification;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "customerNotifications"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
    }
)
public class CustomerNotificationMDB implements MessageListener {

  private static final Logger LOGGER = Logger.getLogger(CustomerNotificationMDB.class.getName());
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void onMessage(Message message) {
    try {
      if (message instanceof TextMessage) {
        TextMessage textMessage = (TextMessage) message;
        String json = textMessage.getText();
        
        LOGGER.info("========================================");
        LOGGER.info("EMAIL NOTIFICATION RECEIVED");
        LOGGER.info("========================================");
        
        CustomerNotification notification = objectMapper.readValue(json, CustomerNotification.class);
        
        sendEmail(notification);
        
        LOGGER.info("========================================");
        LOGGER.info("Email sent successfully");
        LOGGER.info("========================================");
        
      } else {
        LOGGER.warning("Message received is not TextMessage: " + message.getClass().getName());
      }
    } catch (JMSException e) {
      LOGGER.log(Level.SEVERE, "Error processing JMS message", e);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error processing notification", e);
    }
  }

  private void sendEmail(CustomerNotification notification) {
    LOGGER.info("TO: " + notification.getCustomerEmail());
    LOGGER.info("SUBJECT: Purchase Confirmation - Invoice #" + notification.getInvoiceId());
    LOGGER.info("BODY:");
    LOGGER.info("  Hello Customer #" + notification.getCustomerId() + ",");
    LOGGER.info("");
    LOGGER.info("  Thank you for your purchase. Here are the details:");
    LOGGER.info("");
    LOGGER.info("  Invoice #: " + notification.getInvoiceId());
    LOGGER.info("  Date: " + notification.getTimestamp());
    LOGGER.info("");
    LOGGER.info("  Purchased products:");
    
    if (notification.getItems() != null) {
      for (CustomerNotification.PurchasedItem item : notification.getItems()) {
        LOGGER.info(String.format("    - %s x%d @ $%s = $%s",
            item.getProductName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getSubtotal()));
      }
    }
    
    LOGGER.info("");
    LOGGER.info("  TOTAL: $" + notification.getTotalAmount() + " " + notification.getCurrency());
    LOGGER.info("");
    LOGGER.info("  Thank you for your purchase!");
  }
}
