# Stage 1: Build
FROM gradle:8.5-jdk17-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 의존성 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (캐싱 최적화)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 스테이지에서 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 포트 노출
EXPOSE 8080

# 환경 변수 설정 (Render에서 오버라이드 가능)
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 헬스체크 설정 (선택사항)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
