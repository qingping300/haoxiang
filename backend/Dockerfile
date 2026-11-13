FROM m.daocloud.io/docker.io/library/maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package

FROM m.daocloud.io/docker.io/library/eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/haoxiang-backend.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
