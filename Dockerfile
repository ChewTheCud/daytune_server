# Use Amazon Corretto 17 as a parent image
FROM amazoncorretto:17

# Set the working directory
WORKDIR /app

# Copy the built JAR file into the container
COPY build/libs/eumakase-0.0.1-SNAPSHOT.jar app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
