# Guía Rápida de Pruebas

## 1. Levantar la Aplicación

```bash
sudo docker-compose up -d
```

Esperar ~30 segundos para que todos los servicios inicien correctamente.

## 2. Crear un Pago

```bash
curl -X POST http://localhost:8082/api/payments/charge \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 100,
    "customerEmail": "cliente@test.com",
    "amount": 5000.00
  }'
```

**Respuesta esperada:**
```json
{"id":1,"customerId":100,"productId":null,"amount":5000.00}
```

## 3. Crear una Factura

```bash
curl -X POST http://localhost:8083/api/invoices \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": 1,
    "customerId": 100,
    "customerEmail": "cliente@test.com",
    "amount": 5000.00,
    "items": [
      {
        "productId": 1,
        "productName": "Laptop",
        "supplierId": 1,
        "supplierName": "TechSupplier Inc",
        "quantity": 1,
        "unitPrice": 3000.00,
        "subtotal": 3000.00
      },
      {
        "productId": 2,
        "productName": "Mouse",
        "supplierId": 2,
        "supplierName": "Electronics Co",
        "quantity": 2,
        "unitPrice": 1000.00,
        "subtotal": 2000.00
      }
    ]
  }'
```

**Respuesta esperada:**
```json
{"invoiceId":1,"invoice":{...}}
```

## 4. Verificar Notificaciones

### Kafka - Notificaciones a Proveedores

```bash
sudo docker logs supplier-listener --tail 50
```

**Deberías ver:**
```
========================================
NOTIFICACION RECIBIDA PARA PROVEEDOR
========================================
Proveedor: TechSupplier Inc
Email: TechSupplier Inc@supplier.com
Factura ID: 1
Productos vendidos:
  - Laptop x1 @ 3000.00 = 3000.00
========================================

========================================
NOTIFICACION RECIBIDA PARA PROVEEDOR
========================================
Proveedor: Electronics Co
Email: Electronics Co@supplier.com
Factura ID: 1
Productos vendidos:
  - Mouse x2 @ 1000.00 = 2000.00
========================================
```

### JMS/WildFly - Notificaciones a Clientes

```bash
sudo docker logs notification-mdb --tail 50
```

**Deberías ver:**
```
========================================
EMAIL NOTIFICATION RECEIVED
========================================
TO: cliente@test.com
SUBJECT: Purchase Confirmation - Invoice #1
BODY:
  Hello Customer #100,

  Thank you for your purchase. Here are the details:

  Invoice #: 1
  
  Purchased products:
    - Laptop x1 @ $3000.00 = $3000.00
    - Mouse x2 @ $1000.00 = $2000.00

  TOTAL: $5000.00 USD

  Thank you for your purchase!
========================================
Email sent successfully
========================================
```

## 5. Detener la Aplicación

```bash
sudo docker-compose down
```

---

## Resumen de Servicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| payments-service | 8082 | Gestión de pagos |
| billing-service | 8083 | Generación de facturas |
| notification-mdb | 8085 | MDB para notificaciones JMS |
| supplier-listener | 8084 | Listener Kafka para proveedores |
| Kafka | 9092 | Message broker (proveedores) |
| WildFly Artemis | 61616 | JMS broker (clientes) |

## Notas Importantes

- **Kafka**: Envía notificaciones a proveedores por cada producto en la factura
- **JMS**: Envía UNA notificación al cliente con todos los productos
- Los IDs de `paymentId` en la factura deben coincidir con el ID del pago creado
- Cada proveedor recibe solo la información de SUS productos
