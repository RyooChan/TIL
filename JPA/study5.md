# 인프런 스터디 5
###### tags: `Tag(인프런)`

## 기본키 매핑

### 매핑 방법
* 직접 할당
    * @ID
* 자동 할당(@GeneratedValue)
    * IDENTITY
        * DB에 위임, MYSQL
    * SEQUENCE
        * DB시퀀스 오브젝트 사용, ORACLE
            * @SequenceGenerator 필요
    * TABLE
        * 키 생성용 테이블 사용, 모든 DB
            * @TableGenerator 필요
    * AUTO
        * 방언에 따라 자동 지정, 기본값

#### INDENTITY

* 기본 키 생성을 DB에 위임.
* 주로 MySql, PostgreSQL, SQL Server, DB2에서 사용한다.
* JPA는 보통 트랜잭션 커밋 시점에 INSERT SQL실행
* AUTO_INCREMENT는 DB에 INSERT SQL을 실행한 이후에 ID값을 알 수 있다.
* **INDENTITY전략은 em.persist()시점에 즉시 INSERT SQL을 실행하고 DB에서 식별자를 조회한다.**

이 전략은 DB에서 변경사항을 위임하는 것이다.
근데 이게 문제가 뭐냐면 내가 ID에 값을 넣을 수가 아예 없고, DB에서만 해당 요청이 실행된다.
즉, DB에 값이 들어가 봐야 ID값을 알 수 있다.
이전에 했던 영속성 컨텍스트에서 관리가 되려면 PK가 있어야 한다.

영속성컨텍스트에서 관리하려면 PK가 있어야 함.
근데 DB에 값이 들어가야 PK가 생김.

이 문제를 해결하기 위해, em.persist()호출 시점에 바로 INSERT쿼리를 날린다.
이렇게 INSERT가 완료되고 나면 그제서야 ID를 알 수 있다.

추가로 insert를 하고 ID를 안다는 것이, select를 추가로 진행하는 것은 아니다.
JDBC에서 알아서 insert할 때 값을 받아오기 때문에 이미 값은 저장되어 있는 것이다.

여기서는 insert를 한꺼번에 보내는 지연 전략이 의미가 없어지게 된다.

#### SEQUENCE
* DB시퀀스는 유일한 값을 순서대로 생성하는 특별한 DB오브젝트(ORACLE의 SQUENCE같은거)
* 오라클, PostgreSQL, DB2, H2 등에서 사용한다.

![](https://i.imgur.com/5Dky5cR.png)

![](https://i.imgur.com/jqKIeCh.png)

참고로 이거는 insert한꺼번에 보내는것이 가능하다.
그게 가능한 이유는 필요할 때에 sequence를 호출하여 값을 받아올 수 있기 때문임.

> **성능 최적화**

이게 근데 문제는 매번 값을 받기 위해 네트워크를 왔다갔다 해야하는데...오히려 성능에 문제가 생기는게 아닌가? 할 수 있다.
이를 해결할 때 사용하는 것이 allocationSize / initialValue이다.

처음 next sequence를 call할 때에 시작점을 allocationSize에 설정한 기본값만큼 키워서 진행한다.
만약 id가 2, 3, 4, 5를 갖는 값을 insert하며 id를 확인하려 한다면
1. 처음에 1을 호출한다.
2. id들은 이 1보다 큰 값들을 가진다. 다음은 50을 호출한다.
3. 이렇게 하면 sequence는 값이 늘어나 있고, id들은 메모리 상에서 빈 값에서 나타나기 때문에 더이상 호출을 할 필요 없이 값이 저장될 수 있다.
4. 여러번 왔다갔다 할 필요 없으므로 성능이 개선된다.
5. 50개보다 많은 값들을 넣으려 하면 한번 더 call해 올 것이다.

다만 이게 막 크기를 10000 이렇게 하면 더 좋긴 하지만 sequence에 구멍이 뻥뻥 뚫리게 되므로 보통 50, 100정도로 사용한다고 한다.
참고로 이거 동시성 이슈도 생기지 않는다. 자신이 호출한 값을 그냥 받아오기 때문이다.

#### TABLE
* 키 전용 테이블을 하나 만들어서 DB SEQUENCE를 흉내내는 것이다.
* 장점은 모든 DB에서 사용 가능
* 단점은 성능이 떨어짐.

![](https://i.imgur.com/jYO847M.png)

![](https://i.imgur.com/nnvCU2E.png)

이거 쓸바에는 위에 두개중에 하나 선택해 쓰는것이 좋다.
그냥 내 생각에는 SEQUENCE가 가장 좋을듯 싶다.

### 식별자 전략
* 기본 키 제약 조건
    * Null비허용, 유일성, 변하면 안된다.

> 저 변하면 안되는 것을 만족하는 방법은 사용하기 힘들다.
> 왜냐면 10, 20, 100년 뒤까지 이게 쭉쭉 사용되면 변할 수 있기 때문이다.
> 따라서 자연키(주민번호 이런거... 변경될 여지가 있기도 하고 저장하면 안되기도 하고.) 대신 대리키(대체키)를 사용하도록 한다.

권장하는 방법은 -> Long형 + 대체키(sequence 등) + 키 생성전략 사용
AUTO_INCREMENT나 SEQUENCE이런거 걍 쓰면 될거같다.

