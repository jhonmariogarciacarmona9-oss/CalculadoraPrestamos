FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/calculadora-prestamos-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx300m", "-jar", "app.jar"]