FROM maven:3.9.5-amazoncorretto-21-debian AS builder

WORKDIR /bot

COPY . .

RUN mvn clean package -DskipTests

FROM openjdk:21

COPY --from=builder /bot/target/NPLAY-Bot.jar ./NPLAY-Bot.jar

COPY src/main/resources/db/migration ./db/migration

CMD ["java", "-jar", "NPLAY-Bot.jar"]
