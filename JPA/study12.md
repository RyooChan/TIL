# 인프런 스터디 12
###### tags: `Tag(인프런)`

## 객체지향 쿼리 언어(JPQL)
JPA는 다양한 쿼리 방법을 지원한다.
* JPQL
    * Java 코드로 짜서, 이를 SQL로 빌드해주는 것
* QueryDSL
    * Java 코드로 짜서, 이를 SQL로 빌드해주는 것
* 네이티브 SQL
    * 완전히 특정DB에 종속되는 쿼리를 사용해야 할 때에, 이를 사용한다.
    * 생쿼리
* JPA Criteria
* JDBC API 직접사용, MyBatis, SpringJdbcTemplate등등...

### JPQL
* EntityManager.find()
* 객체 그래프 탐색(a.getB().getC()) 등등..

만약 여기서 나이가 18세 이상인 데이터를 가져오고 싶다면?

#### JPQL의 등장 배경
- JPA를 사용하면 엔티티 중심으로 개발한다.
- 문제는 검색 쿼리이다.
- 검색을 할 때에도 Table이 아닌, Entity대상으로 검색해야한다.
- 모든 DB데이터를 객체로 변환해서 검색하는것은 실질 불가능하다.
- 그렇기 때문에 애플리케이션이 필요한 데이터만 DB에서 볼러오려면 결국 검색 조건이 포함된 SQL이 필요할 것이다.

#### JPQL 특징
* JPA는 SQL을 추상화한 JPQL이라는 **객체지향쿼리언어**를 제공한다.
* SQL과 문법이 유사하다.
* 엔티티 객체를 대상으로 쿼리한다.
    * 이는 DB Table을 대상으로 쿼리하는 SQL과의 가장 큰 차이점이다.
* SQL을 추상화하여 특정 DB SQL에 의존하지 않는다.

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

            List<Member> result = em.createQuery("select m From Member m where m.username like '%kim%'"
                        , Member.class)
                        .getResultList();

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

예를 들어 다음과 같은 코드를 실행하면
![](https://i.imgur.com/BNseTd0.png)

이렇게 번역되어 실행되는데, 이 process는
1. Entity를 대상으로 쿼리하면
2. 이 Entity에의 매핑 정보를 읽어
3. 적절한 SQL을 만들어낸다.

---

### Criteria
기존의 JPQL은 동적 쿼리의 생성이 어렵다.
예를 들어 값이 null이 아닌 경우 where문 실행과 같은 경우

if(값 != null) String += "where ~~~~ "
이렇게 해야한다.

이런 동적 쿼리의 해결을 위한 방법이다.

코드를 예를 들어

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

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Member> query = cb.createQuery(Member.class);

            Root<Member> m = query.from(Member.class);

            CriteriaQuery<Member> cq = query.select(m).where(cb.equal(m.get("username"), "kim"));
            
            List<Member> resultList = em.createQuery(cq).getResultList();

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

이렇게 하면 Criteria가 자바 문법처럼 확인하도록 나타내 준다.

![](https://i.imgur.com/fIeONXt.png)

이런 식으로

---

이렇게 Criteria로 사용하면 
* 문자가 아닌 자바코드로 JPQL을 작성할 수 있다.
* 쿼리 생성 도중 컴파일 오류를 바로 잡아줌
* 동적 쿼리 생성시 기존의 JPA보다 편하게 생성 가능하다.
* 다만 이게 사실 뭔가 쿼리같은 느낌이 들지가 않는다...
    * 그렇기 때문에 유지보수가 어려워 실무에서는 잘 사용하지 않는다.
        * 거의 반쯤 망한 방법이라 함.

이 Criteria대신에 훨씬 좋은 방법이 **QueryDSL**이다.

---

### QueryDSL
오픈소스임.
* 문자가 아닌 자바코드로 JPQL을 작성 가능하다.
* JPQL빌더 역할
* 컴파일 시점에 문법 오류를 찾을 수 있다.
* 동적쿼리 작성이 편리하다.
* 단순하고 쉽다.
* 실무에서 사용할때는 이걸 쓰자

---

### Native SQL
* JPA가 제공하는 SQL을 직접 사용하느 기능
* JPQL로 해결할 수 없는 특정 DB에 의존적인 기능
    * CONNECT BY 등등..
* 생쿼리를 집어넣으면 된다.

---

## JDBC직접사용, SpringJcbcTemplate 등
* JPA를 사용하면서 JDBC커넥션을 직접 사용하거나, 스프링 JdbcTemplate, 마이바티스 등을 함께 사용 가능하다.
* 단 이를 사용하면 JPA와 실질적인 관련은 없는 것이다.
    * 따라서 영속성 컨텍스트 사용 도중 적절한 타이밍에 강제로 flush해주어야 한다.
        * flush는 기본적으로 commit할 때나 쿼리를 날릴 때에 동작하게 되는데, 여기서는 우회하여 실행하기 때문에 이것이 동작하지 않기 때문

---

간단하게 사용하자면
1. JPQL
2. QueryDSL

이 두개를 선택하여 사용하도록 하자

---
