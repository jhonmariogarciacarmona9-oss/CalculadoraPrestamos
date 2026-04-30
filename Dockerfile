# 1. Usamos una imagen de Maven ligera para compilar
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY . .
# Forzamos la compilación limpia
RUN mvn clean package -DskipTests

# 2. Usamos una imagen de Java mínima para ejecutar
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# El asterisco ayuda a encontrar el archivo sin importar el nombre exacto
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Limitamos la memoria de Java para el plan gratuito de Render
ENTRYPOINT ["java", "-Xmx300m", "-Xss512k", "-jar", "app.jar"]