# 인프런 스터디 9
###### tags: `Tag(인프런)`

## 프록시
JPA에서는 em.find()말고도 em.getReference()라는 메소드를 제공한다.

이 em.getReference()는 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체를 조회한다.
-> 즉 DB에 쿼리가 나가지 않은 상태로 객체를 조회한다.

코드를 통해 확인해 보자면

* Member클래스에서 필요없는거 없애기

```
@Entity
public class Member extends BaseEntity {

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

---

* Locker 클래스 삭제하기

---

* JpaMain 클래스 변경

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
            member.setUsername("hello");

            em.persist(member);

            em.flush();
            em.clear();

            Member findMember = em.getReference(Member.class, member.getId());

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

이렇게 하면 findMember은 getReference를 실행하지만, 실제로 이게 쿼리를 조회하지는 않는다.

![](https://i.imgur.com/l9sgFz8.png)

이런 식으로 insert만 하고 종료됨을 확인 가능하다.

---

* JpaMain 변경

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
            member.setUsername("hello");

            em.persist(member);

            em.flush();
            em.clear();

            Member findMember = em.getReference(Member.class, member.getId());

            System.out.println(findMember.getId());
            System.out.println(findMember.getUsername());

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

이렇게 처음 설정해준 Id외의 Username값을 가져오게 되면

![](https://i.imgur.com/3sR1c4T.png)

그 Username을 가져오려 할 때 select 쿼리가 날라가는 것을 확인할 수 있다.

---

저 getReference로 만들어지는 클래스는 ProxyClass라는 가짜 클래스이다.

### 프록시의 특징
* 실제 클래스를 상속받아 만들어진다.
* 실제 클래스와 겉모양이 같다.
* 사용자 입장에서는 이제 진짜 객체인지 프록시 객체인지 굳이 구분 없이 사용하면 된다.(이론상)
* 프록시 객체는 실제 객체의 참조를 보관한다.
    * 그렇게 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드를 호출한다.
    ![](https://i.imgur.com/fWPeRQt.png)
    1. 프록시 객체 가져오기
    2. getName으로 이 이름 가져오기
    3. 프록시 객체에는 이 getName이 없다.
    4. 영속성 컨텍스트에 초기화 요청함.(진짜 값을 만들어내는 것을 초기화라 한다.)
    5. DB를 조회해서 실제 엔티티를 생성하고, 여기에 target을 이용하여 연결해준다.
    6. 프록시 객체에서 target으로 실제 entity의 값을 가져와서 전달해준다.
* 프록시 객체는 처음 사용할 때에 한 번만 초기화해준다.
* 프록시 객체를 초기화 하는것이, 실제 엔티티로 바뀌는 것은 아니다. 단지 실제 엔티티에 접근 가능하도록 하는 것이다.
* 프록시 객체는 원본 엔티티를 상속받는다.
    * **타입 체크시 주의해야한다.** (==비교 실패, 대신 instance of를 사용할것.)
        * 하나는 프록시에서 하나는 entity에서 받아오면 다를수 있으니
            * 근데 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해도 entity가 반환됨.
                * 이미 영속성 컨텍스트가 있는데 굳이 새거를 만들 필요가 없기도 하고
                * JPA가 한 트랜잭션 내에서 동작하는 것에 대해 == 동일성을 보장해 준다. 그를 위해 이미 있는 것에 대해 만들어지면 동일 엔티티로 만들어 주는 것이다.
            * 위와는 반대로 em.getReference()를 통해 프록시를 만들어내고, 이후 em.find()를 진행하게 되면 em.find()로 만들어진 것이 엔티티가 아니라 프록시가 된다.
                * 이 또한 위에서와 마찬가지로 JPA에서 == 비교를 맞추어 주기 위해 엔티티를 프록시로 만들어 주게 되기 때문이다.
* 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일때 프록시 사용하면 문제가 발생한다.(예외터짐)
    * detach하거나 close하거나 clear하거나~

### 프록시 확인
* 프록시 인스턴스의 초기화 여부 확인
    * (emf에서).PersistenceUnitUtil.isLoaded(Object entity)
        * 그니까 상속받은 엔티티가 있는지를 확인해주는거
* 프록시 클래스 확인
    * entity.getClass()
        * 그러면 클래스 타입(HibernateProxy인지 이런거)이 나온다.
* 프록시 강제 초기화
    * Hibernate.initialize(entity)
        * 강제로 알아서 초기화됨.
            * 참고로 JPA표준에는 강제 초기화 없으므로 걍 호출하면 초기화 됨.

---
