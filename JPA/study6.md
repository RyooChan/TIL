# 인프런 스터디 6
###### tags: `Tag(인프런)`

## 연관관계 매핑

### 객체 모델링 진행시

* Member Entity 코드
```
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    @Column(name = "TEAM_ID")
    private Long teamId;

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

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}
```

---

* Team Entity코드
```
@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;

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

---

* JpaMain코드
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
            team.setName("TeamA");

            em.persist(team);

            Member member = new Member();
            member.setName("member1");
            member.setTeamId(team.getId());
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

이렇게 하고 실행하면
![](https://i.imgur.com/WMXXcDZ.png)
이렇게 member에서 TEAM_ID에 해당하는 id가 저장되게 될 것이다.

이런 식으로 하면, 이후에 관련해서 변경하거나 조회하는 경우 가져온 식별자를 통해 다시 검색하고... 이렇게 하게 된다.
이는 객체 지향적인 방법이 아니다.

즉 **객체를 테이블에 맞추어 데이터 중심으로 모델링하면, 협력 관계를 만들 수 없다.**
* 테이블은 외래키로 조인을 사용하여 연관된 테이블을 찾는다.
* 객체는 참조를 사용하여 연관된 객체를 찾는다.

둘의 패러다임이 완전히 다르다.

---

### 단방향 연관관계

먼저 Member가 Team을 참조하게 만들어 보도록 하자.

* Member Entity 수정
```
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

//    @Column(name = "TEAM_ID")
//    private Long teamId;

    // 1대N 매핑. 이는 Team을 참조한다.
    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;

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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
```

Member의 코드를 다음과 같이 바꾼다.
단방향 매핑이기 때문에 Member에만 team을 참조로 넣어준다.

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
            Team team = new Team();
            team.setName("TeamA");

            em.persist(team);

            Member member = new Member();
            member.setName("member1");
            member.setTeam(team);
            em.persist(member);

            Member findMember = em.find(Member.class, member.getId());

            Team findTeam = findMember.getTeam();
            System.out.println("findTeam = " + findTeam.getName());

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

JpaMain에서 Member를 통해 Team을 바로 세팅해서 넣어주고, 값을 꺼내온다.

---

![](https://i.imgur.com/MsaW1UE.png)

이렇게 TeamA의 값을 참조하여 바로 가져올 수 있다.
insert보다 먼저 가져온 이유는 당연히 insert가 commit될 때 지연되어 이루어지고 team의 값들은 영속화되어 캐시되었기 때문.

---

### 양방향 연관관계

* member코드

```
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

//    @Column(name = "TEAM_ID")
//    private Long teamId;

    // 1대N 매핑. 이는 Team을 참조한다.
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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
```

---

* Team 코드

```
@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")       // mappedBy는 일대다 매핑에서 반대쪽에는 어떤 것과 연결되어 있는지를 보여준다.
    private List<Member> members = new ArrayList<Member>();

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
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

양방향 매핑을 위해 Team에서도 members를 참조하는 ArrayList를 만들어 준다.
해당 ArrayList는 member에서 Team을 참조하는 값을 mappedBy로 나타내 준다.

---

* JpaMain 코드

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
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeam(team);
            em.persist(member);

            em.flush();
            em.clear();

            Member findMember = em.find(Member.class, member.getId());
            List<Member> members = findMember.getTeam().getMembers();

            for(Member m : members){
                System.out.println("m = " + m.getUsername());
            }

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

양방향 매핑을 확인하기 위해 Insert 후 Team에서 member을 찾아 준다.
참고로 em.flush를 통한 영속화를 미리 진행해 주어 먼저 insert를 진행할 수 있도록 한다.

![](https://i.imgur.com/0uTPBE0.png)
![](https://i.imgur.com/Sgnh9zP.png)

insert를 하고, 제대로 값이 들어간 것을 확인할 수 있다.

---

#### mappedBy
저기서 mappedBy의 용도가 매우 중요하다.
객체와 연관관계 간의 차이를 이해해야 한다.

객체에서 양방향 연관관계는 사실 단방향 연관관계가 2개 있는 것이다.
근데 Table에서의 연관관계는 양방향 1개로 이루어져 있다.(FK 하나로 다 가능하다.)

여기서 이제 문제가 생긴다.
예를 들어 Member와 Team이 양방향 매핑된다고 하자(다대일)

Member A는 Team A에 소속되어 있고
이 Member A가 Team B로 소속을 옮긴다 하면
이는 DB에서는 Member의 FK를 바꾸어 주면 될것이다.
근데 객체에서 진행하려 하면 어디서 바꿔야 할지??
단방향이면 하나만 바꾸면 되는데 양방향이면 둘 다 바꿔야 하기 때문..

이를 해결하기 위해 **둘 중 하나로 외래키를 관리하도록 정해야 한다.**
즉, DB의 FK에 해당하는 부분을 Member객체에서 관리할지, Team객체에서 관리할지에 관한 것이 바로 연관관계의 주인이다.

* 연관관계의 주인
    * 양방향 매핑 규칙
        * 객체의 두 관계중 하나를 연관관계의 주인으로 지정
        * 연관관계의 주인만이 외래 키를 관리(등록, 수정)
        * 주인이 아닌 쪽은 읽기만 가능하다.
        * 주인은 mappedBy속성을 사용하면 안된다.
            * 이 mappedBy의 뜻은 이것에 의해 mapping되었다는 것이기 때문에 당연히 그 mappedBy로 되어있는쪽이 주인이다.
        * 주인이 아니면 mappedBy속성으로 주인을 지정하면 된다.
            * mappedBy가 적용되면 그것으로는 수정이 불가능하고 조회만 가능하다.

그럼 누구를 주인으로 정해야 할까?
* 외래키가 있는쪽을 주인으로 하는게 좋다.
* 즉 Member.team이 연관관계의 주인이 된다.

#### 양방향 매핑 중 가장 많이 하는 실수
연관관계의 주인에 값을 입력하지 않음.

JpaMain에서

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
            em.persist(member);

            Team team = new Team();
            team.setName("TeamA");
            team.getMembers().add(member);
            em.persist(team);

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

이런 코드를 입력하고 실행하면

![](https://i.imgur.com/2as6Vcf.png)

insert가 실행된다. 그런데 실제로 h2에서 확인해 보면

![](https://i.imgur.com/JwJ9Sfc.png)

member에서 TEAM_ID가 null인 것을 확인할 수 있다.
이는 아까 적혀있던대로 team의 member객체는 mappedBy가 사용되어 연관관계의 주인이 아니라서 값을 넣을 수 없기 때문이다. 그렇기 때문에 값을 넣으려면 FK가 있는 연관관계의 주인, 즉 Member에서 값 적용을 해 주어야 한다.

---

* JpaMain코드를 수정해 준다.
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
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeam(team);
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

이렇게 하고 실행하면 연관관계의 주인인 member에서 값을 넣어주었기 때문에 제대로 들어갔을 것이다.

![](https://i.imgur.com/0bijZ6g.png)

이렇게 잘 들어간 것을 확인할 수 있다.

---

그런데, 실제로 할 때에는 그냥 연관관계의 주인이나 이런거 상관없이 둘 다 세팅해주면 된다.
왜냐면 JPA에서 1차 캐싱을 지원하기 때문에 만약 team에 member가 세팅되어 있지 않은 상태에서 값을 가져오면 캐싱되어 있는 값을 가져오기 때문이다.
그리고 Test Case를 작성할 때에는 java collection으로 동작하기 때문에, 이 때를 위해서도 양쪽 모두 세팅해 주면 된다.

다만 어디에 세팅해야 값이 들어가는지는 알고 있어야 할듯.

### **팁**
위에서 세팅을 해 줄 때 굳이 양방향을 하나하나 세팅해 줄 필요가 없다..
* Member에서 setTeam을 할 때에
```
public void setTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);        // 주인쪽에서 set해주면 바로 다른쪽에도 세팅을 진행해 준다.
    }
```
이렇게 해 주면, 바로 setTeam을 통해 매핑해 줄 때 연관관계 주인의 반대쪽에도 set이 걸리기 때문이다.

혹시 반대쪽을 기준으로 넣고 싶다면
* Team에 member를 세팅해줄 addMember메소드만 하나 추가하여
```
    public void addMember(Member member){
        member.setTeam(this);
        members.add(member);
    }
```
이렇게 하면 반대로도 가능하다.
상황마다 맞춰서 해주기.

참고로 이거 양쪽 다 해주면 안된다...하나만 해주기.

#### 양방향 매핑시 무한 루프 가능성
toString, lombok, JSON생성 등에서

만약 Member에서 Team을 불러오는 경우
1. Member의 참조객체 Team을 불러온다.
2. Team의 참조객체 Member를 불러온다.
3. Member의 참조객체 Team을 불러온다.
4. Team의 참조객체 Member를 불러온다.
5. 무한반복.....

이런 일이 벌어진다.

사실 근데 controller에서 Entity를 반환하지 않으면 된다. 
DTO쓰면 참조를 무한으로 안할듯??

---

### 정리
단방향 매핑만으로도 이미 연관관계 매핑은 실질 완료된 상태이다.
-> 처음 설계할 때에는 양방향으로 하지 말고 단방향으로 완료해야 한다.
왜냐면 양방향 매핑이라는 것은 사실 반대쪽에서 조회하는 방법을 추가하는 것이다.

단방향 매핑을 하고 나면 실질 테이블 정의는 완료된 것이다.
그러니 그냥 조회가 필요할 때에 Entity쪽을 조금 수정해 주면 된다.

그리고 비즈니스 로직이 아닌, 외래 키의 위치를 기준으로 정해주어야 한다.
