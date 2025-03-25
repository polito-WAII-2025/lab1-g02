FROM gradle:8.10-jdk23 AS build

WORKDIR /app


# Copy only the necessary files to resolve the dependencies (optimize cache)
COPY build.gradle.kts settings.gradle.kts  ./
COPY gradle ./gradle

# Download the dependenciew, w/o copying the sources (maximize the code)
RUN gradle dependencies --no-daemon

# Now copy the rest of the source files
COPY src ./src

# compile the Kotlin app
#CMD ["./gradlew", "shadowJar"]
RUN gradle build --no-daemon

# command to start the container
ENTRYPOINT ["java", "-jar", "build/libs/RouteAnalyzer-1.0-SNAPSHOT-all.jar"]
