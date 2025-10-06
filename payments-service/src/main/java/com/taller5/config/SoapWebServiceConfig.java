package com.taller5.config;

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

/**
 * Configuración SOAP unificada: registra el MessageDispatcherServlet una sola vez
 * y expone 3 WSDLs (billing, inventory, checkout).
 *
 * Borra las antiguas clases SoapWebServiceConfig que estaban en paquetes específicos.
 */
@EnableWs
@Configuration
public class SoapWebServiceConfig {

    // --- MessageDispatcherServlet (solo una vez) ---
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    // --- Marshallers (uno por dominio, nombres únicos) ---
    @Bean
    public Jaxb2Marshaller billingMarshaller() {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        m.setContextPath("com.taller5.billing.soap"); // ajustá si tu package es distinto
        return m;
    }

    @Bean
    public Jaxb2Marshaller inventoryMarshaller() {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        m.setContextPath("com.taller5.inventory.soap"); // ajustá si tu package es distinto
        return m;
    }

    @Bean
    public Jaxb2Marshaller checkoutMarshaller() {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        m.setContextPath("com.taller5.payments.soap"); // ajustá si tu package es distinto
        return m;
    }

    // --- Billing WSDL / XSD ---
    @Bean(name = "billing")
    public DefaultWsdl11Definition defaultWsdl11DefinitionBilling(XsdSchema billingSchema) {
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

    // --- Inventory WSDL / XSD ---
    @Bean(name = "inventory")
    public DefaultWsdl11Definition defaultWsdl11DefinitionInventory(XsdSchema inventorySchema) {
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

    // --- Checkout WSDL / XSD ---
    @Bean(name = "checkout")
    public DefaultWsdl11Definition defaultWsdl11DefinitionCheckout(XsdSchema checkoutSchema) {
        DefaultWsdl11Definition def = new DefaultWsdl11Definition();
        def.setPortTypeName("CheckoutPort");
        def.setLocationUri("/ws/checkout");
        def.setTargetNamespace("http://taller5.com/soap/checkout");
        def.setSchema(checkoutSchema);
        return def;
    }

    @Bean
    public XsdSchema checkoutSchema() {
        return new SimpleXsdSchema(new ClassPathResource("checkout.xsd"));
    }
}
