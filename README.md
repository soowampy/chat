# 채팅 어플리케이션

## 작업 환경

```
OS : Java 17
Framework : Spring Boot 3.3.2
DB : H2 Database, Redis
WebSocket
Docker-compose
```



## 구현 내용

- 사용자는 닉네임을 설정하고 채팅방 목록에 접속합니다.

- 새로운 채팅방을 생성하거나 기존 채팅방에 입장할 수 있습니다.

- 채팅방에 입장한 후 이전 모든 메시지를 볼 수 있으며, 실시간으로 메시지를 주고 받을 수 있습니다.
- 30분 내 접속자 수를 알 수 있습니다.

- 사용자가 채팅방에 들어오거나 나갈 때 시스템 메시지가 표시됩니다.

- Redis를 사용하여 접속자 수를 관리하고, H2 Database에 채팅 메시지와 채팅방 정보를 저장합니다.

> ### Redis와 RDB를 함께 사용한 이유
>
> - **Redis**는 실시간 데이터 처리를 위해 사용하였습니다.
> - **H2 Database**는 채팅 메시지와 채팅방 정보를 영구적으로 저장하기 위해 사용하였습니다.
> - 실시간 성능과 영구 저장의 두 가지 요구를 충족시키기 위해 Redis와 RDB를 함께 사용하였습니다.



## 실행 방법

1. GitHub에서 프로젝트를 클론 받습니다.

2. gradle build를 실행합니다.

   `./gradlew build`

3. docker desktop을 실행합니다.

4. docker build & run 을 실행합니다.

   ` docker-compose up --build`

5. `localhost:8080` 포트로 접근합니다.
