# ---------- BUILD STAGE ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copier les fichiers Maven en premier (meilleur cache)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

# Télécharger les dépendances (cache)
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copier le code et builder
COPY src src
RUN ./mvnw -q -DskipTests clean package

# ---------- RUN STAGE ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copier le jar
COPY --from=build /app/target/*.jar app.jar

# Render fournit PORT
ENV PORT=8080
EXPOSE 8080

# Lancer l'app sur le port Render
ENTRYPOINT ["sh","-c","java -jar app.jar --server.port=${PORT}"]