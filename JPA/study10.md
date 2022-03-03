# 인프런 스터디 10
###### tags: `Tag(인프런)`

## 즉시 로딩과 지연 로딩

### 지연 로딩 LAZY
fetchType을 LAZY로 설정하면, 지연로딩의 대상이 된 쪽은 프록시로 가져온다.
그리고 이후에 그 값을 실제로 사용할 때에 초기화가 진행된다.

@ManyToMany, @OneToMany는 기본이 LAZY로 되어 있다.


### 즉시 로딩 EAGER
fetchType을 EAGER로 설정하면, 로딩할 때에 모든 값을 join하여 가져온다.
그렇기 때문에 이렇게 가져오면 프록시로 가져오지 않고 실제 엔티티를 가져온다.

아니면 select 쿼리를 두 번 날려서 가져올수도 있다.

* 다만 실무에서는 **가급적 지연 로딩만 사용**한다.
    * 즉시 로딩을 사용하면 전혀 예상하지 못한 SQL이 나타날 확률이 높다.
        * 그리고 join이 여러개 걸리면 엄청나게 문제가 생긴다...(join은 지수함수로 증가해서)
    * 즉시로딩은 JPQL에서 N+1문제를 일으킨다.
        * JPQL은 JPA와는 다르게 최적화가 되어있는것이 아닌 변환해주는 내용이다.
            * 만약 EAGER로 되어있는 값을 갖는 엔티티를 
            `select m from member m`
              이렇게 검색하면
                * member를 select -> Member에 EAGER로 되어있는 모든 값들을 바로 select ... 
                    * 이렇게 값의 개수만큼 검색해버린다.
            * LAZY로 하면 일단 바로 검색은 안하기 때문에 필요할 때만 select가 나간다.
                * 나중에는 이걸 해결하기 위해 fetch join / Entity Graph를 사용하면 된다.

@OneToOne, @ManyToOne은 기본이 EAGER로 되어 있다.
이를 LAZY로 설정하여 바꾸자

#### 활용
* Member와 Team을 자주 함께 사용 -> 즉시 로딩
* Member와 Order는 가끔 항께 사용 -> 지연 로딩
* Order와 Product는 자주 함께 사용 -> 즉시 로딩

이긴 한데....실무에서는 그냥 싹다 지연로딩 할것. 

---

## 영속성 전이(Cascade)
특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속상태로 만들고 싶을때
부모에서 Cascade하고 나서 영속화 하면 그 자식들도 영속화가 되는것이다.

예를 들어

* Parent Entity생성
```
@Entity
public class Parent {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Child> childList = new ArrayList<>();

    public void addChild(Child child){
        childList.add(child);
        child.setParent(this);
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

이곳은 부모엔티티로, 여러 child엔티티를 자식으로 가진다.
Cascade를 설정하여 부모가 영속화되면 이게 자식에도 바로 적용될 수 있도록 한다.

---

* Child Entity 생성

```
@Entity
public class Child {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Parent parent;

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

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
}
```

---

* JpaMain 수정
```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Child child1 = new Child();
            Child child2 = new Child();

            Parent parent = new Parent();

            parent.addChild(child1);
            parent.addChild(child2);

            em.persist(parent);

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

여기서 사용한 addChild는 부모에서 따로 적용해준 연관관계 편의 메소드이다.
바로 child에도 값을 세팅해주도록.

영속화는 Parent에만 적용되게 된다.
그런데 cascade로 하였기 때문에 바로 자식에도 영속화가 진행될 것이다.

실행해 보면

---

![](https://i.imgur.com/ePdTaSq.png)
![](https://i.imgur.com/bIB70YY.png)

잘 들어갔다.

---


### Cascade 주의사항
* 영속성 전이는 연관관계 매핑과 아무런 관련이 없다!!
* 그냥 하나의 엔티티를 영속화할 때 연관된 엔티티도 함께 영속화하는 것 뿐이다.

### Cascade 종류
    - ALL
        - 모두 적용
    - PERSIST
        - 영속
    - REMOVE
        - 삭제
    - MERGE
        - 병합
    - REFRECH
        - 리프레시
    - DETACH
        - 준영속
주로 이 종류에서 ALL, PERSIST, REMOVE정도가 쓰인다.

그래서 주로 언제 쓰이나??
게시판이나 첨부파일 같은경우에 쓰인다.
그 부모에서만 관리하는 경우.
근데 이제 이 엔티티를 여러 장소에서 관리하는 경우에는 쓰지 말자. 잘못하면 어디서 써야하는데 지워버림

즉 완전히 단일 부모에 종속적인 경우에 사용하도록 하자.

---

## 고아 객체
* 고아 객체 제거
    * 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제함
    * orpanRemoval = true
    * 즉 어떤 부모와의 연관관계가 끊어지면 자식을 바로 없애준다.

코드로 보자면

* Parent

```
@Entity
public class Parent {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Child> childList = new ArrayList<>();

    public void addChild(Child child){
        childList.add(child);
        child.setParent(this);
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

    public List<Child> getChildList() {
        return childList;
    }

    public void setChildList(List<Child> childList) {
        this.childList = childList;
    }
}
```

Parent에서 다음과 같이 orphanRemoval을 true로 설정해 준다.

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
            Child child1 = new Child();
            Child child2 = new Child();

            Parent parent = new Parent();

            parent.addChild(child1);
            parent.addChild(child2);

            em.persist(parent);

            em.flush();
            em.clear();

            Parent findParent = em.find(Parent.class, parent.getId());
            findParent.getChildList().remove(0);

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

이렇게 orphanRemoval을 true로 설정해 준 childList Collection에서 값을 삭제하여 Parent와 해당 Child의 연관이 끊기면 child가 바로 삭제될 것이다.

실행해 보면

---

![](https://i.imgur.com/iOFGoCu.png)
![](https://i.imgur.com/KVEJfuT.png)

잘 지워진 것을 확인 가능하다.

---

그리고 당연하겠지만 부모 전체가 삭제되면 자식도 삭제된다.
이는 마치 CascadeType.REMOVE와 비슷하게 동작한다.

---

## 영속성 전이 + 고아객체, 생명주기
CascadeType.ALL + orphanRemoval=true
* 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거
* 두 옵션을 모두 활성화 하면 부모 엔티티를 통해 자식의 생명주기를 관리할 수 있다.
    * 즉 Parent를 통하기만 해도 Child를 관리 가능.
        * **도메인주도설계(DDD)의 Aggregate Root개념을 구현할 때 유용**하다
            * Aggregate Root에서만 리포지토리와 컨텍하고 나머지에서는 리포지토리를 만들지 않는것이 좋다.
