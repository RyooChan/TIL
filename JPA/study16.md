# 인프런 스터디 16
###### tags: `Tag(인프런)`

## 경로 표현식
.(점)을 찍어 객체 그래프를 탐색하는 것

![](https://i.imgur.com/NRYYuj2.png)

* 상태 필드
    * 단순히 값을 저장하기 위한 필드이다.
* 연관 필드(연관관계를 위한 필드)
    * 단일 값 연관 필드
        * @ManyToOne, @OneToOne
            * 대상이 엔티티이다.
    * 컬렉션 값 연관 필드
        * @OneToMany, @ManyToMany
            * 대상이 컬렉션이다.

### 경로 표현식의 특징
* 상태 필드
    * 경로 탐색의 끝, 탐색X
        * 즉 여기 이상으로 갈 수 있는곳이 없다는것이다.
* 단일 값 연관 경로
    * 묵시적 내부조인(inner join)발생, 탐색O
        * 계속해서 .(점)을 찍어서 그 아래 값에 대한 탐색이 가능하다.
* 컬렉션 값 연관 경로
    * 묵시적 내부 조인 발생, 탐색X
        * 컬렉션으로 가져오게 되어서 더이상 내부의 값을 가져오는 것은 안된다.
            * 명시적인 join을 걸어준 후에 그 별칭을 통한 탐색은 가능하다.

단, 실무에서는 묵시적 조인을 사용하지 말고 명시적 조인을 사용하도록 한다.
그 이유는 join이 SQL튜닝의 중요 포인트인데, 묵시적 조인은 이 조인이 일어나는 상황을 한눈에 파악하기 어렵기 때문이다.

---

## 패치 조인(fetch join)
실무에서 매우 중요하다. 
* SQL의 조인 종류에 해당하지는 않는다.
* JPQL에서 성능 최적화를 위해 제공하는 기능이다.
* 연관된 엔티티나 컬렉션을 SQL한 번에 조회하는 기능.
* join fetch 명령어 사용

예를 들어 회원을 조회하면서 연관된 팀을 함께 조회하려 한다(SQL한번에)

select m from Member m join fetch m.team
-> SELECT M.*, T.* FROM MEMBER M INNER JOIN TEAM T ON M.TEAM_ID=T.ID

---

아래와 같이 inner join을 사용하여 Team이 있는 member들을 가져온다고 가정한다면
![](https://i.imgur.com/iy710a2.png)

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
            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            String query = "select m From Member m ";
            List<Member> result = em.createQuery(query, Member.class).getResultList();

            for(Member member : result) System.out.println("member = " + member.getUsername() + ", " + member.getTeam().getName());

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

먼저 이렇게 member를 가져온 후에 찾아내면
이전에 member에서 @ManyToOne을 LAZY로 설정해 두었기 때문에 team은 proxy로 호출된다.
따라서 지연 로딩이 일어나게 된다.

![](https://i.imgur.com/ZE9EXIR.png)

-> 처음 회원1에 대해 팀A가 SQL을 통해 가져와진다.
-> 다음 회원2에 대해 팀A는 1차캐시된다.
-> 다음 회원3에 대해 팀B는 SQL을 통해 가져와진다.

이렇게 되면 굉장히 비효율적인 쿼리가 이루어진다. (N+1)
모든 경우에 대해 따로따로 쿼리가 돌아가기 때문.

---

이를 해결하기 위해 query부분을 fetch join으로 변경해준다.

`String query = "select m From Member m join fetch m.team";`

이렇게 하면 조회할 때에 한꺼번에 값들을 가져온다.
참고로 이미 member에서 지연 로딩으로 설정해 주었지만 fetch가 우선된다.

![](https://i.imgur.com/rrmxIjK.png)
이런식으로
그리고 위에서 한꺼번에 값들을 가져왔기 때문에, 이는 프록시가 아닌 실제 데이터이다.

---

### 컬렉션 페치 조인
일대다 관계, 컬렉션 페치 조인

반대로 일대다에 관한 조인이다.

---

* JpaMain

```
select t from Team t join fetch t.members where t.name = '팀A'
-> SELECT T.*, M.* FROM TEAM T INNER JOIN MEMBER M ON T.ID=M.TEAM_ID WHERE T.NAME = '팀A'

public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            String query = "select t From Team t join fetch t.members";
            List<Team> result = em.createQuery(query, Team.class).getResultList();

            for(Team team : result) System.out.println("team = " + team.getName() + ", " + team.getMembers().size());

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

이런 식으로 일대다에서 join을 걸어 확인하게 되면...

---

![](https://i.imgur.com/rKrYfgz.png)

이렇게 나온다. 그런데 보면
팀A에 대해 members가 2개 걸리는것이 총 2번 출력된다...

그 이유는 아래와 같다.

![](https://i.imgur.com/nYqKmHO.png)

이런 식으로 팀A는 회원1, 회원2를 각각 갖는다.
이를 join해서 가져오게 되면 각 회원 1, 2에 대해 팀A를 가져서 실제로는 2번 출력되게 되는 것이다.

이렇게 DB입장에서 일대다 join하면 결과가 뻥튀기된다.
이 값에 대해

1. JPA는 DB와 통신한 결과를 모두 가져온다.
2. 영속성 컨텍스트에서 팀A의 결과(회원1에 관한)를 저장한다.
3. 영속성 컨텍스트에 팀A가 저장되어 있는 상태에서, 또다른 결과(회원2에 관한)를 저장한다.
4. JPA는 DB에서 받은 결과의 수만큼 출력하기 때문에
5. 같은 주소값을 가진 결과가 2개 출력되게 된다.

### 페치 조인과 DISTINCT
기존 SQL의 DISTINCT만으로는 중복된 결과를 모두 제거하기 힘들다.

JPQL의 DISTINCT는 총 2가지 기능을 제공하는데
1. SQL에 DISTINCT를 추가
2. 애플리케이션에서 엔티티 중복 제거


`String query = "select distinct t From Team t join fetch t.members";`

이런 식으로 distinct를 적용해 주면

![](https://i.imgur.com/YoxT9tI.png)
1. SQL자체에 DISTINCT 적용

![](https://i.imgur.com/mUL68rR.png)
2. 엔티티 중복 제거

가 되었음을 확인할 수 있다.

참고로, 일대다는 DB입장에서는 어쨌든 데이터가 뻥튀기된다.
근데 다대일은 데이터가 뻥튀기되지는 않는다.
이를 알아둘것.


### 페치조인과 일반조인의 차이
일반 조인 실행시 연관된 엔티티를 함께 조회하지 않음.

예를 들어

`String query = "select t From Team t join t.members";`

이런 식으로 일반 join만을 실행하면


![](https://i.imgur.com/CUudUJB.png)

team의 값만 처음에 가져오고

![](https://i.imgur.com/YJ9DQJb.png)

이후에 요청받은 사항에 대해 추가로 select를 실행하는 것을 확인할 수 있다.

즉
-> 페치 조인을 사용할 때에만 연관된 엔티티도 함께 조회(즉시 조인)
-> 페치 조인은 객체 그래프를 SQL한번에 조회하는 개념이다. 

---

### 페치 조인의 특징과 한계
* 페치 조인 대상에는 별칭을 줄 수 없다.
    * 페치 조인은 자신과 관련된 대상을 다 가져오는 것이다. 이상하게 동작할 수 있다.
        * 객체 그래프는 기본적으로 관련된 대상을 모두조회하는 것이다. where같은 것을 써서 일부만 사용하는것은 지양해야한다.
            * 데이터의 정합성이 깨질 수 있기 때문.
* 둘 이상의 컬렉션은 페치 조인할 수 없다.
    * 일대다의 경우도 데이터 뻥튀기되는데 이경우는 일대다대다로 이루어진다.
        * 잘못하면 데이터가 예상치 못하게 매우 커져버릴 수 있다.
* 컬렉션을 페치 조인하면 페이징 API(setFirstResult, setMaxResults)를 사용할 수 없다.
    * 데이터가 뻥튀기 되었는데 그걸 잘라서 가져오게 되는것은 안되기 때문.
    * 일대일, 다대일같은 단일 값 연관 필드들은 페치조인해도 페이징 가능.
    * 하이버네이트는 경고 로고를 남기고 메모리에서 페이징(**매우위험**)
        * 일대다 -> 다대일로 변경해서 사용할 수 있다.
            * 다만 이경우 @BatchSize()를 사용하여 한꺼번에 N개에 해당하는 개수를 로딩해 버릴수 있다(N+1해결을 위해)
            * 혹은 persisten.xml에 hibernate.default_batch_fetch_size를 사용하여 global로 설정할 수 있다.
* 연관된 엔티티들을 SQL한 번으로 조회한다 -> 성능 최적화
* 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선된다.
    * @OneToMAny(fetch=FetchType.LAZY) // 글로벌 로딩전략
* 실무에서 글로벌 로딩 전략은 모두 지연로딩이다.
* 최적화가 필요한 곳은 페치 조인 적용
* 모든것을 페치조인으로 해결할수는 없다.
* 페치 조인은 객체그래프를 유지할 때 사용하면 효과적이다.
    * 현재 위치에서 어딘가의 위치로 찾아갈 때
        * 여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야하면, 페치조인보다는 일반조인을 사용하고 필요한 데이터들만 조회해서 DTO로 반환하는것이 효과적이다.

---
