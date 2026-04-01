FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
COPY pom.xml .
RUN ./mvnw dependency:go-offline -q
COPY src src
RUN ./mvnw package -q -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
