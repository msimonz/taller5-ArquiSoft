#!/bin/bash

echo "Limpiando transacciones XA corruptas y reiniciando el sistema..."

# Detener el sistema
echo "Deteniendo servicios..."
docker compose down

# Limpiar volúmenes de base de datos (esto eliminará todas las transacciones XA corruptas)
echo "Limpiando volúmenes de base de datos..."
docker volume prune -f

# Opcional: Eliminar imágenes para rebuild completo
echo "Eliminando imágenes para rebuild completo..."
docker compose build --no-cache

# Reiniciar el sistema
echo "Reiniciando el sistema..."
docker compose up -d

echo "Sistema reiniciado. Esperando a que los servicios estén listos..."
sleep 30

echo "Verificando estado de los contenedores..."
docker compose ps

echo "Mostrando logs de payments-service..."
docker compose logs payments-service --tail=50