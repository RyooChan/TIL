# 인프런 스터디 13
###### tags: `Tag(인프런)`

## JPQL(Java Persistence Query Language)

### JPQL 기본 문법과 기능
* JPQL은 객체지향 쿼리 언어이다.
    * 테이블 대상이 아닌, 엔티티 객체를 대상으로 쿼리한다.
* SQL을 추상화해서 특정 DB sql에 의존하지 않는다.
* 결국 SQL로 변환된다.

#### JPQL 문법
* select m from Member as m where m.age > 19
    * 엔티티와 속성은 대소문자를 구분한다(Member, age)
    * JPQL키워드는 대소문자를 구분하지 않는다.(select, from, where ... )
    * 엔티티 이름을 사용한다(테이블이름이 아니라)
    * 별칭은 필수이다.(m)

##### TypeQuery, Query
* TypeQuery
    * 반환 타입이 명확할 때 사용
* Query
    * 반환 타입이 명확하지 않을 때 사용

예를 들어

* Member

```
@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;

    private String username;
    private int age;

    @ManyToOne
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
}
```

이런 Member 클래스에서

---

```
* JpaMain

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
            member.setAge(10);
            em.persist(member);

            TypedQuery<Member> query = em.createQuery("select m from Member as m", Member.class);
            Query query2 = em.createQuery("select m.username, m.age from Member as m");

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

TypedQuery를 통해 쿼리를 돌렸을 때 명확한 타입을 알고있으면 그 타입을 뒤에 Member.class 이런식으로 매핑해주고, 받을 수 있다.

명확한 타입을 모르는 경우는 Query를 사용한다.

---

##### 결과 조회 API
* query.getResultList()
    * 결과가 하나 이상일 때, 리스트 반환
        * **결과가 없으면 빈 리스트 반환**
* query.getSingleResult()
    * 결과가 정확히 하나, 단일 객체 반환
        * 결과가 없으면 NoResultException에러가 터지고
        * 결과가 둘 이상이면 NonUniqueResultException에러가 터진다.
    * 값이 정확히 하나가 있다고 보장될 때 사용한다.
    * 추가로 Spring Data JPA에서는 null이나 Exception을 반환시켜준다.

---

### 파라미터 바인딩

이런 식으로 사용한다.

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
            Member member = new Member();
            member.setUsername("member1");
            member.setAge(10);
            em.persist(member);

            TypedQuery<Member> query = em.createQuery("select m from Member as m where m.username=:username", Member.class);
            query.setParameter("username", "member1");

            Member singleResult = query.getSingleResult();
            System.out.println(singleResult.getAge());

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

![](https://i.imgur.com/AwNy6Ie.png)

---

주로 이런 식으로 연결해서 사용해준다.

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
            member.setAge(10);
            em.persist(member);

            Member singleResult = em.createQuery("select m from Member as m where m.username=:username", Member.class)
                    .setParameter("username", "member1")
                    .getSingleResult();
            
            System.out.println(singleResult.getAge());

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

참고로 파라미터 바인딩은 위치 기반으로도 찾아올 수 있지만, 이는 쓰지 않는다.
-> 위치는 변경될 수도 있기 때문이다.

---

