# 인프런 스터디 11
###### tags: `Tag(인프런)`

## 값 타입
* 기본값 타입
* 임베디드 타입(복합 값 타입)
* 값 타입과 불변 객체
* 값 타입의 비교
* 값 타입 컬렉션

### 기본값 타입
JPA는 데이터 타입을 최상위 레벨로 볼 때 두가지로 분류한다.
* 엔티티 타입
    * @Entity로 정의하는 객체
    * 데이터가 변해도 식별자를 통해 지속해서 **추적이 가능**하다.
* 값 타입
    * int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
    * 식별자가 없고 값만 있으므로 변경시 추적이 불가능하다.
    
#### 값 타입의 분류
* 기본값 타입
    * 자바 기본 타입(int, double)
    * 래퍼 클래스(Integer, Long)
    * String
* 임베디드 타입(enbedded type, 복합 값 타입)
* 컬렉션 타입

##### 기본값 타입
* 생명 주기를 엔티티에 의존한다.
    * 즉 엔티티를 삭제하면 기본값도 삭제된다.
* 값 타입은 공유하면 안된다.
    * 예를 들어 회원 이름 변경시 다른 회원의 이름도 함께 변경되면 안된다.

### 임베디드 타입(복합 값 타입)
* 새로운 값 타입을 직접 정의할 수 있다.
* JPA는 임베디드 타입이라 함
* 주로 기본 값 타일을 모아 만들어서 복합 값 타입이라고도 함
* int, String과 같은 값 타입
    * 그러니까 이것도 엔티티가 아닌 값 타입이라 추적이 안된다.

예를 들어 회원 엔티티가 다음과 같을 때
![](https://i.imgur.com/nCzKHbu.png)

![](https://i.imgur.com/vxKtKdV.png)
이렇게 추상화하여 사용할 수 있다.

여기서 기간 / 주소와 같은 내용을 한꺼번에 묶어 임베디드 타입으로 나타내는 것이다.

* 장점
    * 재사용 가능
    * 높은 응집도를 갖는다.(서로 연관도가 높은 것들로 이루어졌기 때문에)
    * 해당 값 타입만 사용하는 의미있는 메소드를 만들어 낼 수 있다.
        * 예를들어 workDay 내에 period.isWork()와 같이 일하는 기간을 산정하는 타입을 정의 가능
    * 값 타입이기 때문에, 이것을 소유한 엔티티에 생명 주기를 의존한다.

Table입장에서는 임베디드 타입을 사용하여도 딱히 내용이 변하지는 않는다.

---

코드를 통해 적용 내용을 확인하자면

* Member 클래스
```
@Entity
public class Member{

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    // 기간 period
    @Embedded
    private Period workPeriod;

    @Embedded
    private Address homeAddress;

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

    public Period getWorkPeriod() {
        return workPeriod;
    }

    public void setWorkPeriod(Period workPeriod) {
        this.workPeriod = workPeriod;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }
}
```

---

* Period 클래스 생성

```
@Embeddable
public class Period {
    private LocalDateTime startData;
    private LocalDateTime endDate;

    public LocalDateTime getStartData() {
        return startData;
    }

    public void setStartData(LocalDateTime startData) {
        this.startData = startData;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
```

---

* Address 클래스 생성
```
@Embeddable
public class Address {

    private String city;
    private String street;
    private String zipcode;

    public Address(){};

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
}
```

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
            member.setHomeAddress(new Address("city", "street", "100"));
            member.setWorkPeriod(new Period());

            em.persist(member);

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

이렇게 설정하고 실행하면

![](https://i.imgur.com/m9ARf5X.png)
![](https://i.imgur.com/w2Sw3SU.png)

잘 동작하는 것을 확인할 수 있다.

---

추가로 동일한 Enbedded 타입을 사용하여 다른 내용을 표현하고 싶다면
* @AttributeOverrides, @AttributeOverride를 사용하면 된다.

예를 들어 집주소, 회사주소를 각각 매핑하고 싶다면

* Member

```
@Entity
public class Member{

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    // 기간 period
    @Embedded
    private Period workPeriod;

    @Embedded
    private Address homeAddress;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name="city", column=@Column(name = "WORK_CITY"))
                        , @AttributeOverride(name="street", column=@Column(name = "WORK_STREET"))
                        , @AttributeOverride(name="zipcode", column = @Column(name = "WORK_ZIPCODE"))
    })
    private Address workAddress;

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

    public Period getWorkPeriod() {
        return workPeriod;
    }

    public void setWorkPeriod(Period workPeriod) {
        this.workPeriod = workPeriod;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }
}
```

이렇게 해주면 된다.

---

### 값 타입과 불변 객체
값 타입은 객체를 단순하고 안전하게 다룰 수 있어야 한다.

#### 값 타입 공유 참조
* 임베디드 타입같은 값 타입을 여러 엔티티에서 공유하면 위험하다.
![](https://i.imgur.com/7XcbfrW.png)

---

예를 들어
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
            Address address = new Address("city", "street", "10000");

            Member member = new Member();
            member.setUsername("member1");
            member.setHomeAddress(address);
            em.persist(member);

            Member member2 = new Member();
            member2.setUsername("member2");
            member2.setHomeAddress(address);
            em.persist(member2);

            member.getHomeAddress().setCity("newCity");

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

이렇게 실행하면 본래 member에서만 City의 값을 newCity로 변경하려 하는 것인데, 실제로 실행해 보면

![](https://i.imgur.com/iVrdAja.png)
![](https://i.imgur.com/bH56ipu.png)

두 값 모두가 변경되는 것을 확인할 수 있다.

-> 이걸 막기 위해서는 값을 복사해서 사용하도록 한다.
```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Address address = new Address("city", "street", "10000");

            Member member = new Member();
            member.setUsername("member1");
            member.setHomeAddress(address);
            em.persist(member);

            Address copyAddress = new Address(address.getCity(), address.getStreet(), address.getZipcode());
            Member member2 = new Member();
            member2.setUsername("member2");
            member2.setHomeAddress(copyAddress);
            em.persist(member2);

            member.getHomeAddress().setCity("newCity");

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

이런 식으로

---

그 이유는
* 객체 타입의 한계
    * 항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용을 피할 수 있다.
        * 임베디드 타입과 같이 **직접 정의한 값 타입은 자바의 기본타입이 아닌 객체 타입**이다.
    * 자바 기본 타입에 값을 대입하면 값을 복사한다.
        * 객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없다.
    * 객체의 공유 참조는 피할 수 없다.
    
#### 불변 객체
* 위의 부작용을 막기 위해서는 객체 타입을 수정할 수 없게 만들어버리면 된다.
* 그러므로 **값 타입은 불변객체로 설계**해야한다.
    * 불변 객체란 생성 이후 값을 절대 변경할 수 없는 객체이다.
* 즉, 생성자로만 값을 설정하고 수정자를 만들지 않도록 한다.
* 참고로 Integer, String은 자바가 제공하는 대표적인 불변 객체이다.

### 값 타입 비교
값 타입은 인스턴스가 달라도, 그 안에 값이 같으면 같은 것으로 본다.
* 동일성(identity) 비교
    * 인스턴스의 **참조값**을 비교 (==)
* 동등성(equivalence) 비교
    * 인스턴스의 **값**을 비교 (equals())

* 값 타입은 a.equals(b)를 사용하여 동등성 비교를 해야 한다.
* 값 타입의 equals()메소드를 적절히 재정의하여 사용(주로 모든 필드 사용)
    * 추가로 이 equals는 기본이 ==이다. 
        * 그렇기 때문에 재정의해야 하고, 그 코드는 아래와 같다.

---

* Address

```
@Embeddable
public class Address {

    private String city;
    private String street;
    private String zipcode;

    public Address(){};

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(city, address.city) && Objects.equals(street, address.street) && Objects.equals(zipcode, address.zipcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, street, zipcode);
    }
}
```

---

### 값 타입 컬렉션
값 타입을 컬렉션에 담아서 사용하는 것.

이 때 문제는, 이를 RDB에 저장할 때에 Table에 저장할 마땅한 방법이 없다는 것이다.
이를 해결하기위해서는 별도의 Table을 새로 만들어서 받아야 한다.

![](https://i.imgur.com/jLhWihM.png)

이런 식으로 진행.

-> 모든 값들을 묶어서 하나의 PK로 만들어내야 한다. 그렇지 않고 식별자 ID같은 개념을 도입시키면 Entity가 되는 것이다.

살펴보자면

---

* Member코드 수정

```
@Entity
public class Member{

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @Embedded
    private Address homeAddress;

    @ElementCollection
    @CollectionTable(name = "FAVORITE_FOOD", joinColumns = @JoinColumn(name = "MEMBER_ID"))
    @Column(name = "FOOD_NAME")     // Collection의 안에 따로 다른 컬럼이 없는 경우(String만 존재) 사용 가능하다.
    private Set<String> favoriteFoods = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "ADDRESS", joinColumns = @JoinColumn(name = "MEMBER_ID"))
    private List<Address> addressHistory = new ArrayList<>();

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

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }
}
```

@ElementCollection을 사용하여 매핑을 해준다.
@CollectionTable을 사용하면 그 테이블의 이름을 지정해 줄 수 있고, joinColumns를 사용하여 join해 줄 컬럼을 지정한다.

실행해 보면

---

![](https://i.imgur.com/Se9dGIJ.png)
![](https://i.imgur.com/eVrNIXr.png)
![](https://i.imgur.com/2CNCjN1.png)

이렇게 테이블이 생성된다.

---

이러한 값 타입 컬렉션은
* 값 타입을 하나 이상 저장할 때 사용
* DB는 컬렉션을 같은 테이블에 저장할 수 없다.
    * 일대다 개념이기 때문에
* 그렇게 컬렉션을 저장하기 위한 별도의 테이블을 필요로 한다.

---

#### 값타입 컬렉션 저장

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
            member.setUsername("member");
            member.setHomeAddress(new Address("homeCity", "street", "10000"));

            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("초밥");
            member.getFavoriteFoods().add("삼겹살");

            member.getAddressHistory().add(new Address("old1", "street", "10000"));
            member.getAddressHistory().add(new Address("old2", "street", "10000"));

            em.persist(member);

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

이렇게 작성하고 실행하면
![](https://i.imgur.com/edbiu2S.png)

이렇게 한번의 persist로 모든 값들이 만들어지는 것을 확인할 수 있다.

이는 값타입 컬렉션이 스스로 생명주기를 가진 것이 아니라, 주인이 되는 엔티티와 생명주기를 공유하기 때문이다.

따라서 만약 이 주인의 값타입이 변경된다면 아래의 값타입 컬렉션의 값이 변경될 것이다.

마치 cascade / orphanremoval을 켜준것과 비슷한 느낌이다.

---

#### 값타입 컬렉션 조회

이 값타입 컬렉션은 지연 로딩으로 조회된다.

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
            member.setUsername("member");
            member.setHomeAddress(new Address("homeCity", "street", "10000"));

            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("초밥");
            member.getFavoriteFoods().add("삼겹살");

            member.getAddressHistory().add(new Address("old1", "street", "10000"));
            member.getAddressHistory().add(new Address("old2", "street", "10000"));

            em.persist(member);

            em.flush();
            em.clear();

            System.out.println("============== start ===============");
            Member findMember = em.find(Member.class, member.getId());

            List<Address> addressHistory = findMember.getAddressHistory();
            for(Address address : addressHistory) System.out.println("address = " + address.getCity());
            tx.commit();

            Set<String> favoriteFoods = findMember.getFavoriteFoods();
            for(String favoriteFood : favoriteFoods) System.out.println("favoriteFood = " + favoriteFood);

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

이런 식으로 설정하고 실행해 보면

![](https://i.imgur.com/mxWOxel.png)
![](https://i.imgur.com/ZpDcheE.png)

이렇게, 먼저 부모 엔티티를 호출하고 이후에 값을 요구할 때에 따로 조회하는 것을 확인 가능하다.

이는 @ElementCollection의 기본 fetch가 지연 로딩으로 되어있기 때문이다.

---

#### 값타입 컬렉션 수정

먼저 하나의 인자를 갖는 값타입 컬렉션을 수정하려 한다면

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
            member.setUsername("member");
            member.setHomeAddress(new Address("homeCity", "street", "10000"));

            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("초밥");
            member.getFavoriteFoods().add("삼겹살");

            member.getAddressHistory().add(new Address("old1", "street", "10000"));
            member.getAddressHistory().add(new Address("old2", "street", "10000"));

            em.persist(member);

            em.flush();
            em.clear();

            System.out.println("============== start ===============");
            Member findMember = em.find(Member.class, member.getId());

            // 하나의 값타입을 삭제하고 이후에 새로 추가
            // 업데이트 자체가 안됨 이거.
            findMember.getFavoriteFoods().remove("치킨");
            findMember.getFavoriteFoods().add("순살치킨");

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

이렇게 하는 수밖에 없다.
값타입의 수정은 불가능하고 새로 추가하고 삭제하여야 한다.

---

![](https://i.imgur.com/gDeQF05.png)
![](https://i.imgur.com/2R9Pr5y.png)

---

그리고 여러 인자를 갖는 컬렉션 값타입을 수정하려 한다면 이전의 값 비교에서 만든 동등성비교/Hash가 중요해진다.
위에서 확인하듯 여기서 수정은 실제로 update를 하는 것이 아니라 해당 객체를 삭제 후 새로 insert하는 것이기 때문이다.

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
            member.setUsername("member");
            member.setHomeAddress(new Address("homeCity", "street", "10000"));

            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("초밥");
            member.getFavoriteFoods().add("삼겹살");

            member.getAddressHistory().add(new Address("old1", "street", "10000"));
            member.getAddressHistory().add(new Address("old2", "street", "10000"));

            em.persist(member);

            em.flush();
            em.clear();

            System.out.println("============== start ===============");
            Member findMember = em.find(Member.class, member.getId());

            // 기본적으로 컬렉션은 대부분 대상을 찾을 때 equals를 사용한다.
            // 완전히 동일한 값을 사용하는 대상을 찾아간다.
            // 따라서 이전에 값 비교에서 만든 equals, hashcode가 제대로 구성되어 있어야 한다.
            findMember.getAddressHistory().remove(new Address("old1", "street", "10000"));
            findMember.getAddressHistory().add(new Address("newCity1", "street", "10000"));

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

이렇게 equals를 사용하여 비교한 뒤, 동등성을 갖는 객체라면 삭제하고 추가해 준다.

---

![](https://i.imgur.com/lgH3l3S.png)
![](https://i.imgur.com/ZvnARAC.png)

이렇게 된다.

그런데 여기서 console에 출력된 내용을 확인해 보면
1. 해당 Member에 존재하는 모든 Address 컬렉션 값 타입 삭제
2. 다시 처음부터 insert

이렇게 된다.

#### 값 타입 컬렉션 제약사항
* 값 타입은 엔티티와 다르게 식별자 개념이 없다.
    * 따라서 값을 변경하면 추적이 어렵다.
* 값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 저장한다.
    * 이것 때문에 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본키를 구성해야한다.
        * 이렇게 하면 null입력, 중복 저장이 안되기 때문이다.

@OrderColumn을 사용하면 해당하는 위치의 PK를 다시 잡아주기 때문에 이걸 사용하면 해결 가능하지만...그냥 쓰지 말자 

#### 값 타입 컬렉션 대안
* 실무에서는 상황에 따라 값 타입 컬렉션 대신 일대다 관계를 고려한다.
    * 실별자가 필요하고, 지속해서 값을 추적/변경해야 한다면 그건 값타입이 아닌 엔티티이다.
* 값 타입 이거는 제한된 상황에서만 쓰는것이 좋다.
    * 예를 들어 1, 2, 3번 옵션을 주고 여기서 선택하도록 할때
