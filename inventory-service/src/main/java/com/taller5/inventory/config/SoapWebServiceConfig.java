package com.taller5.inventory.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@EnableWs
@Configuration
public class SoapWebServiceConfig {

  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(context);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, "/ws/*");
  }

  // marshaller — package: el package donde estarán tus clases JAXB (ej: com.taller5.payments.soap)
  @Bean
  public Jaxb2Marshaller jaxb2Marshaller() {
    Jaxb2Marshaller m = new Jaxb2Marshaller();
    m.setContextPath("com.taller5.inventory.soap");
    return m;
  }

  // Exponer WSDL a partir del XSD
  @Bean(name = "inventory")
  public DefaultWsdl11Definition defaultWsdl11DefinitionInventory(XsdSchema inventorySchema, Jaxb2Marshaller marshaller) {
    DefaultWsdl11Definition def = new DefaultWsdl11Definition();
    def.setPortTypeName("InventoryPort");
    def.setLocationUri("/ws/inventory");
    def.setTargetNamespace("http://taller5.com/soap/inventory");
    def.setSchema(inventorySchema);
    return def;
  }

  @Bean
  public XsdSchema inventorySchema() {
    return new SimpleXsdSchema(new ClassPathResource("inventory.xsd"));
  }
}
