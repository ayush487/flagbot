# Use the official Maven image with JDK 17
FROM maven:3.8.4-jdk-17 AS build

# Copy the source code to the container
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app

# Build the application
RUN mvn -f /usr/src/app/pom.xml clean package

# Use the official OpenJDK image with JDK 17 for the final image
FROM openjdk:17-jre-slim

# Copy the JAR file from the build stage to the final image
COPY --from=build /usr/src/app/target/flagbot-0.0.1-SNAPSHOT.jar /usr/app/flagbot.jar

# Set the working directory in the container
WORKDIR /usr/app

# Command to run the application
CMD ["java", "-jar", "flagbot.jar"]
