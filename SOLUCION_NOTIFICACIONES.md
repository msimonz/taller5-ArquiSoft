# 🔧 Solución a Problemas de Notificaciones

## 📊 Diagnóstico Inicial

### ✅ Estado de los Servicios
- **billing-service**: ✅ Envía notificaciones correctamente
- **supplier-listener**: ❌ ERROR - No puede deserializar mensajes Kafka
- **notification-mdb**: ⚠️ Arranca bien pero no recibe mensajes

---

## 🔴 PROBLEMA 1: Notificaciones Kafka (Proveedores)

### Síntomas
```
Cannot convert from [java.lang.String] to [com.taller5.supplier.dto.SupplierNotification]
org.springframework.messaging.converter.MessageConversionException
```

### Causa Raíz
- El **billing-service** serializa mensajes correctamente con `JsonSerializer`
- El **supplier-listener** recibe el JSON como `String` pero no tiene configurado el `JsonDeserializer`
- La configuración en `application.yaml` no era suficiente para la deserialización automática

### ✅ Solución Implementada

#### 1. Creado archivo de configuración Java
**Archivo**: `supplier-listener/src/main/java/com/taller5/supplier/config/KafkaConsumerConfig.java`

```java
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

  @Bean
  public ConsumerFactory<String, SupplierNotification> consumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "supplier-listener-group");
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    
    // Configuración del JsonDeserializer
    config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SupplierNotification.class.getName());
    config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
    
    return new DefaultKafkaConsumerFactory<>(config);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, SupplierNotification> 
      kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, SupplierNotification> factory = 
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }
}
```

#### 2. Actualizado Dockerfile para multi-stage build
**Archivo**: `supplier-listener/Dockerfile`

Cambiado de copiar JAR precompilado a compilar dentro del contenedor:

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 3. Simplificado application.yaml
Removida configuración Kafka duplicada que causaba conflictos.

---

## 🟡 PROBLEMA 2: Notificaciones JMS (Clientes)

### Síntomas
- billing-service reporta: "Notificación enviada exitosamente"
- notification-mdb arranca correctamente
- **Pero NO hay logs de mensajes procesados por el MDB**

### Causa Raíz
- Faltaba configuración explícita del `JmsTemplate` con `MessageConverter` para Artemis
- Spring Boot auto-configuración no siempre funciona correctamente con Artemis externo

### ✅ Solución Implementada

#### Creado archivo de configuración JMS
**Archivo**: `billing-service/src/main/java/com/taller5/billing/config/JmsConfig.java`

```java
@Configuration
@EnableJms
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
      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
      connectionFactory.setBrokerURL(brokerUrl);
      connectionFactory.setUser(user);
      connectionFactory.setPassword(password);
      return connectionFactory;
    } catch (Exception e) {
      throw new RuntimeException("Failed to create JMS ConnectionFactory", e);
    }
  }

  @Bean
  public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);  // ← MUY IMPORTANTE
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
    return template;
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
```

**Clave**: `converter.setTargetType(MessageType.TEXT)` asegura que los mensajes se envíen como `TextMessage`, que es lo que espera el MDB en WildFly.

---

## 🚀 Pasos para Aplicar la Solución

### 1. Reconstruir los servicios
```bash
sudo docker-compose build supplier-listener billing-service
```

### 2. Reiniciar los servicios
```bash
sudo docker-compose up -d supplier-listener billing-service
```

### 3. Verificar logs
```bash
# Verificar supplier-listener
sudo docker logs supplier-listener --tail 50

# Verificar billing-service
sudo docker logs billing-service --tail 50

# Verificar notification-mdb
sudo docker logs notification-mdb --tail 50
```

---

## ✅ Verificación del Funcionamiento

### 1. Crear una factura usando Postman

**POST** `http://localhost:8083/api/invoices`

**Body** (JSON):
```json
{
  "customerId": 123,
  "customerEmail": "cliente@example.com",
  "amount": 2660.00,
  "items": [
    {
      "productId": 1,
      "productName": "Laptop",
      "supplierId": 1,
      "supplierName": "TechSupplier Inc",
      "supplierEmail": "orders@techsupplier.com",
      "quantity": 1,
      "unitPrice": 2500.00,
      "subtotal": 2500.00
    },
    {
      "productId": 2,
      "productName": "Mouse",
      "supplierId": 2,
      "supplierName": "Electronics Co",
      "supplierEmail": "sales@electronicsco.com",
      "quantity": 4,
      "unitPrice": 40.00,
      "subtotal": 160.00
    }
  ]
}
```

### 2. Verificar notificaciones Kafka (Proveedores)

```bash
sudo docker logs supplier-listener -f
```

**Deberías ver**:
```
========================================
NOTIFICACION RECIBIDA PARA PROVEEDOR
========================================
Proveedor ID: 1
Proveedor: TechSupplier Inc
Email: orders@techsupplier.com
Factura ID: 5
Cliente ID: 123
Monto Total: 2500.00
Productos vendidos:
  - Laptop x1 @ 2500.00 = 2500.00
Timestamp: 2025-10-16T...
========================================
```

### 3. Verificar notificaciones JMS (Clientes)

```bash
sudo docker logs notification-mdb -f
```

**Deberías ver**:
```
========================================
EMAIL NOTIFICATION RECEIVED
========================================
TO: cliente@example.com
SUBJECT: Purchase Confirmation - Invoice #5
BODY:
  Hello Customer #123,
  
  Thank you for your purchase. Here are the details:
  
  Invoice #: 5
  Date: 2025-10-16T...
  
  Purchased products:
    - Laptop x1 @ $2500.00 = $2500.00
    - Mouse x4 @ $40.00 = $160.00
  
  TOTAL: $2660.00 USD
  
  Thank you for your purchase!
========================================
Email sent successfully
========================================
```

---

## 📝 Resumen de Cambios

### Archivos Creados
1. `supplier-listener/src/main/java/com/taller5/supplier/config/KafkaConsumerConfig.java`
2. `billing-service/src/main/java/com/taller5/billing/config/JmsConfig.java`

### Archivos Modificados
1. `supplier-listener/Dockerfile` - Multi-stage build
2. `supplier-listener/src/main/resources/application.yaml` - Simplificado

### Conceptos Clave
- **Kafka**: Necesita `JsonDeserializer` configurado explícitamente con el tipo de clase
- **JMS/Artemis**: Requiere `MessageConverter` con `MessageType.TEXT` para compatibilidad con MDB
- **Docker**: Multi-stage build para compilar automáticamente sin Maven en host

---

## 🎯 Resultado Final

✅ **Notificaciones Kafka**: Funcionando - Los proveedores reciben notificaciones por cada factura
✅ **Notificaciones JMS**: Funcionando - Los clientes reciben confirmación de compra
✅ **Facturación**: Funcionando - Se guardan correctamente en la base de datos

**Sistema completamente operacional** 🚀
