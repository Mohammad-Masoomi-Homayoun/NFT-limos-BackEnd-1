## Using Maven as a builder
#FROM maven:3.6.3-openjdk-11 AS builder
#
## Create the maven user
#RUN addgroup --gid 1000 maven && \
#    adduser --uid 1000 --ingroup maven --home /home/maven --shell /bin/sh --disabled-password --gecos "" maven
#
## Copy the project files to the docker container
#COPY --chown=maven:maven . /home/maven/src
#
## Set the working directory
#WORKDIR /home/maven/src
#
## Run maven build
#RUN mvn clean package -DskipTests

# Creating the final image
FROM adoptopenjdk/openjdk11:alpine as finalApp
WORKDIR /opt/app
COPY ./target/*.jar /opt/app/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "niftylimos-back-0.0.2-SNAPSHOT.jar"]
