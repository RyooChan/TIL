# 인프런 스터디 15
###### tags: `Tag(인프런)`

## 페이징
* JPA는 페이징을 두 개의 API를 사용해서 추상화한다.
* setFirstResult(int startPosition)
    * 조회 시작 위치(0부터 시작)
* setMaxResults(int maxResult)
    * 조회할 데이터 수

실제로 사용해 보면

---

* Member클래스

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

toString을 정의해준다.
여기서 Team은 빼주는데, 양쪽에서 계속 호출하면 무한루프에 빠질 수 있기 때문이다.


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
            for(int i=0; i<100; i++){
                Member member = new Member();
                member.setUsername("member" + i);
                member.setAge(i);
                em.persist(member);
            }

            em.flush();
            em.clear();

            List<Member> result = em.createQuery("select m from Member m order by m.age desc", Member.class)
                            .setFirstResult(1)
                            .setMaxResults(10)
                            .getResultList();

            System.out.println("size = " + result.size());
            for(Member member1 : result) System.out.println("member1 = " + member1);

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

order by ~ desc를 사용하여 나이가 많은 사람 순서대로 가져오게 된다.
여기서 setFirstResult(1)로 설정해 줬다. 시작이 0이니까 하나를 빼고 값을 가져올 것이다.

---

![](https://i.imgur.com/IpmdIA1.png)

이렇게 가져온다.
그리고 본래 페이징이 상당히 귀찮은데...이거를 바로 해준다는 장점이 있다.
예를 들어 Oracle DB를 쓴다면 

![](https://i.imgur.com/HjxJ5JA.png)

이런 느낌이다.

---

## 조인
* 내부 조인
    * SELECT m FROM Member m [INNER] JOIN m.team t
* 외부 조인
    * SELECT m FROM Member m LEFT [OUTER] JOIN m.team t
* 세타 조인
    * select count(m) from Member m, Team t where m.username = t.name

### 내부조인

* Member

```
@Entity
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

* Team

```
@Entity
public class Team {
    @Id @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

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

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
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
            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member");
            member.setAge(10);

            member.setTeam(team);

            em.persist(member);

            em.flush();
            em.clear();

            String query = "select m from Member m inner join m.team t";
            List<Member> result = em.createQuery(query, Member.class).getResultList();

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

위에서 Member을 LAZY로 해 주어야 한다.
그래야 Member을 찾을 때 바로 Team에 대한 검색을 하지 않는다.

---

![](https://i.imgur.com/uRyAww9.png)

잘 가져올 수 있다.

---

### 외부조인

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
            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member");
            member.setAge(10);

            member.setTeam(team);

            em.persist(member);

            em.flush();
            em.clear();

            String query = "select m from Member m left outer join m.team t";
            List<Member> result = em.createQuery(query, Member.class).getResultList();

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

조건만 살짝 바꿔주면

---

![](https://i.imgur.com/JbCq7rt.png)

나온다.

---

### 세타 조인

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
            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member");
            member.setAge(10);

            member.setTeam(team);

            em.persist(member);

            em.flush();
            em.clear();

            String query = "select m from Member m, Team t where m.username = t.name";
            List<Member> result = em.createQuery(query, Member.class).getResultList();

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

![](https://i.imgur.com/ffQIjJ1.png)

---

### Join - ON 절
* ON절을 활용한 조인(JPA 2.1부터 지원)
    * 조인 대상 필터링 가능
        * ex) 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인함
        * `SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'A'`
    * 연관관계 없는 엔티티 외부 조인(하이버네이트 5.1부터)
        * ex) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
        * `SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name`

---

## 서브쿼리
### 서브쿼리 지원함수
* [NOT] EXISTS (subquery) 서브쿼리에 결과가 존재하면 참
    * select m from Member m where exists(select t from m.team t where t.name = '팀A')
* ALL | ANY | SOME (subquery)
    * ALL 모두 만족하면 참
    * ANY, SOME : 조건을 하나라도 만족하면 참
        * select o from Order o where o.orderAmount > ALL (select p.stockAmount from Product p)
        * select m from Member m where m.team = ANY (select t from Team t)
* [NOT] IN (subquery) 서브쿼리 결과 중 하나라도 같은 것이 있으면 참

#### JPA서브쿼리의 한계
* JPA는 WHERE, HAVING절에서만 서브쿼리 사용 가능
* SELECT절도 가능(하이버네이트에서 지원해줌)
* **FROM절의 서브쿼리는 현재 JPQL에서 불가능**
    * 조인으로 풀 수 있으면 풀어서 해결한다. 

## JPQL 타입 표현과 기타식
문자, 숫자 이런걸 어떻게표현할거냐?

* 문자
    * ''
* 숫자
    * L(Long), D(Double), F(Float)
* Boolean
    * TRUE, FALSE
* ENUM
    * 자바의 전체 패키지명을 포함해서 넣는다.
* 엔티티 타입
    * TYPE(m) = Member(상속 관계에서 사용)
        * 상속받은곳의 DTYPE을 가진것 가져오도록 한다.
* EXISTS, IN
* AND, OR, NOT
* = > <= ...
* BETWEEN, LIKE, IS NULL 등등...

---

## 조건식(CASE식)

* 기본 CASE식
    * 조건
```
    select
        case when m.age <= 10 then '학생'
             when m.age >= 60 then '경로'
             else '일반'
         end
     from Member m
```

* 단순 CASE식
    * 매치
```
    select
        case t.name
            when '팀A' then '인센티브110%'
            when '팀B' then '인센티브120%'
            else '인센티브105%'
        end
    from Team t
```

* COALESCE
    * 하나씩 조회해서 null아니면 반환
    * 사용자 이름이 없으면 이름 없는 회원을 반환
        * select COALESCE(m.username, '이름없음') from Member m
* NULLIF
    * 두 값이 같으면 NULL, 다르면 첫번째 값 반환
    * 사용자 이름이 '관리자'면 null, 나머지는 본인의 이름을 반환
        * select NULLIF(m.username, '관리자') from Member m

---

## JPQL 기본 함수
* JPQL이 제공하는 표준 함수
    * CONCAT
        * 두 문자열을 합치는 함수
    * SUBSTRING
        * 문자열 자르기
    * TRIM
        * 공백 제거
    * LOWER, UPPER
        * 대소문자 변경
    * LENGTH
        * 문자 길이
    * LOCATE
        * 해당 문자의 문자 내 위치 찾기
    * ABS, SQRT, MOD
        * 수학 function들
    * SIZE(JPA용도)
        * 해당 collection의 크기 return
    * INDEX(JPA용도)
        * 일반적으로는 사용 X
            * @OrderColumn을 사용할 때에 쓴다.
                * Collection의 위치값을 구할 때 사용.
* 사용자 정의 함수
    * 하이버네이트는 사용전 방언에 추가해야 한다.
        * 새로 Class를 만들어서 진행하며, extends받는 Dialect내의 참조를 참고하여 진행한다.
            * 이후에 persist에 해당Dialect를 등록해주면 된다.
                * 코드를 통해 보자면

- MyH2Dialect 사용

![](https://i.imgur.com/SUqYqXG.png)

```
public class MyH2Dialect extends H2Dialect {
    public MyH2Dialect(){
        registerFunction("group_concat", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
    }
}
```

이곳에 있는 regusterFunction내 코드는 H2Dialect 내에 미리 정의되어 있으므로 사용할 함수를 쓰면 된다.

---

- persistence.xml 코드 변경

![](https://i.imgur.com/BqHovxZ.png)

사용해줄 dialect로 변경해 준다.

---

- JpaMain 변경

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Member member1 = new Member();
            member1.setUsername("member1");
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("member2");
            em.persist(member2);

            em.flush();
            em.clear();

            String query = "select function('group_concat', m.username) From Member m ";
            List<String> result = em.createQuery(query, String.class).getResultList();

            for(String s : result) System.out.println("S = " + s);

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

![](https://i.imgur.com/hfJUWjK.png)

원하는 함수가 실행되었다.

---

