package com.taller5.billing.config;

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
    m.setContextPath("com.taller5.billing.soap");
    return m;
  }

  // Exponer WSDL a partir del XSD
  @Bean(name = "billing")
  public DefaultWsdl11Definition defaultWsdl11DefinitionBilling(XsdSchema billingSchema, Jaxb2Marshaller marshaller) {
    DefaultWsdl11Definition def = new DefaultWsdl11Definition();
    def.setPortTypeName("BillingPort");
    def.setLocationUri("/ws/billing");
    def.setTargetNamespace("http://taller5.com/soap/billing");
    def.setSchema(billingSchema);
    return def;
  }

  @Bean
  public XsdSchema billingSchema() {
    return new SimpleXsdSchema(new ClassPathResource("billing.xsd"));
  }
}