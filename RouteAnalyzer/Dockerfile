FROM gradle:8.10-jdk23 AS build

WORKDIR /app

COPY . .

# Compila l'applicazione Kotlin
#CMD ["./gradlew", "shadowJar"]
RUN gradle build --no-daemon

# Comando di avvio del container
ENTRYPOINT ["java", "-jar", "build/libs/RouteAnalyzer-1.0-SNAPSHOT-all.jar"]
