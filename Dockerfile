# syntax=docker/dockerfile:1

# --- Build: compila con el Maven Wrapper. Las pruebas ya se ejecutan en el job de CI. ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY infrastructure/pom.xml infrastructure/
COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src

RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -B -ntp -DskipTests -Djacoco.skip=true package

# Capas de Spring Boot: las dependencias (cambian poco) quedan en capas propias y se reutilizan.
RUN java -Djarmode=tools -jar infrastructure/target/infrastructure-*.jar \
    extract --layers --launcher --destination /workspace/extracted

# --- Runtime: solo JRE, usuario sin privilegios. ---
FROM eclipse-temurin:21-jre
RUN groupadd --system app && useradd --system --gid app --no-create-home app
WORKDIR /app

COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER app

# Perfil prod: sin valores por defecto ni clave efímera. Al ejecutar hay que definir DB_URL, DB_USERNAME,
# DB_PASSWORD, SECURITY_JWT_PUBLIC_KEY y SECURITY_JWT_PRIVATE_KEY (y opcionalmente SECURITY_BOOTSTRAP_ADMIN_*).
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8081
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
