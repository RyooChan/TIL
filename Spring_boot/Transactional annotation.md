# Transactional annotation

## Transaction이란

> DB에서의 상태 변경을 위해 수행하는 작업의 단위

간단하게 말하면 CRUD를 통해 DB에의 접근을 진행하는 것이다.

Transaction은 `ACID`라는 성질을 갖는다.

* Atomicity(원자성)
    * 트랜잭션은 모두 DB에 반영되거나, 그렇지 않으면 모두 반영되지 않아야 한다.
* Consistency(일관성)
    * 트랜잭션 처리 결과는 일관성이 있어야 한다.
    * 즉, 트랜잭션 진행 도중 DB가 변경되어도 처음 트랜잭션 진행을 위해 참조한 데이터 기준으로 적용
* Isolation(독립성)
    * 둘 이상의 트랜잭션이 동시에 수행되고 있는다면 다른 트랜잭션의 연산에는 끼어들수 없다.
* Durability(지속성)
    * 트랜잭션이 성공적으로 완료되면 그 결과는 영구적으로 반영되어야 한다.

## Transactional annotation

Spring boot에서 제공하는 annotation인데, 이를 사용해서 트랜잭션 기능이 포함된 프록시 객체가 생성된다.
이를 통해 자동 commit/rollback이 구현된다.
-> 말하자면 트랜잭션 처리를 proxy객체에게 위임하여 AOP로 동작시키는 것이다.

## Transactional 기능

사실 나는 그냥 Transactional을 통해 commit/rollback을 구현했고, readonly정도 말고는 다른 기능을 쓴적이 없다.
근데 이게 생각보다 엄청 기능이 많다.

* isolation
    * 격리 수준
        * 일관성 없는 데이터 허용 수준 설정
* propagation
    * 전파 옵션
        * 동작 도중 다른 트랜잭션 호출 시, 어떻게 할 것인지
* noRollbackFor
    * 특정 예외 발생 시 rollback이 동작하지 않도록 설정
* rollbackFor
    * 특정 예외 발생 시 rollback이 동작하도록 설정
* timeout
    * 지정 시간 내 메소드 수행이 완료되지 않으면 rollback
* readOnly
    * 읽기 전용

### isolation

[이거](https://hello-backend.tistory.com/182)를 보고오면 해당 격리수준의 내용을 확인할 수 있다.

각각의 격리수준을

`@Transactional(isolation = Isolation.원하는수준)`

으로 설정하면 된다.

* DEFAULT(기본 설정)
    * DBMS의 기본 격리수준 적용
* READ_UNCOMMITED
    * 별로 권장되는 방식은 아님
* READ_COMMITED
    * postgre, SQL Server, Oracle의 DEFAULT 설정
* REPEATABLE_READ
    * Oracle에서는 미지원
    * MySql의 DEFAULT 설정
* SERIALIZABLE
    * 이것도 잘 안쓰인다...

### propagation

하나의 트랜잭션이 동작하고 있는 동안 다른 트랜잭션을 실행하는 상황에 선택한다.
이를 사용해서 트랜잭션이 호출되었을 때에 `새로운 트랜잭션을 생성할지` / `기존 트랜잭션을 그대로 사용할지` 고를수 있다.

`@Transactional(propagation = Propagation.원하는전파옵션)

* REQUIRED(기본 설정)
    * 현재 활성화된 트랜잭션이 있으면 그 안에서 실행
    * 현재 활성화된 트랜잭션이 없으면 새로운 트랜잭션 실행
* SUPPORTS
    * 현재 활성화된 트랜잭션이 있으면 그 안에서 실행
    * 현재 활성화된 트랜잭션이 없으면 그냥 트랜잭션 없이 실행
* MANDATORY
    * 현재 활성화된 트랜잭션이 있으면 그 안에서 실행
    * 현재 활성화된 트랜잭션이 없으면 예외 발생
    * 독립적으로 트랜잭션을 진행하면 안되는 경우에 사용한다.
* NEVER
    * 현재 활성화된 트랜잭션이 있으면 예외 발생
    * 현재 활성화된 트랜잭션이 없으면 그냥 트랜잭션 없이 실행
    * 아예 트랜잭션을 쓰면 안되는 경우에 사용한다.
* NOT_SUPPORTED
    * 현재 활성화된 트랜잭션이 있으면 이걸 일단 보류 후 트랜잭션 없이 작업 수행
    * 현재 활성화된 트랜잭션이 없으면 그냥 트랜잭션 없이 실행
* REQUIRES_NEW
    * 현재 활성화된 트랜잭션이 있으면 이걸 일단 보류 후 새 트랜잭션 생성
    * 현재 활성화된 트랜잭션이 없으면 새로운 트랜잭션 실행
* NESTED
    * 현재 활성화된 트랜잭션이 있으면 해당 시점에 저장점 생성후 하위에 트랜잭션 생성
        * 하위 트랜잭션이 commit/rollback되어도 기존 트랜잭션은 영향을 받지 않는다.
            * 예외 발생시 해당 시점으로 rollback
    * 현재 활성화된 트랜잭션이 없으면 새로운 트랜잭션 실행
    * 작업 중 로그를 DB에 저장하는 작업을 할 때에 사용
        * 로그를 DB에 저장하는것은 중요하지만, 이게 전체 transaction의 fail 요소가 되어서는 안된다. 사실상 비즈니스 로직과는 관계가 없기 때문
            * 그리고 전체 로직(상위트랜잭션)이 rollback되면 하위 로그 transaction도 rollback되는게 맞다.

### rollbackFor, noRollbackFor

> 본래 spring에서는 런타임 예외만을 롤백 대상으로 삼는다.
> 체크 예외는 커밋된다.

-> Spring에서 데이터 액세스 기술의 예외는 런타임 예외로 전환되기 때문에 런타임 예외만 롤백 대상으로 삼은 것이다.

근데 이제 위의 친구들을 사용해서 다른 예외들을 롤백대상으로 삼을 수 있다.

* rollbackFor
    * 이런 예외가 발생하면 강제로 Rollback한다.
    * `@Transactional(rollbackFor=Exception.class)`
* noRollbackFor
    * 이런 예외는 Rollback처리하지 않는다.
    * ` @Transactional(noRollbackFor=Exception.class)`

### timeout

지정 시간 내에 메소드 수행이 끝나지 않으면 rollback한다.

`@Transactional(time=초단위시간기입)`

### readOnly

`@Transactional(readOnly = true)`

읽기 전용으로 설정

필자는 기본적으로 spring에서 기능을 구현할 때에 기본적으로 readonly로 해준다.
그 이유는 이게 읽기만 하는 경우 성능 최적화가 가능하기 때문이다.

물론 이걸 선언하면 CUD로직이 있는 곳에서는 따로 readOnly default를 갖는 Transactional을 선언해줘야 하기는 한다.

