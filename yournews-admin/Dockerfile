FROM eclipse-temurin:17-jre
COPY ./build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]