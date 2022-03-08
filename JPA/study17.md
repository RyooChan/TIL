# 인프런 스터디 17
###### tags: `Tag(인프런)`

## JPQL 다형성 쿼리

![](https://i.imgur.com/hewXxFk.png)
여기서 조회 대상을 특정 자식으로 한정

---

### TYPE
예를 들어 Book, Movie만을 찾으려 한다면
[JPQL]
`select i from Item i where type(i) IN (Book, Movie)`

[SQL]
`select i from i where i.DTYPE in ('B', 'M')`

---

### TREAT
부모인 item과 자식 Book이 있다.

[JPQL]
`select i from Item i where treat(i as Book).auther = 'kim'`

[SQL]
`select i.* from Item i wher i.DTYPE = 'B' and i.auther = 'kim'`

---

## 엔티티 직접 사용
JPQL에서 엔티티를 직접 사용하면 SQL에서 해당 엔티티의 기본 키 값을 사용한다.

[JQPL]
`select count(m.id) from Member m //엔티티 아이디를 사용`
`select (m) from Member m // 엔티티 직접 사용`

[SQL] 둘 다 같은 SQL실행됨.
`select count (m.id) as cnt from Member m`

> 그러니까 Entity를 구분하는것 자체가 PK이기 때문에 Entity를 바로 넘기면 PK를 통해 조작한다.

즉 엔티티를 파라미터로 전달하거나

* JpaMain

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            String query = "select m From Member m where m = :member";
            Member findMember = em.createQuery(query, Member.class).setParameter("member", member1).getSingleResult();

            System.out.println("findMember = " + findMember);

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

![](https://i.imgur.com/wgyt0GE.png)

---

식별자를 직접 전달하나

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            String query = "select m From Member m where m.id = :memberId";
            Member findMember = em.createQuery(query, Member.class).setParameter("memberId", member1.getId()).getSingleResult();

            System.out.println("findMember = " + findMember);

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

![](https://i.imgur.com/n9cpacB.png)

똑같은 SQL이 실행됨을 확인 가능하다.

---

당연히 외래키 값을 사용해도 똑같이 사용된다.

---

## Named쿼리 - 어노테이션
@NamedQuery 라는 어노테이션을 사용해서 미리 쿼리를 정의해둘 수 있다.
즉
* 미리 정의해서 이름을 부여해두고 사용하는 JPQL
* 정적 쿼리
* 어노테이션, XML에 정의
* 애플리케이션 로딩 시점에 초기화 후 재사용
    * 정적 쿼리이기 때문에 변할 염려가 없어 로딩 시점에 바로 파싱하여 캐시시킨다.
* 애플리케이션 로딩 시점에 쿼리를 검증
    * 컴파일 시점에 바로 에러의 검증이 가능하다.
    * SQL을 바로바로 확인할 수 있다는 큰 장점이 있다.

---

* Member에서 정의하기

```
@Entity
@NamedQuery(
        name = "Member.findByUsername"
        , query = "select m from Member m where m.username = :username"
)
public class Member {
    @Id @GeneratedValue
    private Long id;

    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID")
    private Team team;

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void changeTeam(Team team){
        this.team = team;
        team.getMembers().add(this);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", age=" + age +
                '}';
    }
}
```

---

* JpaMain
```

public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            List<Member> resultList = em.createNamedQuery("Member.findByUsername", Member.class)
                            .setParameter("username", "회원1")
                            .getResultList();

            for(Member member : resultList) System.out.println("member = " + member);

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

---

![](https://i.imgur.com/pApg1tC.png)

이렇게 원하는 결과가 바로 실행된다.
또, 미리 정적으로 선언해둔 쿼리는 컴파일 단계에서 에러를 잡아준다.

---

* Named쿼리 설정은 XML이 항상 우선권을 가진다.
* 애플리케이션 운영 환경에 따라 다른 XML을 배포할 수 있다.

추가로 Spring Data JPA에서 중요한 것이
**Repository(DAO)에서 @Query() 로 정의하는 쿼리가 바로 이 Named쿼리**이다.
따라서 @Query 어노테이션을 사용해서 정의한 쿼리는 문법 오류 발생시 바로 잡을 수 있다.
-> ~~이걸 부를때는 NoName NamedQuery라고 한다고 함.~~

---

## JPQL 벌크 연산
뭉탱이로 Update / Delete 등을 하는 것.

예) 재고가 10개 미만인 모든 상품의 가격을 10% 상승한다.

본래 JPA변경 감지 기능으로 실행하려면 너무 많은 SQL이 실행된다.
1. 재고가 10개 미만인 상품을 리스트로 조회한다.
2. 상품 엔티티의 가격을 10% 증가한다.
3. 트랜잭션 커밋 시점에 변경감지가 동작한다.

이렇게 JPA변경 감지 기능으로 하면 100건에 대해 100개의 Update가 실행된다.
JPA는 보통 벌크보다는 실시간(단건)의 연산에 더 치중되어있기 때문이다.

이걸 한꺼번에 해주는것이 벌크연산이다.

---

예를들어 전체 회원의 나이를 20살로 맞춘다고 한다면


```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            int resultCount = em.createQuery("update Member m set m.age = 20").executeUpdate();

            System.out.println(resultCount);

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

![](https://i.imgur.com/T39SkIv.png)

이렇게, 한꺼번에 되고 변경된 row를 받아올 수 있다.

---

따라서 벌크연산은
* 쿼리 한번으로 여러 테이블 로우 변경(엔티티)
* executeUpdate()의 결과는 영향받은 엔티티의 수를 반환한다.
* UPDATE, DELETE 지원
* 벌크 연산 사용시 자동으로 em.flush가 호출된다.
    * 저것은 DB에만 반영되기 때문에 이전에 저장한 값들은 변화되지 않고 남아있는다!

### 벌크 연산 주의점
* 벌크 연산은 영속성 컨텍스트를 무시하고 DB에 직접 쿼리한다. 여기서 데이터가 꼬이지 않게 하려면
    * 벌크 연산을 먼저 실행
    * Or 벌크 연산 수행 후 영속성 컨텍스트 초기화
        * 벌크 연산이 수행되면 flush가 한번 이루어진다. 따라서 이후 영속성 컨텍스트를 초기화하고 진행해주면 된다.(즉 캐시를 지워 다시 처음부터 찾기)
