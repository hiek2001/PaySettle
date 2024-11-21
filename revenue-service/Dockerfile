# Step 1: 빌더 단계
FROM gradle:jdk21-alpine AS builder

# Bash와 필수 유틸리티 설치
RUN apk update && apk add --no-cache bash

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew /app/
COPY gradle /app/gradle/
COPY build.gradle /app/
COPY settings.gradle /app/

# revenue-service 모듈의 Gradle 파일 및 src 디렉토리 복사
COPY revenue-service/build.gradle /app/revenue-service/
COPY revenue-service/src /app/revenue-service/src/

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# revenue-service 모듈을 빌드 (테스트 제외)
RUN ./gradlew :revenue-service:bootJar -x test --stacktrace
#RUN gradle :revenue-service:build -x test --no-daemon
# 빌드된 JAR 파일을 최종 이미지로 복사
RUN cp revenue-service/build/libs/revenue-service-0.0.1-SNAPSHOT.jar /app/app.jar

# Step 2: 실행 단계
FROM eclipse-temurin:21-jre-alpine

# 빌드 단계에서 생성된 JAR 파일을 복사
COPY --from=builder /app/app.jar /app.jar

# 애플리케이션 실행
CMD ["java", "-jar", "/app.jar"]