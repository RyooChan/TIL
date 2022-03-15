## OSIV와 성능 최적화
Open Session In View : 하이버네이트
Open EntityManager In View : JPA (관례상 OSIV라 한다.)

### OSIV ON

![](https://i.imgur.com/pt3FC7v.png)

Spring boot 어플리케이션을 처음 실행하면 다음과 같은 warn문구가 출력된다.
`2022-03-15 11:23:35.504  WARN 11052 --- [  restartedMain] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning`

이 기본값이 시작 시점에 warn로그를 남기는 것에는 이유가 있다.

JPA가 영속성 컨텍스트와 데이터베이스 커넥션을 처음 가져오는 시점은 기본적으로 DB transaction을 시작할 때이다.

* 데이터베이스 커넥션을 돌려주는 시점은
    * OSIV가 켜져 있으면
        * 해당 Transaction이 끝나고, 밖으로 나가도 반환하지 않는다.
            * 반환 시점은
                * API가 유저에게 반환될 때 까지 유지된다.
                * View가 렌더링이 끝날 때 까지 유지된다.

이 덕분에 영속성 컨텍스트가 Controller등에서도 살아 있어서 지연 로딩 등이 가능했다.

다만 해당 전략은 너무 오랫동안 DB커넥션 리소스를 사용한다.
따라서 실시간 트래픽이 중요한 애플리케이션에서는 커넥션이 말라버릴수가 있다. 이는 장애로 이어진다.

예를 들어서 컨트롤러에서 외부 API를 호출할 때 3초정도 걸린다 하면 그 3초동안 데이터베이스 커넥션을 사용중인 것이다.
그 시간동안 커넥션 리소스를 반환하지 못하고 유지해야 한다.

### OSIV OFF
![](https://i.imgur.com/lJGcEdP.png)

* spring.jpa.open-in-view: false
를 써서 off할 수 있다.

off하면 트랜잭션이 종료될 때에 영속성 컨텍스트를 닫고, 데이터베이스 커넥션도 반환한다.

이를 통해 커넥션 리소스를 낭비하지 않게 한다.
다만, 이렇게 되면 모든 지연로딩을 트랜잭션 안에서 처리해야 한다.
그리고 view template에서 지연로딩이 동작하지 않는다.
결론적으로 트랜잭션이 동작중인 동안 지연 로딩을 강제로 호출해 주어야 한다.

이 문제를 해결하는 방법이 있다.

#### 커멘드와 쿼리 분리
보통 비즈니스 로직은 특정 엔티티 몇개를 등록하거나 수정하기 때문에 성능에 크게 문제가 없다.
그런데 복잡한 화면 출력용 쿼리 등은 화면에 맞추어 성능을 최적화 하는 것이 중요하다.
그런데 복잡성에 비해 핵심 비즈니스에 큰 영향을 주는 것은 아니다.

화면 찍는것과 핵심 비즈니스의 관심사를 명확하게 분리하여 진행한다.

##### Query용 서비스 생성
관심사 분리 방법 중 하나는 로직에 따라 새로운 서비스를 만들어서 처리하는 것이다.

@Transactional 어노테이션을 받는 추가적인 서비스를 생성한다.

이후에 기존에 Controller등에서 처리하던(OSIV OFF시 영속성 컨텍스트의 영향에서 벗어나는) 영속성 컨텍스트 관련 로직을 여기로 옮겨서 처리한다.

---

### 결론

OSIV를 켜면 쿼리 서비스를 view나 controller에서 처리해 줄 수 있다.

그래서 실무에서 고객 서비스의 실시간 API는 OSIV를 끄고, ADMIN처럼 커넥션을 많이 사용하지 않는 곳에서는 OSIV를 켠다고 한다.

---
