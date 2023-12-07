# Use the official Maven image with a tag to specify the version of Maven you want to use
FROM maven:3.8.4-jdk-11 AS build

# Copy the source code to the container
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app

# Build the application
RUN mvn -f /usr/src/app/pom.xml clean package

# For the final image, use the official OpenJDK image to run the application
FROM openjdk:11-jre-slim

# Copy the JAR file from the build stage to the final image
COPY --from=build /usr/src/app/target/flagbot-1.0.jar /usr/app/flagbot-1.0.jar

# Set the working directory in the container
WORKDIR /usr/app

# Command to run the application
CMD ["java", "-jar", "flagbot-1.0.jar"]

