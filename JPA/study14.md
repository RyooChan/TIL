# 인프런 스터디 14
###### tags: `Tag(인프런)`

## 프로젝션
* SELECT 절에 조회할 대상을 지정하는 것이다.
* 대상
    * Entity, 임베디드타입, 스칼라타입(숫자, 문자등 기본 데이터타입)
* SELECT m FROM Member m
    * 엔티티 프로젝션
* SELECT m.team FROM member m
    * 엔티티 프로젝션
* SELECT m.address FROM Member m
    * 임베디드 타입 프로젝션
* SELECT m.username, m.age FROM Member m
    * 스칼라 타입 프로젝션
* DISTINCT로 중복 제거 가능

여기서 중요한것은
엔티티 프로젝션 : 영속성 컨텍스트가 관리함
임베디드 타입 프로젝션 : 
스칼라 타입 프로젝션 : 

### 프로젝션 - 여러 값 조회
SELECT m.username, m.age FROM Member m
1. Query타입으로 조회
2. Object[]타입으로 조회
3. new 명령어로 조회

#### Query타입으로 조회
변환 타입이 명확하지 않을 때 사용하므로 여러 값 조회가 가능함.

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

            em.flush();
            em.clear();

            List memberList = em.createQuery("select m.username, m.age from Member as m").getResultList();

            Object o = memberList.get(0);
            Object[] result = (Object[]) o;

            System.out.println("username = " + result[0]);
            System.out.println("age = " + result[1]);

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

![](https://i.imgur.com/jlqeQU2.png)

---

#### Object 타입으로 조회
Typed를 사용하는데 여기서 Type을 Object로 조회한다.

이런 느낌이다.

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

            em.flush();
            em.clear();

            List<Object[]> memberList = em.createQuery("select m.username, m.age from Member as m").getResultList();

            Object[] result = memberList.get(0);

            System.out.println("username = " + result[0]);
            System.out.println("age = " + result[1]);

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

#### new 명령어로 조회
* 단순값을 DTO로 바로 조회

* MemberDTO 생성

```
public class MemberDTO {
    private String username;
    private int age;

    public MemberDTO(String username, int age) {
        this.username = username;
        this.age = age;
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
            Member member = new Member();
            member.setUsername("member1");
            member.setAge(10);
            em.persist(member);

            em.flush();
            em.clear();

            List<MemberDTO> result = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member as m", MemberDTO.class).getResultList();

            MemberDTO memberDTO = result.get(0);
            System.out.println("memberDTO.username = " + memberDTO.getUsername());
            System.out.println("memberDTO.age = " + memberDTO.getAge());

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

JPQL의 내에서 new jpa.MemberDTO 이런 식으로
해당 위치를 가져와 생성자를 호출하듯 만들어낼수 있다.

MemberDTO에서 이미 생성자를 만들어 두었기 때문에 이렇게 하면 생성자를 사용해 바로 적용 가능하다.

---

![](https://i.imgur.com/p3vMpAs.png)

잘 나온다.

---

