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
FROM adoptopenjdk/openjdk11:alpine as finalApp

ARG JAR_FILE=target/niftylimos-back-0.0.2-SNAPSHOT.jar

WORKDIR /opt/app

# Copy the JAR file from the maven stage to the /opt/app directory of the current stage.
COPY --from=builder /home/maven/src/${JAR_FILE} /opt/app/

# Expose the Tomcat port
EXPOSE 8080

# Set the entrypoint command to run the application
ENTRYPOINT ["java", "-jar", "niftylimos-back-0.0.2-SNAPSHOT.jar"]
