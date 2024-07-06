FROM redgate/flyway as flyway

WORKDIR /flyway


FROM maven:3.9.5-amazoncorretto-21-debian AS builder

WORKDIR /bot
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21

COPY --from=flyway /flyway ./flyway
COPY src/main/resources/db/migration ./db/migration

COPY --from=builder /bot/*.sh .
COPY --from=builder /bot/target/NPLAY-Bot.jar ./NPLAY-Bot.jar
COPY --from=builder /bot/embeds.json .

RUN chmod +x ./wait-for-it.sh ./entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
CMD ["java", "-jar", "NPLAY-Bot.jar"]
