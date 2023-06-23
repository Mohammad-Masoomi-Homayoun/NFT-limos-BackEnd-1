# Using Maven as a builder
FROM maven:3.6.3-openjdk-11 AS builder

# Create the maven user
RUN addgroup --gid 1000 maven && \
    adduser --uid 1000 --ingroup maven --home /home/maven --shell /bin/sh --disabled-password --gecos "" maven

# Copy the project files to the docker container
COPY --chown=maven:maven . /home/maven/src

# Set the working directory
WORKDIR /home/maven/src

# Run maven build
RUN mvn clean package -DskipTests

# Creating the final image
FROM tomcat:9.0-jdk11 as finalApp

# Copy the war file from the builder image to the Tomcat webapps directory
COPY --from=builder /home/maven/src/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Expose the Tomcat port
EXPOSE 8080

#ENTRYPOINT ["-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n"]
# Start Tomcat
CMD ["catalina.sh", "run"]
