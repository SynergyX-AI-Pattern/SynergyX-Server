# 📈 PatternCatcher - Main Server
  

  
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)

> **AI 주가 예측 및 차트 패턴 기반 실시간 감지·백테스팅 시스템**  
> 🏆 한이음 드림업 장려상 수상작

[🏠 Organization](https://github.com/SynergyX-AI-Pattern) • [📗 ML Server](https://github.com/SynergyX-AI-Pattern/SynergyX-ML-Server) • [📙 Client](https://github.com/SynergyX-AI-Pattern/SynergyX-Client)



<img width="100%" alt="1-6f60498e" src="https://github.com/user-attachments/assets/24c17eaf-4506-4365-9752-37737730047c" />

---

## 📌 Overview
**PatternCatcher**는 개인 투자자가 자신만의 차트 패턴을 정의하고, <br>  실시간 감지 및 백테스팅을 통해 투자 전략의 유효성을 검증할 수 있는 AI 투자 보조 시스템입니다.

**Main Server**는 사용자 인증, 종목 데이터 수집, 패턴 관리, FCM 푸시 알림 등 **핵심 비즈니스 로직을 담당하는 Spring Boot 기반 RESTful API 서버**입니다. <br>   모든 클라이언트 요청을 처리하고, AI 기능(패턴 감지, 백테스팅, 주가 예측)은 [ML Server](https://github.com/SynergyX-AI-Pattern/SynergyX-ML-Server)와 연동하여 수행합니다.

### 주요 역할
- 🔐 **사용자 인증/인가** - JWT 기반
- 📊 **실시간 주가 데이터 수집** - 한국투자증권 API 연동 (KOSPI 100 종목)
- 💾 **과거 5년 데이터 관리** - 백테스팅용 주가 데이터 수집/저장
- 🎯 **패턴 CRUD** - 사용자 정의 패턴 생성/수정/삭제
- 🔔 **실시간 알림** - FCM 푸시 알림 발송
- 🤝 **ML Server 연동** - AI 분석 요청 및 결과 저장
---

## 🛠 Tech Stack

| Category | Technologies |
|----------|-------------|
| **Framework** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.4-6DB33F?logo=springboot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?logo=springsecurity&logoColor=white) ![JPA](https://img.shields.io/badge/JPA-6DB33F?logo=spring&logoColor=white) |
| **Language** | ![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white) |
| **Database** | ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white) |
| **Security** | ![JWT](https://img.shields.io/badge/JWT-000000?logo=jsonwebtokens&logoColor=white) ![BCrypt](https://img.shields.io/badge/BCrypt-003A70?logo=spring&logoColor=white) |
| **Infrastructure** | ![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?logo=amazonec2&logoColor=white) ![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?logo=amazons3&logoColor=white) |
| **Documentation** | ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?logo=swagger&logoColor=black) ![SpringDoc](https://img.shields.io/badge/SpringDoc-85EA2D?logo=spring&logoColor=white) |
| **External API** | ![한국투자증권](https://img.shields.io/badge/한국투자증권_Open_API-003876?logo=investopedia&logoColor=white) ![Firebase](https://img.shields.io/badge/Firebase_FCM-FFCA28?logo=firebase&logoColor=black) |

---

## 🏗 System Architecture
<img width="579" alt="System Architecture" src="https://github.com/user-attachments/assets/f7ae2bbf-5c16-4480-94c4-e54c5ca0359a" />

---

## 🗂 ERD
<img width="2868" alt="ERD" src="https://github.com/user-attachments/assets/886f1424-93fe-4d88-a384-a7e9a6786d1d" />

---

## 📁 Project Structure
```
src/main/java/com/synergyx/trading/
├── TradingApplication.java        # Spring Boot main
├── apiPayload/                    # API 응답 처리
├── config/                        # 설정 (Security, JWT)
├── controller/                    # REST API 컨트롤러
├── dto/                           # DTO
│   ├── user/
│   ├── pattern/
│   ├── backtest/
│   └── kis/
├── model/                         # 엔티티
├── repository/                    # JPA Repository
├── service/                       # 비즈니스 로직
│   ├── userService/
│   ├── stockService/
│   ├── patternService/
│   ├── backtestService/
│   └── fcmService/
├── scheduler/                     # 스케줄러
└── util/                          # 유틸리티
```

---

## 🚀 주요 기능

### 1. 사용자 인증/인가
- JWT 기반 액세스/리프레시 토큰 인증
- Spring Security 기반 보안 설정
- 회원가입, 로그인, 로그아웃 API

### 2. 실시간 주가 데이터 수집 및 관리
- **한국투자증권 Open API 연동** (KOSPI 100 종목)
- **15분 주기 자동 데이터 수집** (스케줄러 기반)
  - 실시간 현재가 수집 및 DB 저장
  - 15분봉/1시간봉/1일봉/1개월봉 OHLCV 데이터 수집
  - 재무정보 자동 업데이트
- **과거 5년 거래 데이터 수집** (백테스팅용)
- 수집된 데이터를 ML Server 및 Client에 제공

### 3. 종목 정보 관리
- 종목 검색 및 목록 조회 (Top 20, AI Top 20)
- 종목 상세 정보 조회 (실시간 가격, 재무정보)
- 관심 종목 등록/조회/삭제
- 최근 조회 종목 자동 기록

### 4. 차트 패턴 관리
- 사용자 정의 패턴 생성/수정/삭제
- 패턴 목록 조회 및 상세 정보
- 패턴 적용 종목 설정
- 실시간 패턴 감지 알림 설정

### 5. 백테스팅 및 주가 예측 관리
- ML Server로 백테스팅 요청 전달 (수집된 과거 5년 데이터 활용)
- 백테스팅 결과 저장 및 조회
- 백테스팅 랭킹 제공 (최대 수익률 기준)
- AI 주가 예측 결과 조회

### 6. AI 기능 관리
- **감정 투자 일기**: ML Server 연동하여 일기 작성, 감정 분석 요청 및 결과 저장/조회
- **AI 종목 검색**: ML Server 연동하여 이미지 기반 종목 검색 요청 처리

### 7. 실시간 알림
- Firebase Cloud Messaging(FCM)을 통한 푸시 알림
- 패턴 매칭 시 자동 알림 발송
- 알림 히스토리 조회 및 읽음 처리
- 알림 설정 관리 (on/off)

---

## 👥 Contributors

| | 한지수 | 조수민 |
|:---:|:------:|:------:|
| **GitHub** | [@eldeoddt](https://github.com/eldeoddt) | [@Soomxn](https://github.com/Soomxn) |
| **Role** | Team / Backend Lead | Backend / ML Engineer |
| **Profile** | <img width="120" src="https://avatars.githubusercontent.com/eldeoddt" /> | <img width="120" src="https://avatars.githubusercontent.com/Soomxn" /> |
| **담당** | <div align="left"> • 프로젝트 구조 설계 <br/>• JWT 인증 시스템<br/>• 한국투자증권 API 연동<br/>• AWS 인프라 구축</div> | <div align="left"> • 실시간 패턴 감지 <br/> • 백테스팅/패턴 관리 <br/>• FCM 푸시 알림<br/>• ML Server 연동 API </div> |

---

## 🔗 Related Repositories

- [📗 ML Server](https://github.com/SynergyX-AI-Pattern/SynergyX-ML-Server) - FastAPI 기반 AI 서버
- [📙 Client](https://github.com/SynergyX-AI-Pattern/SynergyX-Client) - Flutter 모바일 앱

---

## 📧 Contact

**Email**: patterncatcher83@gmail.com

---

<div align="center">

**PatternCatcher Main Server** by Team SynergyX

© 2025 Team SynergyX. All rights reserved.

</div>
