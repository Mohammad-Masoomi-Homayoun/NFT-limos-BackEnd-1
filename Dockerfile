# Use the official Apache Tomcat image as a base image
FROM tomcat:9-jdk11-openjdk-slim

# Remove the default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file from the target directory to the Tomcat webapps directory
COPY target/niftylimos-back-*.war /usr/local/tomcat/webapps/ROOT.war

# Expose port 8080 for the Tomcat server
EXPOSE 8080

# Start the Tomcat server
CMD ["catalina.sh", "run"]
