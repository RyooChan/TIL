# Spring Data JPA
###### tags: `Tag(인프런2)`

## 공통 인터페이스란?
JpaRepository<Type, ID>

![](https://i.imgur.com/L1jUzfy.png)

대부분의 DB에서 동일한 메서드를 사용하여 최적화 가능.

* 제네릭 타입
    * T
        * 엔티티
    * ID
        * 엔티티의 식별자 타입
    * S 
        * 엔티티와 그 자식 타입
* 주요 메서드
    * save(S)
        * 새로운 엔티티는 저장하고, **이미 있는 엔티티는 병합한다.(merge)**
    * delete(T)
        * 엔티티 하나를 삭제한다.
            * EntityManager.remove() 호출
    * findById(ID)
        * 엔티티 하나를 조회한다.
            * EntityManager.find() 호출
    * getOne
        * 엔티티를 프록시로 조회한다.
            * EntityManager.getReference() 호출
    * findAll(...)
        * 모든 엔티티를 조회한다. 정렬이나 페이징 조건을 파라미터로 제공할 수 있다.

이런 메소드들로 공통된 기능은 거의 모두 제공해준다.

---

## 쿼리 메소드

* 쿼리 메소드의 기능 3가지
    * 메소드 이름으로 쿼리 생성
    * 메소드 이름으로 JPA NamedQuery 호출
    * @Query어노테이션을 사용해서 repository interface에 쿼리 직접 정의

### 메소드 이름으로 쿼리 생성
ex) 이름과 나이를 기준으로 select를 진행하는 경우

본래 JPA를 사용하면 JPQL을 작성해야 한다.
하지만 Spring Data JPA에서는 

`List<Member> findByUsernameAndAgeGreaterThan(String username, int age);`

이렇게 이름을 잘 정의해주면 된다.

예를 들어서 위의 이름으로 정의된다면

```

em.createQuery(
                "select m from Member m where m.username = :username and m.age > :age"
                ).setParameter("username", username)
                .setParameter("age", age)
                .getResultList();
```

이런 코드를 완성시켜 주는 것이다.

쿼리 메소드에서 이름을 생성시키는 규칙은

https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation

이곳에서 확인할 수 있다.

추가로, 엔티티가 변경된다면 어플리케이션 실행 단계에서 바로 이 메소드가 에러가 발생하게 된다.
이는 실제로 서비스 동작 전에 문제를 알 수 있기 때문에 큰 장점이 된다.

다만 해당 방식은 조건이 추가될 때 마다 메소드의 이름이 굉장히 길어지게 된다... 
이럴 때에는 다른 방식으로 하는것이 더 좋을 것이다.

### 메소드 이름으로 JPA NamedQuery 호출
실제로 실무에서 사용할일은 별로 없는 기능이다.
유지보수가 상당히 귀찮기 때문.

* 사용할 Entity의 위에서
```
@Entity
@NamedQuery(
        name = "Member.findByUsername"
        , query = "select m from Member m where m.username = :username"
)
public class Member {
```

* Spring Data JPA를 extends받는 Repository

```
@Query(name = "Member.findByUsername")
List<Member>findByUsername(@Param("username") String username);
```

이런 식으로 Entity위에 NamedQuery를 정의하고, Repository에서 call해서 사용하는 것이다.

@Query를 사용하여 해당 위치의 NamedQuery를 불러오고, 받아올 이름은 임의로 지정한다.
참고로 받아올 이름이 실제로 있는 NamedQuery와 같다면(해당 내용은 List로 Member를 받아온 뒤 findByUsername Namedquery를 받아오기 때문에 같은 위치, 같은 이름이다.) @Query를 사용하지 않고도 NamedQuery를 받아올 수 있다.


### Repository에 바로 쿼리 정의
위의 @NamedQuery의 장점을 모두 가지면서도 편한 방법이다.
실무에서 자주 쓰이는 방식이다.

```

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    @Query("select m from Member  m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);
}
```

이런 식으로 Repository에서 바로 Quury를 사용하여 정의하고 사용할 수 있다.


이를 한번 테스트해보면

```
@Test
public void 쿼리생성방식테스트() throws Exception {
    //given
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("AAA", 20);
    memberRepository.save(m1);
    memberRepository.save(m2);

    //when

    List<Member> result = memberRepository.findUser("AAA", 10);

    //then
    Assertions.assertThat(result.get(0)).isEqualTo(m1);
}
```

![](https://i.imgur.com/EP4FIsE.png)
`select member0_.member_id as member_i1_0_, member0_.age as age2_0_, member0_.team_id as team_id4_0_, member0_.username as username3_0_ from member member0_ where member0_.username='AAA' and member0_.age=10;`

제대로 된 결과가 나온것이 확인된다.

---


이 방식은
* 메소드 이름으로 쿼리 생성 방식보다
    * 짧은 메소드 이름을 가질 수 있으며 복잡한 JPQL을 바로 넣어줄 수도 있다.
* JPA NamedQuery 호출 방식에 비해
    * 위의 모든 장점을 가지며 여기저기 옮겨가며 확인하지 않기 때문에 유지보수성이 좋다.

#### @Query를 사용해서 값, DTO조회하기

1. 값 타입
```
   @Query("select m.username from Member m")
    List<String> findUsernameList();
```

2. DTO로 받기

```
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();
```

값 타입은 바로 받을 수 있고, DTO로 받으려면 new로 새로 만들어서 진행한다.

---

## Spring Data JPA의 페이징과 정렬

페이징과 정렬 파라미터
* org.springframework.data.domain.Sort
    * 정렬 기능
* org.springframework.data.domain.Pageable
    * 페이징 기능(내부에 Sort 포함)

특별한 반환 타입
* org.springframework.data.domain.Page
    * 추가 count쿼리 결과를 포함하는 페이징
* org.springframework.data.domain.Slice
    * 추가 count쿼리 없이 다음페이지만 확인가능(내부적으로 limit+1 조회)
        * 예를 들어 모바일에서 더보기를 통해 다음페이지만 확인하는 등의 기능이다.
* List(자바 컬렉션)
    * 추가 count쿼리 없이 결과만 반환

---

한번 확인해보자면
### Page의 경우

* MemberRepository에 해당 코드를 추가하고
`Page<Member> findByAge(int age, Pageable pageable);`

---

* Test에서 해당 test를 실행하면
```
@Test
public void 페이징기능들() throws Exception {
    //given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    int age = 10;
    // 가져올페이지, 페이지크기, 정렬조건, 조건 property를 갖는 pageRequest 구현체를 만들어 이를 넘겨주면 된다.
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

    //when

    Page<Member> page = memberRepository.findByAge(10, pageRequest);

    //then
    List<Member> content = page.getContent();
    long totalElements = page.getTotalElements();

    for (Member member : content) {
        System.out.println("member = " + member);
    }

    System.out.println("totalEleents = " + totalElements);
}
```

---

테스트 동작시

`select member0_.member_id as member_i1_0_, member0_.age as age2_0_, member0_.team_id as team_id4_0_, member0_.username as username3_0_ from member member0_ where member0_.age=10 order by member0_.username desc limit 3;`

`select count(member0_.member_id) as col_0_0_ from member member0_ where member0_.age=10;`

![](https://i.imgur.com/pAnfNkk.png)

이런 결과가 확인된다.
* 페이징을 진행한 데이터를 받아오고
* Page로 받는 경우 추가 count쿼리가 제공되기 때문에 이 count를 받아오는 쿼리와
* 전체 값

들이 잘 나오는 것을 확인 가능하다.

참고로

* getTotalElements()
    * 전체 개수
* getNumber()
    * 현재 페이지
* getTotalPages()
    * 전체 페이지 개수
* page.isFirst()
    * 현재 페이지가 첫번째인지
* page.hasNext()
    * 다음 페이지가 있는지

등등 여러 기능을 제공한다. 이는 매우 쉽게 페이징과 관련 기능을 수행할 수 있도록 도와준다.

---

### Slice의 경우

* MemberRepository 코드

`Slice<Member> findByAge(int age, Pageable pageable);`

기존 Page를 Slice로 변경해준다.

---

* Test 코드

```
@Test
public void 페이징기능들() throws Exception {
    //given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    int age = 10;
    // 가져올페이지, 페이지크기, 정렬조건, 조건 property를 갖는 pageRequest 구현체를 만들어 이를 넘겨주면 된다.
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

    //when

    Slice<Member> page = memberRepository.findByAge(10, pageRequest);

    //then
    List<Member> content = page.getContent();

    // Slice는 totalElements가 없다.
//        long totalElements = page.getTotalElements();

    Assertions.assertThat(content.size()).isEqualTo(3);
    Assertions.assertThat(page.getNumber()).isEqualTo(0);
    Assertions.assertThat(page.isFirst()).isTrue();
    Assertions.assertThat(page.hasNext()).isTrue();

}
```

Slice를 사용하는 식으로 변경한다.
Slice는 전체 페이지를 갖지 않는다고 했으니 테스트를 실행해 보면

---

`select member0_.member_id as member_i1_0_, member0_.age as age2_0_, member0_.team_id as team_id4_0_, member0_.username as username3_0_ from member member0_ where member0_.age=10 order by member0_.username desc limit 4;`


![](https://i.imgur.com/53KYegL.png)

따로 count관련 내용 없이 원하는 동작이 잘 되었음이 확인된다.

그리고, 분명 코드에서 size는 3으로 설정했는데, 여기서 limit를 4개를 호출한다.
이 이유로는 Slice를 사용하면 다음 내용을 더보기 가능하도록 하나 더 가져와 주는 것이다.
이는 페이징 방식을 변경할 때에 엄청나게 많은 생산성의 향상을 부여해준다.

---

그리고 실무에서 실제로 문제가 생기는 부분은 TotalCount를 가져올때이다.(이 때 성능이 느려질 확률이 높다.)

예를 들어 join(left outer join)을 실행하는 경우는 굳이 join을 하지 않아도 되는데, 이를 실행하게 되면 성능이 매우 느려질 것이다.

* MemberRepository

```
@Query(value = "select m from Member m left join m.team t")
Page<Member> findByAge(int age, Pageable pageable);
```

이런 식으로 left join이 걸린다고 가정한다면
```

@Test
public void 페이징기능들() throws Exception {
    //given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    int age = 10;
    // 가져올페이지, 페이지크기, 정렬조건, 조건 property를 갖는 pageRequest 구현체를 만들어 이를 넘겨주면 된다.
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

    //when

    Page<Member> page = memberRepository.findByAge(10, pageRequest);

    //then
    List<Member> content = page.getContent();
}
```

---

```
select member0_.member_id as member_i1_0_, member0_.age as age2_0_, member0_.team_id as team_id4_0_, member0_.username as username3_0_ from member member0_ left outer join team team1_ on member0_.team_id=team1_.team_id order by member0_.username desc limit 3;
```

`select count(member0_.member_id) as col_0_0_ from member member0_ left outer join team team1_ on member0_.team_id=team1_.team_id;`

이런 식으로 페이징과 count모두에서 join이 걸리는것을 확인할 수 있다.
그런데 확인해보면 left outer join은 이미 member의 값만을 가질 것이기 때문에 이후의 join은 불필요하다.

---

그래서 이를 분리하는 방법이 있다.

* MemberRepository에서 countQuery용 추가

```
@Query(value = "select m from Member m left join m.team t", countQuery = "select count(m.username) from Member m")
Page<Member> findByAge(int age, Pageable pageable);
```

이런 식으로 countQuery용 쿼리를 따로 만들어서 진행해주면

---

```
select member0_.member_id as member_i1_0_, member0_.age as age2_0_, member0_.team_id as team_id4_0_, member0_.username as username3_0_ from member member0_ left outer join team team1_ on member0_.team_id=team1_.team_id order by member0_.username desc limit 3;
```

```
select count(member0_.username) as col_0_0_ from member member0_;
```

join이 불필요한 count에서는 join이 이루어지지 않음을 확인 가능하다.
필요에 따라 이렇게 쿼리를 바꾸어 가며 진행할 수 있다.

그리고 위의 내용에서 확인할 수 있듯, @Query를 사용한다고 해도 Paging을 사용하면 JPA가 알아서 해당 로직을 추가해서 진행시켜 준다!!

---

### Page를 DTO로 바로 변환
검색할때는 Entity로 가져오는데...내보낼때는 DTO로 내보내야 한다.
이 방법은 매우 간단하다.

```
@Test
public void 페이징기능들() throws Exception {
    //given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    int age = 10;
    // 가져올페이지, 페이지크기, 정렬조건, 조건 property를 갖는 pageRequest 구현체를 만들어 이를 넘겨주면 된다.
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

    //when

    Page<Member> page = memberRepository.findByAge(10, pageRequest);

    // Entity to DTO
    Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

    //then
    List<Member> content = page.getContent();
}
```

---

이렇게,    

`Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));`

이 코드를 사용해 주면 바로 DTO로 변경해서 사용이 가능하다.

---

## 벌크성 수정 쿼리
Update나 Delete 등을 한꺼번에 처리할 때 사용한다.

본래 JPA에서는 이를

```
public int bulkAgePlus(int age){
    int resultCount = em.createQuery("update Member m set m.age = m.age + 1 where m.age >= :age")
            .setParameter("age", age)
            .executeUpdate();
    return resultCount;
}
```

이런 식으로 하는데, Spring Data JPA에서는

```
@Modifying(clearAutomatically = true)
@Query("update  Member m set m.age = m.age + 1 where m.age >= :age")
int bulkAgePlus(@Param("age") int age);
```

이런 코드를 사용한다.
return형태는 변경되는 데이터의 수이다.
@Modifying을 선언하여 기존 JPA에서의 executeUpdate()를 수행해 준다.
이것이 없으면 single update가 수행되거나 에러가 발생한다.

### 벌크연상 주의점
* 벌크 연산은 영속성 컨텍스트를 무시하고 DB에 직접 쿼리한다. 여기서 데이터가 꼬이지 않게 하려면
    * 벌크 연산을 먼저 실행
    * Or 벌크 연산 수행 후 영속성 컨텍스트 초기화
        * 벌크 연산이 수행되면 flush가 한번 이루어진다. 따라서 이후 영속성 컨텍스트를 초기화하고 진행해주면 된다.(즉 캐시를 지워 다시 처음부터 찾기)

이 영속성 컨텍스트 초기화는 어떻게 할까?
Spring Data JPA에서는 이 초기화 방법을 쉽게 해준다.
위의
@Modifying(clearAutomatically = true)
를 확인해 보면, clearAutomatically = true이면 벌크 연산 수행 후 clear를 자동으로 수행해 준다.
벌크 연산 이후로 관련 데이터 내용을 다시 조작해야 할 일이 생기면 진행해 주면 될 것이다.

---

## EntityGraph
기존에 LAZY로 되어있는 데이터를 가져오게 되면 N+1 에러가 발생하게 된다.
이 때에 fetch join을 사용하면 모든 데이터 값을 한번에 다 가져와서 해결한다.

그런데 Spring Data JPA에서는 매번 귀찮게 @Query를 사용해서 JPQL을 작성하지 않아도 N+1문제를 해결할 수 있도록 도와준다.

EntityGraph를 사용하면 fetch join을 사용한 것 처럼 N+1문제를 해결해 주면서도, 메소드 이름을 사용한 검색이 가능하게 해준다.

### 코드

* Repository 코드 작성

```
@Override
@EntityGraph(attributePaths = {"team"})
List<Member> findAll();
```

findAll()은 해당 repository가 상속받는 기존 데이터이다.
이를 Override한 뒤, @EntityGraph 어노테이션을 사용하여 바로 가져올 엔티티를 작성해준다.

---

* Test Code

```
@Test
public void entityGraph() throws Exception {
    //given
    Team teamA = new Team("teamA");
    Team teamB = new Team("teamB");
    teamRepository.save(teamA);
    teamRepository.save(teamB);

    memberRepository.save(new Member("member1", 10, teamA));
    memberRepository.save(new Member("member2", 20, teamB));

    em.flush();
    em.clear();

    //when
    List<Member> members = memberRepository.findAll();

    //then
    for (Member member : members) {
        member.getTeam().getName();
    }
}
```

---

`select member0_.member_id as member_i1_0_0_, team1_.team_id as team_id1_1_1_, member0_.age as age2_0_0_, member0_.team_id as team_id4_0_0_, member0_.username as username3_0_0_, team1_.name as name2_1_1_ from member member0_ left outer join team team1_ on member0_.team_id=team1_.team_id;`

EntityGraph를 작성해주면, 알아서 전체 데이터를 가져오는 것이 확인된다.

이 EntityGraph는 기본적으로 Left OUTER join을 사용한다.

---

참고로 쿼리가 아주 간단한 경우는 EntityGraph를 사용하고, 쿼리가 복잡해지면 그냥 JPQL로 작성한다고 한다.

---

## JPA Hint
JPA쿼리 힌트(하이버네이트에게 알려주는 힌트)
즉 SQL힌트가 아니라, JPA구현체에게 제공하는 힌트이다.

---

기존의 Dirty Checking(어떤 객체의 변경이 일어난 경우 영속성 컨텍스트로 관리하여 update해준다)에는 비효율적인 동작이 존재한다.

변경을 감지하기 위해서는 하나의 객체에 대해
1. 기존의 데이터
2. 변경된 데이터

모두를 가지고 있어야 가능하다는 것이고, 이는 하나의 동작을 위해 두 개의 객체를 관리하고 있다는 뜻이다.

그렇기 때문에 만약 Dirty Checking을 사용하지 않고, 단순히 조회만을 사용하고 싶다고 할 때에는 이런 기능이 필요하지 않을 것이다.
그것에 대한 최적화는 Hibernate에서는 제공하지만 JPA표준에서는 제공하지 않는다.
이를 사용하기 위해 Hint를 사용한다.

Hint를 사용하면 문자열로 Hibernate에 다양한 요청을 보낼 수 있다. 위에 상황을 일례로

---

* Repository

```
@QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
Member findReadOnlyByUsername(String username);
```

readOnly를 true로 보내면

---

* Test

```
@Test
public void queryHint() throws Exception {
    //given
    Member member1 = new Member("member1", 10);
    memberRepository.save(member1);
    em.flush();
    em.clear();

    //when
    Member findMember = memberRepository.findReadOnlyByUsername(member1.getUsername());

    findMember.setUsername("member2");

    em.flush();

    //then

}
```

---

![](https://i.imgur.com/pd4fH52.png)

Dirty checking이 이루어지지 않고 그대로 Test가 종료되는 것을 확인할 수 있다.
Hibernate에서 읽기 전용으로 최적화하였기 때문이다.

### 결론

실무에서 @QueryHint는 생각보다 많이 쓰이지는 않는다.
왜냐면 Dirty Checking을 위한 스냅샷 등은 성능에 크게 영향을 미치지 않기 때문이다.
-> 실질 성능의 문제는 쿼리 등에서 발생한다.

---

## JPA Lock

repository에서 @Lock 어노테이션을 사용하여 편리하게 Lock을 걸 수 있다.
Lock에 관해서는 따로 설명x

참고로 실시간 트레픽이 많은 서버에서는 Lock사용X

---

## 확장 기능
### 사용자 정의 리포지토리
본래 스프링 데이터 JPA는 인터페이스만 제공하고, 구현체는 스프링이 자동 생성한다.
만약에 여기서 인터페이스의 메서드를 직접 구현하고 싶다면?
예를 들어
* JPA직접 사용(EntityManager)
* 스프링 JDBC Template사용
* MyBatis사용
* 데이터베이스 커넥션 직접 사용
* QueryDsl 등등...

이러한 내용을 적용할 때, 따로 구현체에 만들어두고 사용하는 것이다.
방법은 아래와 같다.

---

* MemberRepositoryCustom interface 생성

```
public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
```

먼저 구현체를 하나 만들어 주고, 사용할 메소드를 정의한다.

---

* MemberRepositoryImpl class 생성

```
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Member> findMemberCustom(){
        return em.createQuery("select m from Member m").getResultList();
    }
}
```

해당 custom을 implemets받아 내용을 작성해줄 Impl class를 작성한다.
추가로 이 이름 규칙은 최종적으로 Custom을 상속받을 class이름에 Impl을 더하는 식으로 진행해 준다.
이렇게 Impl이라 적어주면 Spring Data interface가 알아서 둘을 매칭시켜 준다.

---

* MemberRepository

```
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

]
```

이런 식으로 해당 Custom interface를 받아와서 사용해주면 된다.

---

주로 이런 Custom은 QueryDsl을 받아올 때 사용한다.
QueryDsl을 사용하는 경우가 많아서...

추가로, 항상 이런 방식으로 진행하는것은 아니다.
그냥 임의의 리포지토리를 추가로 만들어 진행할 수 있다.
핵심 비즈니스와 화면에의 출력을 분리하는 방식으로 진행해도 된다.

---

## Auditing
Entity를 생성, 변경할 때 변경한 사람과 시간을 추적하고 싶으면 사용한다.

실무에서 이걸 남겨놓으면 나중에 추적할때 편하다.
반드시 사용해야 한다고 한다.

Spring date JPA에서는 이전에 배웠던 @MappedSuperclass를 활용하면 된다.

---

* BaseEntity 생성

```
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass  // 진짜 상속관계는 아니고, 속성을 아래 테이블로 내려준다.
@Getter
public class BaseEntity {

    @CreatedDate
    @Column(updatable = false) // 괜히 업데이트 못하게
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;
}
```

생성/수정 시간, 유저를 바로 받아오는 superclass를 생성한다.
@MappedSuperclass를 사용하여 상속관계를 바로 매핑해준다.

추가로, username의 경우는 여기서 알 방법이 없기 때문에 @Bean을 통해 정의해준다.

---

* Application부분(메인)

```
@EnableJpaAuditing		// Auditing을 위함(createDate, updateDate)
@SpringBootApplication
public class DataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataJpaApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorProvider(){
		// 해당 내용은 랜덤아이디를 생성하고, 실무에서는 security 등에서 꺼내온 userid를 넣어주면 된다.
		return () -> Optional.of(UUID.randomUUID().toString());
	}
}
```

어플리케이션 동작부에
@EnableJpaAuditing 어노테이션을 사용하여 해당 동작에 대한 Auditing을 실시해 준다.

@Bean에 AUditorAware을 사용하여 유저의 정보를 받는다.
해당 예제에서는 random Id를 생성하였지만, 실무에서는 security에서 받아온 정보를 활용하는 등의 방법을 사용할 것이다.

---

* 해당 엔티티를 상속받을 위치 아무데서나

`public class Member extends BaseEntity {`

상속받아주면 된다.

---

* Test해보면

```
@Test
public void JpaEventBaseEntity() throws Exception {
    //given
    Member member = new Member("member1");
    memberRepository.save(member);  // @prePersist 발생!

    Thread.sleep(100);
    member.setUsername("member2");

    em.flush(); // @preUpdate 발생!
    em.clear();

    //when
    Member findMember = memberRepository.findById(member.getId()).get();

    //then
    System.out.println("findMember.createdDate = " + findMember.getCreatedDate());
    System.out.println("findMember.updatedDate = " + findMember.getLastModifiedDate());
    System.out.println("findMember.createdBy = " + findMember.getCreatedBy());
    System.out.println("findMember.updatedBy = " + findMember.getLastModifiedBy());
}
```

---

findMember.createdDate = 2022-03-17T00:10:48.862415
findMember.updatedDate = 2022-03-17T00:10:49.062879
findMember.createdBy = ee5b897d-3eee-4f25-ab13-722e08d81a95
findMember.updatedBy = c54d1bb7-7fa7-47f5-bd3c-19fa6ec68a68

![](https://i.imgur.com/aLKqQSY.png)


---

잘 나온다.
참고로 현업에서는 보통
생성/수정 시간은 거의 다 필요로 하지만
생성/수정 유저는 필요없을 때가 있을 때도 있다고 한다.

이럴 때에는


```
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass  // 진짜 상속관계는 아니고, 속성을 아래 테이블로 내려준다.
@Getter
public class BaseTimeEntity {
    
    @CreatedDate
    @Column(updatable = false) // 괜히 업데이트 못하게
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
```

```
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass  // 진짜 상속관계는 아니고, 속성을 아래 테이블로 내려준다.
@Getter
public class BaseEntity extends BaseTimeEntity {

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;
}
```

이런 식으로 BaseTimeEntity를 받고, BaseEntity를 이를 상속받은뒤에
날짜만 필요하면 BaseTimeEntity를 상속받고 둘다 필요하면 BaseEntity를 상속받는 식으로 진행한다고 한다.

---

## Web확장 - 도메인 클래스 컨버터

```
@GetMapping("/members/{id}")
public String findMember(@PathVariable("id") Long id){
    Member member = memberRepository.findById(id).get();
    return member.getUsername();
}

@GetMapping("/members2/{id}")
public String findMember2(@PathVariable("id") Member member){
    return member.getUsername();
}
```

두 코드는 동일하게 동작한다.
Spring data JPA에서 PK를 통해 검색하면 도메인 컨버터가 알아서 동작해서 시행해준다.

그리고 도메인 클래스 컨버터로 엔티티를 파라미터로 받으면, 이 엔티티는 단순조회용으로만 사용해야한다.
이 이유는 트랜잭션이 없는 범위에서 엔티티를 조회해서 Dirty Checking의 범위에 포함되지 않아 DB에 반영되지 않기 때문이다.

그런데 이 기능은 복잡하면 그냥 안쓰는게 낫다고 하고, 실제로 그냥 사용하지 않는것이 나을 것 같다.

---

## Spring boot에서 데이터 업데이트할 때 주의점

본래 save() 메소드는 값이 없는 경우 저장하고, 있는 경우는 merge()로 동작한다.
그렇기 때문에 이를 사용해서도 update가 가능하다.
하지만 merge()에는 단점이 있는데 merge는 DB select를 한번 하게 된다.
그리고 select결과 값이 있으면 update시키기 때문에 한번의 쿼링이 더 행해지게 된다.

그러므로 가급적이면 데이터 변경은 Dirty checking을 통해 진행해야 한다.
merge는 쓰지 말자.

merge()는 실질 update보다는 준영속 상태의 데이터를 영속화할 때에 사용하는것이다.

### 엔티티가 새로운 것인지 구분하는법
위의 내용에서 save()가 새로운 메소드이면 저장 / 존재하는 메소드이면 merge()한다고 했는데, 이 판단 방법이 있다.

* 새로운 엔티티를 판단하는 기본 전략
    * 식별자가 객체일 때 'null'로 판단.
        * 예를 들어 PK가 Long이면 'null'로 파악(식별자가 객체이다)
    * 식별자가 자바 기본 타입일 때 '0'으로 판단.
        * 예를 들어 PK가 long이면 '0'으로 파악(식별자가 자바 기본 타입이다)
    * 'Persistable' 인터페이스를 구현해서 판단 로직 변경 가능.


그런데 만약에 PK를 @GeneratedValue 없이 사용하면 어떨까?
예를 들어

```

@Entity
@Getter
@NoArgsConstructor
public class Item {

    @Id 
    private String id;

    public Item(String id) {
        this.id = id;
    }
}
```

이런 식으로 임의의 String값을 직접 넣어주는 Item을 save()하게 되면 위의 판단 기본 전략에 해당하지 않게 될 것이다.
그래서 save()메소드가 DB에 직접 Item을 select해보고, 여기서 값이 없으면 그때서야 새로운 메소드라고 판단할 것이다.
**즉, 하나의 쿼리가 추가로 실행된다는 것이다.**

그럼 그때 어떡할까??

---

### 'Persistable' 인터페이스를 구현해서 판단 로직 변경 가능.

이전의 코드를 이렇게 바꾼다.
```

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Item implements Persistable<String> {

    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;

    public Item(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew(){
        return createdDate == null;
    }
}
```

1. Persistable<Id타입> 을 implements받는다.
2. 해당 메소드가 새것인지 판단하는 isNew 생성(implements받으면 바로 생성 가능하다.)
3. 이전에 @CreatedDate의 경우 @EntityListners를 통해, 메소드 동작을 감지하여 동작한다 했다.
4. 그렇기에 JPA동작이 진행될 때에 @CreatedDate가 적용될 것이고, 그 전까지 createDate필드는 null일 것이다.
5. isNew에서 해당 createdDate가 현재 null이면 새로운 메소드일 것이다.

@GeneratedValue가 아닌 경우 해당 방식을 사용하면 merge()를 사용하지 않을 수 있다.

---
