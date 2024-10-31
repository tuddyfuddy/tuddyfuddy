# Spring Boot 서비스용 Dockerfile (auth-service, user-service, notification-service 등)

# Build stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /build

# 그래들 파일만 복사하여 의존성 다운로드 (캐시 활용)
COPY build.gradle settings.gradle /build/
COPY gradle /build/gradle
RUN gradle dependencies --no-daemon

# 소스 복사 및 빌드
COPY src /build/src
RUN gradle build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 빌드된 jar 파일 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# 힙 메모리 설정 등 JVM 옵션
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]