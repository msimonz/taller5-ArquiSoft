# Stage 1: Build WAR with Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Deploy to WildFly
FROM quay.io/wildfly/wildfly:31.0.0.Final-jdk17

USER root

# Copiar el WAR compilado al directorio de deployments de WildFly
COPY --from=build /app/target/notification-mdb.war /opt/jboss/wildfly/standalone/deployments/

# Copiar script de configuración de Artemis
COPY wildfly-artemis-config.cli /opt/jboss/wildfly/

# Usuario administrador de WildFly
RUN /opt/jboss/wildfly/bin/add-user.sh admin admin123 --silent

# Configurar Artemis para aceptar conexiones remotas
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/wildfly-artemis-config.cli

# Cambiar propietario
RUN chown -R jboss:jboss /opt/jboss/wildfly

USER jboss

# Exponer puertos
EXPOSE 8080 9990 61616

# Iniciar WildFly con configuración full (incluye Artemis JMS)
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-full.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
