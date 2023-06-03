FROM amazoncorretto:17
WORKDIR '.'
COPY ./build/libs/apptracky-0.0.1-SNAPSHOT.jar apptracky-0.0.1-SNAPSHOT.jar
ENTRYPOINT java -jar -Dspring.profiles.active=prod /apptracky-0.0.1-SNAPSHOT.jar