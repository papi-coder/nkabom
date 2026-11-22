FROM maven:3.9.15-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY approval-backend/pom.xml approval-backend/pom.xml
COPY approval-backend/src approval-backend/src
COPY index.html index.html
RUN mvn -B -f approval-backend/pom.xml -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 10001 appuser
COPY --from=build /workspace/approval-backend/target/nkabom-approval-backend-1.0.0.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
