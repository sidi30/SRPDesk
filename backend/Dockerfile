FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src/ src/
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre
RUN addgroup --system --gid 1001 app && adduser --system --uid 1001 --gid 1001 app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
USER app
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar app.jar"]
