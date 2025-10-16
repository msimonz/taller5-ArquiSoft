package com.taller5.billing.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import jakarta.jms.ConnectionFactory;

@Configuration
@EnableJms
@Slf4j
public class JmsConfig {

  @Value("${spring.artemis.broker-url}")
  private String brokerUrl;

  @Value("${spring.artemis.user:admin}")
  private String user;

  @Value("${spring.artemis.password:admin123}")
  private String password;

  @Bean
  public ConnectionFactory connectionFactory() {
    try {
      log.info("🚀 Creando ConnectionFactory con broker-url: {}", brokerUrl);
      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
      connectionFactory.setBrokerURL(brokerUrl);
      connectionFactory.setUser(user);
      connectionFactory.setPassword(password);
      log.info("✅ ConnectionFactory creado exitosamente");
      return connectionFactory;
    } catch (Exception e) {
      log.error("❌ Error creando ConnectionFactory", e);
      throw new RuntimeException("Failed to create JMS ConnectionFactory", e);
    }
  }

  @Bean
  public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);
    converter.setTypeIdPropertyName("_type");
    converter.setObjectMapper(objectMapper);
    return converter;
  }

  @Bean
  public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, 
                                  MessageConverter messageConverter) {
    JmsTemplate template = new JmsTemplate(connectionFactory);
    template.setMessageConverter(messageConverter);
    template.setPubSubDomain(false); // Usar colas, no topics
    // NO establecer defaultDestinationName - usaremos nombres explícitos
    return template;
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
