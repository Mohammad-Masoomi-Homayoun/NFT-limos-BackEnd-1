FROM openjdk:11-jre-slim

# Copy your WAR file to the container
COPY target/niftylimos-back-*.war app.war

# Command to run the Spring Boot WAR
ENTRYPOINT ["java", "-jar", "app.war"]

# Expose the port your app runs on
EXPOSE 8080
