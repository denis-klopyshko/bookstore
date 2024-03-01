FROM maven:3.8.4-eclipse-temurin-17 as builder

WORKDIR /app
COPY . .

RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar bookstore.jar

EXPOSE 8181

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/bookstore.jar ${0} ${@}"]
