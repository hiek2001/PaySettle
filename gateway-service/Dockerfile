# Step 1: 빌더 단계
FROM eclipse-temurin:21-jdk-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
#COPY gradlew /app/
#COPY gradle /app/gradle/
#COPY build.gradle /app/
COPY . .
# Gradle Wrapper에 실행 권한 부여
#RUN chmod +x ./gradlew

# gateway-service 모듈을 빌드 (테스트 제외)
#RUN ./gradlew :gateway-service:bootJar -x test --stacktrace

# 빌드된 JAR 파일을 최종 이미지로 복사
RUN cp build/libs/gateway-service-0.0.1-SNAPSHOT.jar /app/app.jar

# Step 2: 실행 단계
FROM eclipse-temurin:21-jdk-alpine

# 빌드 단계에서 생성된 JAR 파일을 복사
COPY --from=builder /app/app.jar /app.jar

# 애플리케이션 실행
CMD ["java", "-jar", "/app.jar"]