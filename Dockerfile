FROM gradle:7.4.2-jdk8 AS build_stage
WORKDIR /home/gradle
COPY . .
RUN gradle build

FROM amazoncorretto:11
COPY --from=build_stage /home/gradle/drill-cli/build/libs/app.jar /opt/

ENTRYPOINT java -jar /opt/app.jar $MODE_PARAM