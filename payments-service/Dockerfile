# ---------- stage 1: build with Maven ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN mvn -B -f pom.xml dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

# ---------- stage 2: run with JRE ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 👇 instala curl para que el healthcheck funcione
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

# copia el jar
COPY --from=build /app/target/*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --server.port=${SERVER_PORT:-8080}"]
