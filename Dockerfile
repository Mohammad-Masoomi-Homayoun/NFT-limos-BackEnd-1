FROM openjdk:11
COPY target/niftylimos-back-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 7070
CMD ["java", "-jar", "app.jar"]
