# First stage: build the application
FROM gradle:7.5.1-jdk17 AS build

# Set the working directory
WORKDIR /app

# Copy the build files
COPY build.gradle settings.gradle /app/
COPY gradlew /app/
COPY gradle /app/gradle

# Copy the application source
COPY src /app/src

# Download dependencies and build the application
RUN ./gradlew bootJar --no-daemon

# Second stage: create the executable image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file from the first stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
