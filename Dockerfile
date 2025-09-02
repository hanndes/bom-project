# ---------- BUILD STAGE ----------
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

# Maven wrapper ve pom'u kopyala
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn

# Bağımlılıkları indir
RUN ./mvnw -q -e -DskipTests dependency:go-offline

# Kaynak kodu kopyala ve build et
COPY src ./src
RUN ./mvnw -q -DskipTests package

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Jar dosyasını kopyala
COPY --from=build /app/target/*SNAPSHOT.jar /app/app.jar

# Docker profilini aktif et
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
