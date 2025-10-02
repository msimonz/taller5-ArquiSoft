# ---------- stage 1: build with Maven ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# copy pom and download dependencies (cache)
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN mvn -B -f pom.xml dependency:go-offline

# copy source and package
COPY src ./src
RUN mvn -B -DskipTests package

# ---------- stage 2: run with JRE ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# copy the jar from build stage (assumes standard spring-boot maven layout)
ARG JAR_FILE=$(ls /app/target/*.jar | head -n 1)
# The above ARG attempt may not work in all build contexts; instead copy by known file name:
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --server.port=${SERVER_PORT:-8080}"]
