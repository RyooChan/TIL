# 인프런 스터디 7
###### tags: `Tag(인프런)`

## 연관관계 매핑
* 다중성
* 단방향, 양방향
* 연관관계의 주인

### 다중성
* 다대일
    * @ManyToOne
* 일대다
    * @OneToMany
* 일대일
    * @OneToOne
* 다대다
    * @ManyToMany

### 단방향, 양방향
* 테이블
    * 외래키 하나로 양쪽 모두에서 join이 가능하다.
    * 방향이라는 개념이 없다.
* 객체
    * 참조용 필드가 있는 쪽으로만 참조 가능
    * 한쪽만 참조하는 경우 단방향
    * 양쪽이 서로 참조하면 양방향

#### 다대일
가장 많이 사용하는 연관관계.
'다' 쪽에 FK가 위치한다.
그렇기 때문에 '다'쪽이 연관관계의 주인이 된다.

#### 일대다
일대다에서는 일이 연관관계의 주인이 되어, FK를 관리한다.
사실 별로 권장되지 않는 방법이다.

* 일대다 단방향
    * 예를 들어 Team과 Member에 대해 Team이 Member의 정보를 갖고 Member는 Team의 정보를 알고싶지 않은 것이다.
    * 그런데 생각해보면 무조건 Member에 Team에의 FK가 존재한다. 
    * 그러니까 객체의 Team을 통해 DB의 Member에 있는 FK를 관리해야 한다는 것이다.

이를 코드로 진행해보면 다음과 같다.

* Member코드
```
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
```

Member코드는 자신의 값만을 가진다.
다만 DB에서는 FK를 갖게 될 것이다.

---

* Team코드
```
@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;

    @OneToMany()
    @JoinColumn(name = "TEAM_ID")
    private List<Member> members = new ArrayList<Member>();

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
```

Team코드는 member에 대해 OneToMany로 조인한다.
또 이곳에서 member에 TEAM_ID를 갖는 FK키를 생성시킨다.

---

* JpaMain코드
```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{

            Member member = new Member();
            member.setUsername("member1");

            em.persist(member);

            Team team = new Team();
            team.setName("teamA");
            team.getMembers().add(member);

            em.persist(team);

            tx.commit();

        } catch (Exception e){  // 문제가 발생하면 Transaction rollback 진행
            tx.rollback();
        } finally {  // 로직이 끝나면 무조건 em을 닫아준다.
            em.close();
        }
        // application끝날 때에는 emf를 닫아 준다.
        emf.close();
    }
}
```

작성 후 실행시킨다.

---

![](https://i.imgur.com/xR3Xm99.png)
![](https://i.imgur.com/wwz0mSL.png)
![](https://i.imgur.com/nho1pVX.png)

---

![](https://i.imgur.com/OSQleSF.png)

결과를 보면 DB에 값이 제대로 들어갔음을 확인 가능하다.
그런데 Console창을 확인해 보면

1. Member값 insert
2. Team값 insert
3. Member값 update

의 순서로 진행된다.
이는 처음에는 Member에 값을 알수 없으므로 그냥 들어가고 Team이 저장된 후에 다대일에서 다시 update를 치는것이다.

성능상에 불이익이 있다.
또 로직이 뒤죽박죽 이루어져서 유지보수가 힘들수도 있다.

그래서 보통은 다대일을 기준으로 진행하다가 필요할 때 다대일+일대다로 양방향을 추가시키는쪽으로 가는것이 좋다.

> 정리

* 일대다 단방향은 일이 연관관계의 주인이다.
* 다만 항상 '다'쪽에 FK가 있다.
* 객체와 테이블 간의 차이때문에 반대편 테이블의 외래 키를 관리하게 되는 구조를 취하게 된다.
* @JoinColumn을 반드시 사용해야 한다. 그렇지 않으면 조인 테이블 방식을 사용하게 된다.
    * 뭔가 새로운 테이블을 하나 만들어서 Join용으로 사용하게 된다.
* 일대다 단방향 쓰기보다는 다대일 양방향 매핑을 사용하자.
    * 그냥 일대다 단방향 안쓰는게 나음.
* 참고로 일대다 양방향 매핑도 있기는 하다(공식적으로 존재는하지 않지만 쓸수는 있다)
    * 이거 쓸바에 다대일 양방향을 쓰자.

#### 일대일 관계
* 일대일 관계는 반대도 일대일
* 주 테이블이나 대상 테이블 중에 외래키 선택 가능
* 외래 키에 DB 유니크 제약조건 추가

거의 다대일 방식과 유사하다 생각하면 된다.
단방향의 경우 한쪽에서 Join을 걸고, 양방향의 경우 연관관계 주인의 반대쪽에서는 @OneToOne 어노테이션에서 mappedBy를 걸어주면 된다.

* 근데 만약에 외래키가 반대쪽에 있다면(마치 일대다처럼)?
    * 아예 이거는 단방향에서는 지원 자체가 안된다.
    * 양방향에서는 구현 가능하다...만 그냥 외래키 있는쪽을 연관관계 주인으로 설정하는 것이다.
  
> 정리

* 주 테이블에 외래 키 존재
    * 주 테이블에 FK를 두고 대상 테이블을 찾는다.
    * 객체지향 개발자가 선호하는 방식
    * JPA매핑이 편리하다.
    * 장점은 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 바로 확인이 가능하다.
    * 단점은 값이 없으면 외래키에 null이 허용된다.
* 대상 테이블에 외래 키 존재
    * 전통적인 DB개발자가 선호한다.
    * 장점은 주 테이블과 대상 테이블을 일대일 > 일대다로 변경할 때 편리하고, 외래키에 null이 허용되지 않는다.
    * 단점은 무조건 양방향으로 해야하고 지연 로딩으로 설정해도 항상 즉시로딩된다.
        * 주 테이블에서는 대상 테이블쪽에 값이 있는지를 확인해야 하기 때문이다. 그래서 바로 검색을 진행해버림.
            * FK가 대상테이블에만 있으므로 이 FK를 찾기위한 검색 바로 진행.

#### 다대다 관계
* 관계형 DB는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없다.
* **연결 테이블을 추가**하여 일대다, 다대일 관계로 풀어내야 한다.

- 객체는 Collection을 사용하여 객체 2개로 다대다 관계가 가능하다.


```
@ManyToMany를 통해 연결시키고
@JoinTable(name = "테이블이름")
```
을 하여 매핑해 줄 수 있다.

양방향을 하면 반대쪽에
`@ManyToMany(mappedBy = "주인에서의 값")`
으로 가능하다.

**위의 방법은 실무에서 사용하지 않는다.**
이유는
1. 연결 테이블이 단순히 연결만 해주는 것이 아니라 주문시간, 수량같은 데이터가 들어갈 수 있는데, 이 연결 리스트는 그 정보가 들어가지 않는다.
2. 쿼리의 경우도 생각치 못한 쿼리가 나타나게 될 수 있다.

실제로 다대다 관계를 구현하기 위해서는
1. @OneToMany, @ManyToOne을 하나씩 만들어 준다.
2. 연결 테이블을 하나의 엔티티로 승격시켜 준다. 즉 @JoinTable을 사용하지 않는다.
3. 그 연결 테이블에 필요한 정보를 집어넣어 주면 된다.



### 연관관계의 주인
* 테이블은 외래키 하나로 두 테이블이 연관관계를 맺는다.
* 객체는 A>B B>A 이렇게 참조가 2군데가 있다.
* 객체 양방향 관계는 참조가 양쪽에 다 있기 때문에, 외래키를 관리할 장소를 지정해야 한다.
* 즉 연관관계의 주인이란 외래키를 관리하는 참조이다.
    * 연관관계 주인의 반대는 외래키에 영향을 주지 않고, 조회만 가능하다.

