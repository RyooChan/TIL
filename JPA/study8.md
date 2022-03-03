# 인프런 스터디 8
###### tags: `Tag(인프런)`

## 상속관계 매핑
객체는 상속관계가 있지만, 관계형DB는 상속 관계가 없다.
슈퍼타입-서브타입 관계가 객체 상속과 유사하다.

여기서 상속관계 매핑이란 객체의 상속과 구조와 DB의 슈퍼타입 서브타입 관계를 매핑하는 것이다.

* 슈퍼타입-서브타입 논리 모델을 실제 물리 모델로 구현하는 방법
    * 각각 테이블로 변환 -> 조인 전략
    * 통합 테이블로 변환 -> 단일 테이블 전략
    * 서브타입 테비을로 변환 -> 구현 클래스마다 테이블 전략

![](https://i.imgur.com/1FbS1hP.png)

먼저 다음과 같은 논리 모델이 있다고 가정한다.

### 조인 전략
Item이라는 테이블을 만든 후, ALBUM MOVIE BOOK 테이블을 각각 만들어 준다.
그리고 값을 넣을 때 join을 통해 값을 넣어주게 한다.

![](https://i.imgur.com/XIFyRNx.png)

이런 식으로 값들을 나누어서 만들어 두고 각각 필요한 곳에 insert한다. join은 ITEM의 PK를 각각 테이블에서 PK이자 FK로 구성하여 진행한다.
그리고 조인 도중 상태 테이블 구분을 위해 DTYPE을 만들어 준다.

코드 작성하기

* Item 클래스 생성

```
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 조인 전략으로 설정하기
@DiscriminatorColumn
public abstract class Item {

    @Id @GeneratedValue
    private Long id;

    private String name;
    private int price;

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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
```

abstract로 만들어 준 이유는 해당 Item은 단일로 사용되는 것이 아니라 슈퍼타입으로만 쓰이기 때문이다.

Inheritance의 전략을 InheritanceType.JOINED로 하면 join 전략으로 진행한다.
@DiscriminatorColumn 를 사용하여 DTYPE의 구현이 가능하다.
-> 추가로 만약 DTYPE에서 들어가는 Value를 설정하고 싶으면 자식 클래스에서 @DiscriminatorValue("설정할이름") 를 통해 설정 가능하다.

---

* Album클래스 생성

```
@Entity
public class Album extends Item{
    private String artist;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
```

---

* Movie클래스 생성

```
@Entity
public class Movie extends Item {
    private String director;
    private String actor;

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }
}
```

---

* Book클래스 생성

```

@Entity
public class Book extends Item{
    private String author;
    private String isbn;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
```

---

* JpaMain 작성

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{

            Movie movie = new Movie();
            movie.setDirector("aaaa");
            movie.setActor("bbbb");
            movie.setName("바함사");
            movie.setPrice(10000);

            em.persist(movie);

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

![](https://i.imgur.com/s7fIH8W.png)
![](https://i.imgur.com/5JW2Lf8.png)


원하는 구성이 제대로 이루어졌음을 확인 가능하다.

이제 이를 조회해보면

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

            Movie movie = new Movie();
            movie.setDirector("aaaa");
            movie.setActor("bbbb");
            movie.setName("바함사");
            movie.setPrice(10000);

            em.persist(movie);

            em.flush();
            em.clear();

            Movie findMovie = em.find(Movie.class, movie.getId());
            System.out.println("findMovie" + findMovie);

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

이렇게 캐시를 지우고 값을 보낸 뒤 확인해 보면

![](https://i.imgur.com/puNO39n.png)


이렇게 join하여 값을 가져오게 되는 것을 확인할 수 있다.

* 장점
    * 데이터가 정규화되어 들어간다.
    * 외래키 참조 무결성 제약조건의 활용이 가능하다.
    * 저장공간을 효율적으로 관리 가능하다.
* 단점
    * 조회시 조인을 많이 사용하여 성능이 저하된다.
    * 조회 쿼리가 복잡해진다.
    * 데이터 저장시 Insert SQL이 2번 호출된다


### 단일 테이블 전략
![](https://i.imgur.com/tvTuvYa.png)

하나의 테이블에서 싹다 처리한다.

이 전략으로 바꾸기 위해서는 위의 코드에서 Item에 있는 전략을 바꾸어 주기만 하면 된다.

* Item 클래스에서
`@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`

으로 변경하고 실행해 주면

![](https://i.imgur.com/2F7D0VP.png)

Movie, Book, Album과 같은 테이블들은 따로 생성되지 않고, 모든 값들을 가진 SINGLE_TABLE하나가 생성된다.

![](https://i.imgur.com/bku9y6M.png)
![](https://i.imgur.com/rUcqjph.png)

insert와 select도 한번에 진행된다.

참고로 SINGLE_TABLE전략은 @DiscriminatorColumn 코드가 따로 없어도 알아서 DTYPE을 구현해 준다.
이게 없으면 각각의 차이를 알 수 없기 때문.

여기서 뭔가 이상한점이 있다.
DB를 확인해 보면 ITEM이라는 Table에 모든 값들이 존재하고 이를 상속받는 테이블들은 실제로 존재하지 않는다.
근데 JpaMain코드에는 각각의 객체들은 존재하고, 이를 통해 값을 세팅해주고 있다.

이게 JPA의 장점 중 하나인데, 전략만 바꾸고 코드를 바꿀 필요 없이 바로 변경해준다.

* 장점
    * 조인이 없어서 조회 성능이 빠르다.
    * 조희 쿼리가 단순하다.
* 단점
    * 자식 엔티티가 매핑한 컬럼은 모두 null허용
        * 무결성에 문제가 생길 수 있다.
    * 단일 테이블에 모든것을 저장해서 테이블이 커질 수 있다.
    * 상황에 따라 조회 성능이 떨어질 수도 있다.


### 구현 클래스마다 테이블 전략
![](https://i.imgur.com/IjC5Zjz.png)

테이블을 구현할 때 각각의 테이블이 모든 정보를 가지고 있도록 한다.

* Item 클래스에서
`@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)`

로 설정해 주면 된다.
위의 단일 테이블과는 반대로 여기서는 ITEM 테이블이 구현되지 않고, Movie Book Album과 같은 테이블들이 각각의 값을 모두 가지고 구현되게 된다.

![Uploading file..._7wfrnu0l4]()
![](https://i.imgur.com/nmBH932.png)

이런 결과를 갖게 될 것이다.

이 전략은 큰 결점을 갖고 있는데 만약 존재하지 않는 Item을 통해 검색을 진행하는 경우
UNION을 통해 모든 Movie Book Album을 통합시켜서 거기서 검색을 진행하게 된다.
이 경우 매우 비효율적이고 복잡해진다.
-> 이 전략은 그냥 쓰지 말자 

* 장점
    * 서브 타입을 명활하게 구분해서 처리할 때 효과적
        * insert같은거 할때는 효과적이다.
    * not null 제약조건의 사용이 가능하다.
* 단점
    * 여러 자식 테이블을 함께 조회할 때 성능이 느림(UNION SQL)
    * 자식 테이블을 통합해서 쿼리하기 어렵다.

---

## @MappedSuperclass

* 상속관계 매핑이 아니다!
* 엔티티X, 테이블과 매핑X
* 부모 클래스를 상속받는 자식 클래스에 매핑 정보만을 제공해 준다.
* 조회나 검색이 불가능하다.
* 어차피 생성되지도 않으니 추상 클래스를 사용하자!
* 참고로 Entity클래스는 이 클래스나 @Entity클래스만 상속 가능하다.

![](https://i.imgur.com/WUzkVFt.png)

DB는 관계 없이 객체 입장에서 공통되는 것들을 그냥 상속받아 쓰고싶을 때 사용하는 것이다.
class 상속을 생각하면 될 것 같다.

구현해 보자면

* BaseEntity 생성
```
@MappedSuperclass       // 매핑 정보만 받는 superclass가 된다.
public abstract class BaseEntity {

    private String createBy;
    private LocalDateTime createDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
```

@MappedSuperclass 를 사용하여 이 클래스는 단순히 superclass로 기능하게끔 한다.

---

* Member클래스와 Team클래스에서 이 BaseEntity를 extends해준다.

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

            Member member = new Member();
            member.setUsername("user1");
            member.setCreateBy("kim");
            member.setCreateDate(LocalDateTime.now());

            em.persist(member);

            em.flush();
            em.clear();

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

이렇게 실행해 주면

![](https://i.imgur.com/HVqpF3h.png)
![](https://i.imgur.com/8SQD18x.png)

변환된 DB가 생성되고 
![](https://i.imgur.com/t7jCiXU.png)
![](https://i.imgur.com/dUeV8VY.png)

값도 잘 들어가는 것이 확인된다.

---
