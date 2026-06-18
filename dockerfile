FROM eclipse-temurin:26-jre
WORKDIR /app
COPY ./target/flow-pay-0.0.1-SNAPSHOT.jar ./flow-pay-0.0.1-SNAPSHOT.jar
EXPOSE 8081
CMD ["java", "-jar", "flow-pay-0.0.1-SNAPSHOT.jar"]
