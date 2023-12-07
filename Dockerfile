# Use an official OpenJDK runtime as a parent image
FROM openjdk:19-jdk-slim

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the JAR file into the container
COPY flagbot-0.0.1-SNAPSHOT.jar .

# Specify the command to run on container startup
CMD ["java", "-jar", "flagbot-0.0.1-SNAPSHOT.jar"]
