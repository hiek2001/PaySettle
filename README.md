# PaySystem

## 🚀프로젝트 소개

스트리밍 플랫폼의 판매자를 위한 통계 및 정산 플랫폼입니다. 콘텐츠 성과와 수익을 효율적으로 관리하고 분석할 수 있는 기능을 제공합니다.

**개발 기간**

2024.10.16 ~ 2024.11.12 (4주)

**프로젝트 호스팅**

[김혜란](https://github.com/hiek2001)


## 💻 Docker-Compose 실행 명령어

```
docker-compose up -d
```



## 🗂 ERD

![title](https://file.notion.so/f/f/0a660537-6246-44fa-b89f-8d3674af3d87/f8a18d7c-d8e8-4c33-a05c-049b5c1076ca/image.png?table=block&id=129533b2-f0c0-8093-8cbd-caf6c54ca99f&spaceId=0a660537-6246-44fa-b89f-8d3674af3d87&expirationTimestamp=1731585600000&signature=IZ95Y4Z_9xvv2sLlbCn8V7MR_zJpBI-Zq4TuC3NgVAc&downloadName=image.png)

## 📚 API 명세서

(API 명세 추가)


## 🛠기술스택
- Spring Boot, Spring Batch, Spring Security, Java, Gradle
- MySQL, JPA
- Redis
- Spring Cloud Gateway, Eureka, Resilience4J
- Docker-Compose, Github

## 🏗 아키텍쳐

![ex_screenshot](./image/paySystem_architecture.png)

## 🌟주요 기능

### 백엔드
1. Spring Boot 기반 RESTful API
    -  확장성과 실시간 성능을 고려한  개발
2. MSA 아키텍처
    -  부하 분산 및 장애 복구 기능을 포함
3. 모노레포 구성
    -  여러 모듈을 통합하여 관리 효율성을 높이고, 서비스 간 통신을 통해 데이터 일관성을 유지

### 데이터 처리
1. 대용량 배치 작업 및 파티셔닝
    -  영상 재생 내역을 일별, 주별, 월별로 집계하여 정산 및 통계 조회 데이터로 제공
    -  정산 서비스에서 병렬 처리 기법을 통해 처리 속도와 데이터 작업을 최적화
2. Redis 캐싱
    -  정산 조회 데이터를 Redis에 캐싱하여 사용자에게 빠른 응답 제공
3. MySQL 기반 데이터 관리
    -  안정적인 데이터 저장 및 조회를 위해 MySQL을 활용해 데이터를 관리

### 부하 분산 및 장애 복구
1. 도메인별 서비스 분리 및 로드 밸런싱
    -  부하 분산을 위해 도메인별 서비스 분리
    -  Spring Cloud Gateway, LoadBalancer, Eureka를 활용하여 부하가 큰 서비스에 대해 다수 인스턴스 간의 부하 분산을 구현

### 주요 API 기능
1. 통계 조회 API
    -  일별, 주별, 월별 조회수 및 재생 시간의 Top5 조회하여 콘텐츠의 인기도를 파악할 수 있음
2. 정산 조회 API
    -  일별, 주별, 월별 수익을 확인할 수 있도록 정산 내역 조회 기능 제공

## 🔥성능 최적화

1. Batch 작업 : Reader 최적화와 스탭 파티셔닝 도입

| 단계 | 데이터 규모 | 처리시간 | 개선율 |
| --- | --- | --- | --- |
| 최적화 전 | 300만 건 | 90분 + | - |
| 1차 최적화 | 300만 건 | 34분 6초 | 62.22% ↓ |
| 2차 최적화 | 300만 건 | 6분 29초 | 82.35% ↓ |

(구체적인 내용 링크 삽입)

2. 통계 조회 API : 다중 컬럼 인덱싱을 활용
    -  **조회 쿼리 속도 7798ms -> 1.474ms**

| 단계 | 데이터 규모 | TPS | 개선율 |
| --- | --- | --- | --- |
| 최적화 전 | 50만 건 | 0.77 /sec | - |
| 최적화 후 | 50만 건 | 48.4 /sec | 6185.71% ↑ |

(구체적인 내용 링크 삽입)

3. 정산 조회 API : Redis 캐싱 활용

(아직 완료 안됨)

## 💭 기술적 의사결정
-  Redis 캐싱 사용 이유와 TTL 설정에 대한 의사 결정
- MSA 도입
- 초반 설계 고민 내용

(링크 삽입 예정)