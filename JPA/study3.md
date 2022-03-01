# 인프런 스터디 3
###### tags: `Tag(인프런)`

## 영속성 컨텍스트
* JPA에서 가장 중요한 2가지
    * 객체와 관계형DB의 매핑
    * 영속성 컨텍스트
        * 실제 JPA가 내부에서 어떻게 동작하는지에 관해

### 엔티티매니저 팩토리, 엔티티매니저
![](https://i.imgur.com/DJYYtiq.png)

1. 고객의 요청이 있을 때마다 Factory가 manager를 하나씩 만들어 준다.
2. 이 manager가 DB conn을 통해 DB를 사용한다.

### 영속성 컨텍스트란?
* JPA를 이애하는데 가장 중요한 용어이다.
* **엔티티를 영구 저장하는 환경** 이라는 뜻
* EntityManager.persist(entity)
    * persist가 실제로는 DB에 저장하는 것이 아니라, 영속화 한다는 것이다.


이 EntityManager은 실제로 눈에 보이는 것은 아니고, 논리적인 개념이다.
엔티티 매니저를 통해 영속성 컨텍스트에 접근한다.

> 엔티티 매니저와 영속성 컨텍스트가 1:1로 매핑되어 있다.
![](https://i.imgur.com/Z40jmZ2.png)

### 엔티티의 생명주기
* 비영속 (new/transient)
    * 영속성 컨텍스트와 전혀 관계가 없는 새로운 상태
* 영속(managed)
    * 영속성 컨텍스트에 관리되는 상태
* 준영속 (detached)
    * 영속성 컨텍스트에 저장되었다가 분리된 상태 
* 삭제 (removed)
    * 삭제된 상태

#### 비영속
![](https://i.imgur.com/rLIHa0u.png)
고냥 세팅만 해둔 상태이다.(걍 객체만 생성한거다)

#### 영속
![](https://i.imgur.com/mQsSPl2.png)
객체 생성 후, entitymanager에 persist를 사용해 집어넣고 나면 영속 상태가 된다.
이는 entitymanager 안에 member가 들어갔다는 것이다.

## 영속성 컨텍스트의 이점
* 1차 캐시
* 동일성 보장
* 트랜잭션을 지원하는 쓰기 지연(tansactional write-behind)
* 변경 감지(Dirty Checking)
* 지연 로딩(Lazy Loading)

### 엔티티 조회, 1차 캐시
미리 검색했거나, 저장해 둔 값을 캐싱해 두고 이후 같은 값을 검색하면 이걸 가져와서 사용한다.
약간의 이득을 주지만, 실질 큰 도움은 되지 않는다. (1차 캐시는 하나의 Transaction내에서만 이루어 지기 때문이다.)

코드를 통해 확인해 보자면
```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            // 비영속
            Member member = new Member();
            member.setId(100L);
            member.setName("HelloJPA");

            // 영속
            em.persist(member);

            Member findMember = em.find(Member.class, 100L);
            System.out.println(findMember.getName());

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

![](https://i.imgur.com/KJYaZno.png)

이와 같이, 저 100L의 값은 아직 DataBase에 insert되지 않은 상태인데, 저 tx의 내에서 동작하게 되면 바로 값을 가져오게 된다.
즉, DB를 통하지 않고, 이미 객체 값이 있다면 바로 캐시를 통해 이를 가져오는 것이다.

혹은 select만을 진행할 때에도

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            // 비영속
//            Member member = new Member();
//            member.setId(100L);
//            member.setName("HelloJPA");

            // 영속
//            em.persist(member);

            Member findMember1 = em.find(Member.class, 100L);
            Member findMember2 = em.find(Member.class, 100L);
            Member findMember3 = em.find(Member.class, 100L);
            System.out.println(findMember1.getName());
            System.out.println(findMember2.getName());
            System.out.println(findMember3.getName());

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

![](https://i.imgur.com/uTgk91m.png)

이렇게 하나의 select를 해서 모든 값을 캐싱하여 가져오게 해 준다.

---

### 동일성 보장

JPA는 java의 collection에서 동일 레퍼런스를 통해 데이터를 가져왔을 때에 주소가 같은 것 처럼, 영속 entity의 동일성을 보장시켜 준다.

-> 위의 1차 캐시에서 가져온 개념으로 생각 가능.

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            // 비영속
//            Member member = new Member();
//            member.setId(100L);
//            member.setName("HelloJPA");

            // 영속
//            em.persist(member);

            Member findMember1 = em.find(Member.class, 100L);
            Member findMember2 = em.find(Member.class, 100L);

            System.out.println(findMember1 == findMember2);

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

![](https://i.imgur.com/QjuHyjs.png)

---

### 트랜잭션을 지원하는 쓰기 지연
여러 개의 insert가 존재하면, 이를 가지고 있다가 commit하는 순간에 insert 해 준다.

![](https://i.imgur.com/BzZTF4E.png)

먼저 Member에서 생성자를 만들어 준다.
```
public class Member {

    @Id
    private Long id;
    private String name;

    // 생성자를 하나 만들어 준다.
    public Member(){}
    public Member(Long id, String name) {
        this.id = id;
        this.name = name;
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

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Member member1 = new Member(120L, "A");
            Member member2 = new Member(121L, "B");
            Member member3 = new Member(122L, "C");

            em.persist(member1);
            em.persist(member2);
            em.persist(member3);

            System.out.println("----------------");

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

![](https://i.imgur.com/CEa5zWy.png)

보면 구분선(-------) 을 통해 구분하였는데, insert는 구분선보다 아래에서 이루어졌다.
이는 지연되어 insert되었다는 것이다.(HDBC Batch)

---

### 변경 감지(수정)

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Member member = em.find(Member.class, 120L);
            member.setName("chan~");
            // em.persist(member) 을 해서는 안됨.
            
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


![](https://i.imgur.com/7C6YOMD.png)

이게 되는 이유는
1. JPA는 transaction이 commit되는 시점이 flush()를 실행시킨다
2. 이후 Entity와 스냅샷을 비교한다. (스냅샷이란, 처음 DB에서 값이 들어왔을 때의 상태를 찍어두는 것이라 생각하면 된다.)
3. Transaction시점에서 둘을 비교하여, 값이 바뀌었을 경우 UPDATE쿼리를 쓰기 지연 SQL저장소에 저장시켜 두고, 이후 UPDATE 쿼리가 반영되는 것이다.

![](https://i.imgur.com/Saq5v9x.png)

#### Flush
저기서 사용되는 Flush가 뭘까?

Flush는 이전에 쌓아둔 (Insert, Update, Delete등) 을 DB에 날리는 것
이는 영속성 컨텍스트의 변경 사항과 DB를 맞추어 주는 작업이다.

* 플러시 발생
    * Transaction시행시 자동 수행
    * 변경 감지
    * 수정된 엔티티 쓰기 지연 SQL저장소에 등록
    * 쓰기 지연 SQL저장소의 쿼리를 데이터베이스에 전송함
* 영속성 컨텍스트 플러시 방법
    * em.flush
        * 직접 호출
    * Transaction commit시 자동 호출
    * JPQL쿼리 실행 시 자동 호출
        * 그 이유는 JPQL의 경우 여러 데이터를 호출하는 등의 역할을 하기 때문에 DB에 우리가 만들고 있는 값들도 있어야 한다. 그렇기 때문에 미리 앞의 값들을 저장해두고 실행하게 된다.

> 정리
1. 플러시는 영속성 컨텍스트를 비우지 않는다.
2. 영속성 컨텍스트의 변경 내용을 DB에 동기화한다.
3. commit직전에 동기화시켜준다.


---

### 엔티티 삭제
삭제함ㅇㅇ

---

### 준영속 상태
영속 -> 준영속
영속 상태의 엔티티가 영속성 컨텍스트에서 분리된다.(detached)
영속성 컨텍스트가 제공하는 기능을 사용하지 못한다.

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Member member = em.find(Member.class, 120L);
            member.setName("chan222~");

            // 준영속화 해준다.
            em.detach(member);

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

![](https://i.imgur.com/SC06PCi.png)

보면 원래 DB에서 member를 가져왔을 때, 이는 영속화가 되어 있다.
근데 이를 detach하여 준영속화 해주면, 더이상 JPA에서 해당 객체를 영속화하지 않는다.
따라서, 내용을 바꾸어도 실제로는 영속화되지 않기 때문에 flush의 대상이 되지 않을 것이다.

참고로 em.clear() 를 사용하면 해당 em의 값 전체를 싹다 준영속화해버린다.

영속성 초기화를 해주면 당연히 1차 캐시 이런것의 대상도 되지 않는다. 만약 select를 동일한 객체에 진행하면 쿼리를 다시 호출할 것이다.

* 준영속 상태 만드는 방법
    * em.detach()
        * 해당 하나를 준영속화
    * em.clear()
        * 해당 em 전체를 준영속화
