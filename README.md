# Sistema de Ventas con Notificaciones

Taller de Arquitectura de Software - Sistema de ventas con notificaciones a proveedores (Kafka) y clientes (JMS/MDB)

## Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          FLUJO DE NOTIFICACIONES                         │
└─────────────────────────────────────────────────────────────────────────┘

     Cliente HTTP Request
            │
            ↓
    ┌───────────────┐
    │   Inventory   │ ← Consultar productos y proveedores
    │   Service     │
    └───────────────┘
            │
            ↓
    ┌───────────────┐
    │   Payments    │ ← (Opcional) Procesar pago
    │   Service     │
    └───────────────┘
            │
            ↓
    ┌───────────────────────────────────────────────────────────────┐
    │              BILLING SERVICE (Orquestador)                    │
    │  • Crea factura en BD                                         │
    │  • Agrupa items por proveedor                                 │
    │  • Dispara notificaciones                                     │
    └────────────┬─────────────────────────────────────┬────────────┘
                 │                                     │
                 │                                     │
        ┌────────▼──────────┐              ┌──────────▼─────────┐
        │  KAFKA PRODUCER   │              │   JMS PRODUCER     │
        │  (Spring Kafka)   │              │  (Spring JMS)      │
        └────────┬──────────┘              └──────────┬─────────┘
                 │                                     │
                 │ JSON                                │ JSON
                 │                                     │
        ┌────────▼──────────┐              ┌──────────▼─────────┐
        │   KAFKA BROKER    │              │  ARTEMIS JMS       │
        │  Topics dinámicos │              │  (WildFly)         │
        │  supplier-1       │              │  Queue:            │
        │  supplier-2       │              │  customer-         │
        │  supplier-N       │              │  notifications     │
        └────────┬──────────┘              └──────────┬─────────┘
                 │                                     │
                 │ Pattern: supplier-.*                │
                 │                                     │
        ┌────────▼──────────┐              ┌──────────▼─────────┐
        │ SUPPLIER-LISTENER │              │ NOTIFICATION-MDB   │
        │ (Kafka Consumer)  │              │ (Message Driven    │
        │ Spring Boot       │              │  Bean - Jakarta EE)│
        └────────┬──────────┘              └──────────┬─────────┘
                 │                                     │
                 ↓                                     ↓
        📋 Log Notificación                   📧 Simula Email
        a Proveedores                         al Cliente


┌─────────────────────────────────────────────────────────────────────────┐
│                        CAPA DE DATOS (Aggregator)                        │
├──────────────────┬─────────────────┬────────────────┬──────────────────┤
│  MySQL Inventory │ MySQL Billing   │ MySQL Payments │                  │
│  • Products      │ • Invoices      │ • Payments     │  Aggregator DAL  │
│  • Suppliers     │ • InvoiceItems  │                │  (Spring Boot)   │
└──────────────────┴─────────────────┴────────────────┴──────────────────┘
```

### Flujo de Mensajería

1. **Billing Service** recibe petición de crear factura con items
2. **Agrupación**: Agrupa items por `supplierId`
3. **Notificación a Proveedores (Kafka)**:
   - Por cada proveedor → Publica mensaje en topic `supplier-{id}`
   - `supplier-listener` consume mensajes de todos los topics `supplier-.*`
   - Registra la notificación en logs
4. **Notificación a Cliente (JMS)**:
   - Publica UN mensaje a la cola `customer-notifications`
   - MDB en WildFly consume el mensaje
   - Simula envío de email al cliente

## Tecnologías

- **Spring Boot 3.4.0**: Microservicios
- **MySQL 8.0**: 3 bases de datos (inventory, billing, payments)
- **Kafka 7.5.0**: Mensajería punto a punto para proveedores
- **WildFly 31 con Artemis integrado**: Servidor Jakarta EE con MDB y JMS Queue para notificaciones a clientes
- **Docker Compose**: Orquestación de contenedores
- **Maven 3.9+**: Gestión de dependencias y compilación

## Estructura de Servicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| aggregator-service | 8090 | DAL: Acceso a datos de las 3 BDs |
| payments-service | 8082 | Gestión de pagos |
| inventory-service | 8081 | Gestión de inventario |
| billing-service | 8083 | Facturación y envío de notificaciones |
| supplier-listener | 8084 | Consumer Kafka (notif. proveedores) |
| notification-mdb | 8085 | MDB en WildFly (notif. clientes) |
| mysql_inventory | 3307 | BD Inventario y Proveedores |
| mysql_billing | 3308 | BD Facturas |
| mysql_payments | 3309 | BD Pagos |
| kafka | 9092/29092 | Broker Kafka (interno: 9092, externo: 29092) |
| zookeeper | 2181 | Coordinador de Kafka |
| wildfly artemis | 61616 | JMS Artemis integrado en WildFly |
| wildfly console | 9990 | Consola administración WildFly |

## Pre-requisitos

- Docker y Docker Compose
- Maven 3.9+
- Java 17+
- **Postman** (para realizar las peticiones HTTP a los servicios)

## Construcción y Despliegue

### Opción 1: Despliegue Rápido (Recomendado)

Si los servicios ya están compilados previamente, simplemente ejecuta:

```powershell
docker-compose up --build -d
```

Docker Compose construirá automáticamente todos los servicios desde sus Dockerfiles. Este proceso puede tardar 3-5 minutos la primera vez.

### Opción 2: Compilación Manual

Si prefieres compilar manualmente antes de desplegar:

#### 1. Compilar servicios Spring Boot

```powershell
# Aggregator Service
cd aggregator-service
mvn clean package -DskipTests
cd ..

# Payments Service
cd payments-service
mvn clean package -DskipTests
cd ..

# Inventory Service
cd inventory-service
mvn clean package -DskipTests
cd ..

# Billing Service
cd billing-service
mvn clean package -DskipTests
cd ..

# Supplier Listener
cd supplier-listener
mvn clean package -DskipTests
cd ..
```

#### 2. Compilar el MDB (WildFly)

```powershell
cd notification-mdb
mvn clean package -DskipTests
cd ..
```

#### 3. Levantar con Docker Compose

```powershell
docker-compose up -d
```

**Nota**: El flag `-d` (detached) ejecuta los contenedores en segundo plano.

### Verificar Estado de los Contenedores

```powershell
docker-compose ps
```

Todos los servicios deben mostrar estado "running" o "healthy".

## Prueba del Sistema

> **💡 Nota**: Todas las peticiones HTTP se realizan usando **Postman**. A continuación se detallan los endpoints con su configuración completa.

### Verificar servicios activos

#### Health Checks (GET en Postman):
- **Aggregator**: `http://localhost:8090/actuator/health`
- **Inventory**: `http://localhost:8081/actuator/health`
- **Payments**: `http://localhost:8082/actuator/health`

#### Verificar estado de contenedores (PowerShell):
```powershell
docker-compose ps
```

#### Consola WildFly (Navegador):
- URL: `http://localhost:9990`
- Usuario: `admin`
- Password: `admin123`

### Ver logs de servicios

```powershell
# Logs de Kafka consumer (notificaciones proveedores)
docker logs supplier-listener -f

# Logs de MDB (notificaciones clientes)
docker logs notification-mdb -f

# Logs de billing service
docker logs billing-service -f

# Logs de Kafka
docker logs kafka -f
```

### 📮 Colección de Postman Lista para Importar

**¡Importa la colección completa en un solo click!**

📁 **Archivos incluidos**:
- `postman_collection.json` - Colección completa con todas las peticiones
- `POSTMAN_GUIDE.md` - **Guía detallada paso a paso para usar Postman** 📘

**Cómo importar:**
1. Abre Postman
2. Click en **"Import"** (esquina superior izquierda)
3. Selecciona el archivo `postman_collection.json`
4. La colección **"Taller 5 - Sistema de Ventas con Notificaciones"** aparecerá en tu workspace

> **💡 ¿Primera vez usando la colección?** Lee `POSTMAN_GUIDE.md` para instrucciones detalladas y tips

**La colección incluye:**
1. ✅ **Health Checks** - Verificar que todos los servicios estén activos
2. 📦 **Consultar Productos** - Ver productos con proveedores
3. 💳 **Crear Pago (Opcional)** - Simular un pago (guarda automáticamente el `paymentId`)
4. 📄 **Crear Factura - Opción A** - Sin paymentId (más simple)
5. 📄 **Crear Factura - Opción B** - Con paymentId (usa variable automática)

**Tips para Postman:**
- La petición de crear pago **guarda automáticamente** el `paymentId` en una variable de entorno
- La opción B de crear factura usa `{{paymentId}}` para referenciar el pago previo
- Todas las peticiones tienen descripciones detalladas
- Activa la opción **"Save Responses"** para revisar el historial

### Flujo de Prueba Completo

#### 1. Consultar productos disponibles 📦

**Configuración en Postman:**

| Campo | Valor |
|-------|-------|
| **Método** | `GET` |
| **URL** | `http://localhost:8081/api/products` |
| **Headers** | No requeridos |
| **Body** | No requerido |

**Pasos:**
1. Selecciona método `GET`
2. Copia y pega la URL
3. Click en **"Send"**

**Respuesta esperada** (Status 200):
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "price": 2500.00,
    "stock": 10,
    "supplier": {
      "id": 1,
      "name": "TechSupplier Inc",
      "email": "orders@techsupplier.com"
    }
  },
  {
    "id": 2,
    "name": "Mouse",
    "price": 40.00,
    "stock": 100,
    "supplier": {
      "id": 2,
      "name": "Electronics Co",
      "email": "sales@electronicsco.com"
    }
  },
  {
    "id": 3,
    "name": "Teclado",
    "price": 80.00,
    "stock": 50,
    "supplier": {
      "id": 2,
      "name": "Electronics Co",
      "email": "sales@electronicsco.com"
    }
  }
]
```

#### 2. (Opcional) Crear un pago 💳

Este paso es opcional si quieres simular el flujo completo con payments-service.

**Configuración en Postman:**

| Campo | Valor |
|-------|-------|
| **Método** | `POST` |
| **URL** | `http://localhost:8082/api/payments/charge` |
| **Headers** | `Content-Type: application/json` |
| **Body** | raw → JSON |

**Pasos:**
1. Selecciona método `POST`
2. Copia y pega la URL
3. Ve a la pestaña **"Headers"**, agrega: `Content-Type` = `application/json`
4. Ve a la pestaña **"Body"**, selecciona **"raw"** y **"JSON"**
5. Copia el siguiente JSON:
```json
{
  "customerId": 123,
  "customerEmail": "cliente@example.com",
  "amount": 2660.00
}
```

**Respuesta esperada** (Status 200):
```json
{
  "id": 1,
  "customerId": 123,
  "customerEmail": "cliente@example.com",
  "amount": 2660.00,
  "status": "COMPLETED"
}
```

**Nota**: Guarda el `id` (paymentId) de la respuesta para usarlo en el siguiente paso.

#### 3. Crear la factura y disparar notificaciones (PASO PRINCIPAL) 🚀

Este es el paso que dispara el sistema de notificaciones. El `billing-service` automáticamente:
- ✅ Crea la factura en la BD
- ✅ Agrupa items por proveedor
- ✅ Envía notificaciones a Kafka (uno por cada proveedor)
- ✅ Envía notificación JMS al cliente

**Configuración en Postman:**

| Campo | Valor |
|-------|-------|
| **Método** | `POST` |
| **URL** | `http://localhost:8083/api/invoices` |
| **Headers** | `Content-Type: application/json` |
| **Body** | raw → JSON |

**Pasos:**
1. Selecciona método `POST`
2. Copia y pega la URL
3. Ve a la pestaña **"Headers"**, agrega: `Content-Type` = `application/json`
4. Ve a la pestaña **"Body"**, selecciona **"raw"** y **"JSON"**
5. Copia uno de los siguientes JSON (Opción A o B)

---

**Opción A: Sin paymentId previo (más simple)**

**Body** (raw - JSON):
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

---

**Opción B: Con paymentId (flujo completo)**

Si creaste un pago en el paso 2, usa el `id` recibido como `paymentId`:

**Body** (raw - JSON):
```json
{
  "paymentId": 1,
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

**Respuesta esperada** (Status 200):
```json
{
  "id": 1,
  "customerId": 123,
  "amount": 2660.00,
  "createdAt": "2025-10-15T20:00:00"
}
```

**✨ ¡Al enviar esta petición, automáticamente se disparan las notificaciones!**

#### 4. Ver notificaciones en logs (VERIFICACIÓN)

**IMPORTANTE**: Abre dos terminales PowerShell separadas para ver ambos tipos de notificaciones simultáneamente.

**Terminal 1 - Notificaciones a Proveedores (Kafka):**
```powershell
docker logs supplier-listener -f
```

Verás logs como:
```
🔔 NOTIFICACIÓN RECIBIDA PARA PROVEEDOR
===============================================
Proveedor: TechSupplier Inc (tech@supplier.com)
Factura ID: 1
Total para este proveedor: $2500.00

Productos vendidos:
  - Laptop x1 @ $2500.00 = $2500.00

Mensaje consumido desde topic: supplier-1
===============================================
```

**Se creará un topic de Kafka por cada proveedor**: `supplier-1`, `supplier-2`, etc.

---

**Terminal 2 - Notificaciones a Clientes (JMS/MDB en WildFly):**
```powershell
docker logs notification-mdb -f
```

Verás logs como:
```
📧 SIMULACIÓN DE ENVÍO DE EMAIL
===============================================
TO: cliente@example.com
SUBJECT: Confirmación de tu compra - Factura #1
BODY:
-----------------------------------------------
Hola Cliente #123,

Gracias por tu compra. Tu factura #1 ha sido generada exitosamente.

Detalles de tu compra:
  • Laptop - Cantidad: 1 - Precio: $2500.00
  • Mouse - Cantidad: 4 - Precio: $40.00

Total: $2660.00

Recibirás tu pedido en los próximos días.

Saludos,
Sistema de Ventas
-----------------------------------------------
Mensaje procesado por MDB: CustomerNotificationMDB
===============================================
```

### Troubleshooting de Notificaciones

Si no ves las notificaciones después de enviar la petición desde Postman:

1. **Verificar que billing-service se conectó correctamente:**
```powershell
docker logs billing-service | Select-String "Kafka"
docker logs billing-service | Select-String "JMS"
```

2. **Verificar topics de Kafka creados:**
```powershell
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

Deberías ver topics como `supplier-1`, `supplier-2`, etc.

3. **Verificar conexión de WildFly a Artemis:**
```powershell
docker logs notification-mdb | Select-String "Artemis"
```

4. **Verificar que la petición en Postman fue exitosa:**
   - Status code debe ser `200 OK`
   - Debe devolver un JSON con el ID de la factura creada
   - Revisa la pestaña "Console" de Postman para ver detalles de la petición

## Arquitectura de Mensajería

### Kafka (Notificaciones a Proveedores)
- **Topics dinámicos**: `supplier-{supplierId}` (se crean automáticamente)
- **Patrón**: Point-to-Point (cada proveedor tiene su topic exclusivo)
- **Producer**: `billing-service` (KafkaSupplierNotificationService)
- **Consumer**: `supplier-listener` (subscrito al patrón `supplier-.*`)
- **Serialización**: JSON con Jackson
- **Configuración**: 
  - Bootstrap servers: `kafka:9092`
  - Group ID: `supplier-listener-group`
  - Auto-offset: `earliest`

**Ejemplo de mensaje Kafka:**
```json
{
  "supplierId": 1,
  "supplierName": "TechSupplier Inc",
  "supplierEmail": "tech@supplier.com",
  "invoiceId": 1,
  "totalAmount": 2500.00,
  "products": [
    {
      "productId": 1,
      "productName": "Laptop",
      "quantity": 1,
      "unitPrice": 2500.00,
      "subtotal": 2500.00
    }
  ]
}
```

### JMS/Artemis (Notificaciones a Clientes)
- **Cola**: `customer-notifications`
- **Broker**: Artemis JMS integrado en WildFly (puerto 61616)
- **Producer**: `billing-service` (JmsCustomerNotificationService con Spring JMS)
- **Consumer**: `CustomerNotificationMDB` en WildFly
- **MDB**: Message Driven Bean que simula envío de email
- **Serialización**: JSON con Jackson (deserializado en el MDB)

**Ejemplo de mensaje JMS:**
```json
{
  "invoiceId": 1,
  "customerId": 123,
  "customerEmail": "cliente@example.com",
  "totalAmount": 2660.00,
  "purchasedItems": [
    {
      "productName": "Laptop",
      "quantity": 1,
      "price": 2500.00
    },
    {
      "productName": "Mouse",
      "quantity": 4,
      "price": 40.00
    }
  ]
}
```

**¿Por qué no se usa ActiveMQ externo?**
WildFly 31 incluye Artemis JMS integrado de forma nativa, eliminando la necesidad de un contenedor ActiveMQ separado. Esto simplifica la arquitectura y reduce el overhead de infraestructura.

## Diagrama de Flujo

```
1. Cliente envía checkout con items
   ↓
2. payments-service registra el pago
   ↓
3. billing-service crea factura con items
   ↓
4. billing-service agrupa items por proveedor
   ↓
5. Por cada proveedor:
      → Envía mensaje a Kafka topic "supplier-{id}"
      → supplier-listener consume y registra
   ↓
6. Para el cliente:
      → Envía mensaje a cola JMS "customer-notifications"
      → MDB en WildFly consume y simula envío de email
```

## Troubleshooting

### 🔧 Problemas Comunes

#### 1. Supplier-listener en restart constante

**Síntoma**: `docker ps` muestra el contenedor reiniciándose continuamente.

**Causa**: Error de configuración de Kafka o JAR mal compilado.

**Solución**:
```powershell
# Ver logs para identificar el error
docker logs supplier-listener

# Si hay ClassNotFoundException, recompilar:
cd supplier-listener
mvn clean package -DskipTests
cd ..
docker-compose up -d --build supplier-listener
```

#### 2. Kafka no crea topics automáticamente

**Verificar topics existentes:**
```powershell
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

**Crear topic manualmente (si es necesario):**
```powershell
docker exec kafka kafka-topics --create `
  --topic supplier-1 `
  --bootstrap-server localhost:9092 `
  --partitions 1 `
  --replication-factor 1
```

#### 3. MDB no consume mensajes de JMS

**Verificar logs de WildFly:**
```powershell
docker logs notification-mdb | Select-String "MDB"
docker logs notification-mdb | Select-String "customer-notifications"
```

**Verificar que WildFly está usando standalone-full.xml:**
```powershell
docker logs notification-mdb | Select-String "standalone-full"
```

Si no está usando `standalone-full.xml`, el Artemis JMS no estará disponible.

#### 4. Billing-service no se conecta a Kafka o JMS

**Verificar configuración:**
```powershell
docker logs billing-service | Select-String "Kafka"
docker logs billing-service | Select-String "JMS"
```

**Verificar conectividad:**
```powershell
# Probar desde el contenedor
docker exec billing-service ping kafka -c 2
docker exec billing-service ping notification-mdb -c 2
```

#### 5. Servicios no se conectan entre sí

**Verificar que todos los health checks pasen:**
```powershell
docker-compose ps
```

Todos los servicios deben mostrar estado "Up" o "healthy".

**Verificar red de Docker:**
```powershell
docker network ls
docker network inspect taller5-arquisoft_default
```

#### 6. Error "Connection refused" al hacer curl

**Causa**: Los servicios aún están iniciando.

**Solución**: Espera 1-2 minutos después de `docker-compose up` antes de hacer peticiones.

```powershell
# Ver progreso de inicio
docker-compose logs -f
```

#### 7. MySQL no está listo

**Síntoma**: Servicios muestran errores de conexión a BD.

**Solución**: Los health checks de MySQL tardan ~10 segundos. Espera a que docker-compose muestre "healthy" para los contenedores MySQL.

```powershell
# Verificar estado específico de MySQL
docker-compose ps | Select-String "mysql"
```

## Comandos Útiles

> **💡 Nota**: Para las peticiones HTTP, usa **Postman** con la colección incluida (`postman_collection.json`). Los comandos a continuación son para gestión de Docker y diagnóstico del sistema.

### Gestión de Contenedores

```powershell
# Levantar todos los servicios
docker-compose up -d

# Detener todos los servicios
docker-compose stop

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker logs <nombre-servicio> -f

# Reiniciar un servicio específico
docker-compose restart <nombre-servicio>

# Reconstruir y levantar un servicio específico
docker-compose up -d --build <nombre-servicio>

# Ver estado de los contenedores
docker-compose ps

# Verificar uso de recursos
docker stats
```

### Limpieza del Sistema

```powershell
# Detener y eliminar contenedores (mantiene volúmenes)
docker-compose down

# Detener y eliminar contenedores Y volúmenes (limpieza completa)
docker-compose down -v

# Eliminar también imágenes construidas
docker-compose down -v --rmi all

# Limpiar todo el sistema Docker (¡CUIDADO!)
docker system prune -a --volumes
```

### Acceso a Bases de Datos

```powershell
# Conectarse a MySQL Inventory
docker exec -it mysql_inventory mysql -u simis -p
# Password: 123456

# Conectarse a MySQL Billing
docker exec -it mysql_billing mysql -u simis -p

# Conectarse a MySQL Payments
docker exec -it mysql_payments mysql -u simis -p
```

### Inspección de Kafka

```powershell
# Listar todos los topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Ver detalles de un topic
docker exec kafka kafka-topics --describe --topic supplier-1 --bootstrap-server localhost:9092

# Consumir mensajes de un topic (para debugging)
docker exec kafka kafka-console-consumer --topic supplier-1 --from-beginning --bootstrap-server localhost:9092

# Ver grupos de consumidores
docker exec kafka kafka-consumer-groups --list --bootstrap-server localhost:9092
```

## Estructura del Proyecto

```
taller5-ArquiSoft/
├── aggregator-service/          # DAL - Acceso a datos
│   ├── src/main/java/
│   │   └── com/taller5/aggregator/
│   │       ├── dal/             # Servicios de acceso a datos
│   │       └── dto/             # DTOs de transferencia
│   └── pom.xml
├── billing-service/             # Facturación y notificaciones
│   ├── src/main/java/
│   │   └── com/taller5/billing/
│   │       ├── controller/      # REST endpoints
│   │       ├── service/         # Lógica de notificaciones
│   │       └── config/          # Kafka y JMS config
│   └── pom.xml
├── inventory-service/           # Gestión de inventario
├── payments-service/            # Procesamiento de pagos
├── supplier-listener/           # Consumer Kafka
│   ├── src/main/java/
│   │   └── com/taller5/supplier/
│   │       ├── listener/        # Kafka listeners
│   │       └── dto/             # DTOs de mensajes
│   └── pom.xml
├── notification-mdb/            # MDB para JMS
│   ├── src/main/java/
│   │   └── com/taller5/notification/
│   │       ├── mdb/             # Message Driven Beans
│   │       └── dto/             # DTOs serializables
│   └── pom.xml
├── db/                          # Scripts SQL
│   ├── db_inventory.sql
│   ├── db_billing.sql
│   └── db_payments.sql
└── docker-compose.yml           # Orquestación completa
```

## Notas Importantes

⚠️ **Puertos Utilizados**: Asegúrate de que los siguientes puertos estén libres antes de levantar el sistema:
- 3307, 3308, 3309 (MySQL)
- 8081, 8082, 8083, 8084, 8085, 8090 (Servicios Spring Boot)
- 9092, 29092 (Kafka)
- 2181 (Zookeeper)
- 9990 (WildFly Admin)
- 61616 (Artemis JMS)

🔒 **Credenciales por defecto**:
- MySQL: usuario `simis`, password `123456`
- WildFly Admin: usuario `admin`, password `admin123`

🚀 **Recomendaciones**:
1. Usa `-d` (detached) para ejecutar contenedores en segundo plano
2. Revisa logs regularmente con `docker logs -f` para debugging
3. Usa `docker-compose ps` para verificar el estado de los servicios
4. Si cambias código, recuerda recompilar con Maven antes de `docker-compose up --build`

