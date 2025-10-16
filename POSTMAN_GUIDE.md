# 📮 Guía Rápida de Postman

## Importar la Colección

1. Abre **Postman**
2. Click en **"Import"** (botón en la esquina superior izquierda)
3. Arrastra el archivo `postman_collection.json` o haz click para seleccionarlo
4. La colección aparecerá en tu workspace como: **"Taller 5 - Sistema de Ventas con Notificaciones"**

## Orden de Ejecución

### ✅ Paso 1: Health Checks (Opcional)
Verifica que todos los servicios estén activos:
- Health Check - Aggregator
- Health Check - Inventory  
- Health Check - Payments

**Resultado esperado**: Status `200 OK` con `"status": "UP"`

---

### 📦 Paso 2: Consultar Productos
**Petición**: `2. Consultar Productos`

**Qué hace**: Obtiene la lista de productos con sus proveedores asociados

**Resultado esperado**: Status `200 OK` con array JSON de productos
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "supplier": {
      "id": 1,
      "name": "TechSupplier Inc",
      "email": "orders@techsupplier.com"
    }
  }
]
```

---

### 💳 Paso 3: Crear Pago (OPCIONAL)
**Petición**: `3. Crear Pago (Opcional)`

**Qué hace**: Crea un pago y **guarda automáticamente** el `paymentId` en una variable

**JSON requerido**:
```json
{
  "customerId": 123,
  "customerEmail": "cliente@example.com",
  "amount": 2660.00
}
```

**Campos**:
- `customerId`: ID del cliente que realiza el pago
- `customerEmail`: Email del cliente para la notificación
- `amount`: Monto total del pago

**Resultado esperado**: Status `200 OK` con el pago creado
```json
{
  "id": 1,
  "customerId": 123,
  "customerEmail": "cliente@example.com",
  "amount": 2660.00,
  "status": "COMPLETED"
}
```

**💡 Importante**: Esta petición incluye un script en la pestaña "Tests" que guarda el `id` en la variable `{{paymentId}}` automáticamente.

---

### 🚀 Paso 4: Crear Factura (DISPARA NOTIFICACIONES)

Elige una de estas dos opciones:

#### Opción A: Sin paymentId (Más Simple) ✨
**Petición**: `4A. Crear Factura - SIN paymentId (Simple)`

**Qué hace**: Crea la factura directamente sin requerir pago previo

**Cuándo usar**: Para pruebas rápidas sin necesidad de crear un pago primero

---

#### Opción B: Con paymentId (Flujo Completo) 🔗
**Petición**: `4B. Crear Factura - CON paymentId (Completo)`

**Qué hace**: Crea la factura asociada al pago del Paso 3 usando `{{paymentId}}`

**Cuándo usar**: Para simular el flujo completo: Pago → Factura

**Pre-requisito**: Debes ejecutar primero el Paso 3 (Crear Pago)

---

### 📊 Resultado del Paso 4

**Status esperado**: `200 OK`

**Respuesta**:
```json
{
  "id": 1,
  "customerId": 123,
  "amount": 2660.00,
  "createdAt": "2025-10-15T20:00:00"
}
```

**¡AUTOMÁTICAMENTE SE DISPARAN LAS NOTIFICACIONES!** 🎉

---

## Ver las Notificaciones

Después de crear la factura (Paso 4), abre dos terminales PowerShell:

### Terminal 1: Notificaciones a Proveedores (Kafka) 📋
```powershell
docker logs supplier-listener -f
```

Verás notificaciones como:
```
🔔 NOTIFICACIÓN RECIBIDA PARA PROVEEDOR
===============================================
Proveedor: TechSupplier Inc (orders@techsupplier.com)
Factura ID: 1
Total para este proveedor: $2500.00
...
```

---

### Terminal 2: Notificación al Cliente (JMS/MDB) 📧
```powershell
docker logs notification-mdb -f
```

Verás la simulación de email:
```
📧 SIMULACIÓN DE ENVÍO DE EMAIL
===============================================
TO: cliente@example.com
SUBJECT: Confirmación de tu compra - Factura #1
...
```

---

## Variables de Entorno en Postman (Opcional)

Si quieres automatizar más el flujo, crea un **Environment** en Postman:

1. Click en el icono de engranaje (⚙️) → **"Environments"**
2. Click en **"Add"**
3. Nombre del environment: `Taller5 Local`
4. Agrega estas variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `paymentId` | 1 | 1 |
| `invoiceId` | 1 | 1 |

5. **Activa el environment** en el dropdown de la esquina superior derecha

**Ventaja**: El script de "Crear Pago" actualizará automáticamente `{{paymentId}}` cada vez que lo ejecutes.

---

## Troubleshooting

### ❌ Error: "Could not get any response"
**Causa**: El servicio no está activo o aún está iniciando

**Solución**: 
1. Ejecuta: `docker-compose ps` para ver el estado
2. Espera 1-2 minutos si los contenedores acaban de iniciar
3. Ejecuta los Health Checks para verificar

---

### ❌ Error 500 al crear pago
**Causa**: Faltan campos obligatorios en el JSON

**Solución**:
1. Asegúrate de incluir los 3 campos obligatorios:
   - `customerId`: ID del cliente
   - `customerEmail`: Email del cliente
   - `amount`: Monto del pago
2. JSON correcto:
```json
{
  "customerId": 123,
  "customerEmail": "cliente@example.com",
  "amount": 2660.00
}
```

---

### ❌ Error 500 al crear factura
**Causa**: Los datos en el JSON no coinciden con los productos reales

**Solución**:
1. Ejecuta primero "Consultar Productos" (Paso 2)
2. Usa los `supplierId`, `supplierName` y `supplierEmail` reales de la respuesta
3. Copia exactamente los datos del proveedor

---

### ❌ No veo notificaciones en los logs
**Causa**: La petición falló o hay un error de configuración

**Solución**:
1. Verifica que la petición devolvió `200 OK`
2. Revisa los logs de billing-service: `docker logs billing-service`
3. Verifica que Kafka esté activo: `docker logs kafka`

---

## 🎯 Flujo Completo Recomendado

1. ✅ Importa la colección
2. 📦 Consulta productos (para ver qué hay disponible)
3. 💳 Crea un pago (opcional pero recomendado)
4. 🚀 Crea la factura (Opción B si hiciste pago, Opción A si no)
5. 📋 Abre terminal y ejecuta: `docker logs supplier-listener -f`
6. 📧 Abre otra terminal y ejecuta: `docker logs notification-mdb -f`
7. 🎉 Observa las notificaciones en tiempo real!

---

## 💡 Tips Pro

- **Guarda tus peticiones exitosas**: Click derecho → "Save as Example"
- **Usa la consola de Postman**: `View` → `Show Postman Console` para debug detallado
- **Organiza tu workspace**: Crea carpetas para agrupar peticiones relacionadas
- **Exporta tu colección**: Guarda tus cambios exportando la colección actualizada

---

## 📞 ¿Necesitas más ayuda?

Revisa el `README.md` completo para:
- Arquitectura del sistema
- Troubleshooting detallado
- Comandos de Docker
- Estructura del proyecto
