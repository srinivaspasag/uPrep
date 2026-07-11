# syntax=docker/dockerfile:1

# ---------- Build stage ----------
# Builds the entire lms_nextgen multi-module reactor once and produces
# all *-services fat jars. Multi-arch (works on Oracle Ampere ARM64).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the full source tree (reactor needs all modules to resolve com.lms:* deps)
COPY . .

# Build all modules in dependency order (reactor handles ordering automatically)
RUN mvn -B -DskipTests clean package

# ---------- Runtime stage ----------
# One image that contains every service jar. The container picks which jar to
# run via the SERVICE env var, so docker-compose can start 8 services from the
# same image (build once, run many).
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# Collect every service jar under predictable names: /app/<service>.jar
COPY --from=build /app/user/user-services/target/user-services-0.0.1-SNAPSHOT.jar                 /app/user-services.jar
COPY --from=build /app/organization/organization-services/target/organization-services-0.0.1-SNAPSHOT.jar /app/organization-services.jar
COPY --from=build /app/content/content-services/target/content-services-0.0.1-SNAPSHOT.jar         /app/content-services.jar
COPY --from=build /app/cmds/cmds-services/target/cmds-services-0.0.1-SNAPSHOT.jar                   /app/cmds-services.jar
COPY --from=build /app/board/board-services/target/board-services-0.0.1-SNAPSHOT.jar               /app/board-services.jar
COPY --from=build /app/social/social-services/target/social-services-0.0.1-SNAPSHOT.jar             /app/social-services.jar
COPY --from=build /app/comm/comm-services/target/comm-services-0.0.1-SNAPSHOT.jar                   /app/comm-services.jar
COPY --from=build /app/event/event-services/target/event-services-0.0.1-SNAPSHOT.jar               /app/event-services.jar

# Defaults (overridable via compose / env)
ENV SERVICE=user-services \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms128m -Xmx512m"

# Runs the jar selected by $SERVICE on $SERVER_PORT.
# Mongo connection is supplied via SPRING_DATA_MONGODB_URI (relaxed-binding env).
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dserver.port=$SERVER_PORT -jar /app/$SERVICE.jar"]
