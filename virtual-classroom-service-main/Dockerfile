# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B -DskipTests clean package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=build /app/target/virtualClassRoomService-0.0.1-SNAPSHOT.jar /app/virtual-classroom.jar

ENV SERVER_PORT=20000 \
    JAVA_OPTS="-Xms128m -Xmx384m"

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar /app/virtual-classroom.jar"]
