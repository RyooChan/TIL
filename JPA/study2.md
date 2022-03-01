# 인프런 스터디 2
###### tags: `Tag(인프런)`

## 프로젝트 시작하기

h2 데이터베이스를 다운로드받는다.
버전은 14.1.119로 함.

---

maven으로 프로젝트생성
![](https://i.imgur.com/nLqV53S.png)

![](https://i.imgur.com/4MaqX6f.png)

---

pom.xml 작성

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>jpa-basic</groupId>
    <artifactId>ex1-hello-jpa</artifactId>
    <version>1.0.0</version>
    <dependencies>
        <!-- JPA 하이버네이트 -->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-entitymanager</artifactId>
            <version>5.6.5.Final</version>
        </dependency>
        <!-- H2 데이터베이스 -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.1.210</version>
        </dependency>
    </dependencies>
</project>
```

-> 하이버네이트나 H2의 경우 자신 상태에 맞춰서 진행해주면 된다.

---

persistence.xml 작성하기

![](https://i.imgur.com/fcVrQBp.png)

해당 위치에 persistence.xml 생성

```
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.2"
             xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
    <persistence-unit name="hello">
        <properties>
            <!-- 필수 속성 -->
            <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
            <property name="javax.persistence.jdbc.user" value="sa"/>
            <property name="javax.persistence.jdbc.password" value=""/>
            <property name="javax.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>

            <!-- 옵션 -->
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            <property name="hibernate.use_sql_comments" value="true"/>
            <!--<property name="hibernate.hbm2ddl.auto" value="create" />-->
        </properties>
    </persistence-unit>
</persistence>
```


---

코드 작성하기(연결 확인하기)

```
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JpaMain {
    public static void main(String[] args){
        // persistence unit name을 넘기라 하는데, 이게 hello로 되어 있다.
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        //code
        em.close();
        emf.close();
    }
}
```

이렇게 하고 해당 내용을 실행해 보면, 실행이 되고 종료되는 것을 확인 가능하다.

---

### 기초 설정

#### h2실제 사용

localhost:8082에 접속한다.

![](https://i.imgur.com/MvYwD0E.png)

h2에서 다음 코드를 통해 테이블 생성

```
create table Member ( 
 id bigint not null, 
 name varchar(255), 
 primary key (id) 
);
```

#### java객체 생성
![](https://i.imgur.com/4yVjQZF.png)

```
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Member {

    @Id
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

### Data Insert 진행하기

```
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {
    public static void main(String[] args){
        // persistence unit name을 넘기라 하는데, 이게 hello로 되어 있다.
        // EntityManagerFactory는 딱 한번 실행해 준다.
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        // Transaction 단위마다 em을 하나씩 만들어 줘서 진행한다.
        EntityManager em = emf.createEntityManager();

        // 하나의 Transaction 시작
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        //code
        Member member = new Member();
        // member에 값을 저장한다.
        member.setId(1L);
        member.setName("HelloA");
        // em에 값 설정
        em.persist(member);
        // commit 진행
        tx.commit();

        em.close();
        emf.close();
    }
}
```
JpaMain에서 해당 코드를 실행하면

![](https://i.imgur.com/JuMLMni.png)

IntelliJ에서 실행된 코드가 나타나고

![](https://i.imgur.com/ezEkKT9.png)

DB에도 저장된 것을 확인할 수 있다.


사실 근데 보면, Transaction이 실행되면 그 사이는 Try ~ catch로 로직을 진행해야 한다. 정석적인 코드는
```
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {
    public static void main(String[] args){
        // persistence unit name을 넘기라 하는데, 이게 hello로 되어 있다.
        // EntityManagerFactory는 딱 한번 실행해 준다.
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        // Transaction 단위마다 em을 하나씩 만들어 줘서 진행한다.
        EntityManager em = emf.createEntityManager();

        // 하나의 Transaction 시작
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            //code
            Member member = new Member();
            // member에 값을 저장한다.
            member.setId(2L);
            member.setName("HelloB");
            // em에 값 설정
            em.persist(member);
            // commit 진행
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

이렇게 되기는 한데...이거 Spring에서 알아서 해줘서 상관 x

---

### Data find 진행하기

```
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Member findMember = em.find(Member.class, 1L);
            System.out.println("id : " + findMember.getId() + ", name = " + findMember.getName());
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

![](https://i.imgur.com/madNDMp.png)

값을 찾아 온 것을 확인할 수 있다.
Long 1의 id를 값는 하나의 Table 데이터를 객체로 가져와서 볼 수 있다.

---

### Data Update 진행하기

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
            Member findMember = em.find(Member.class, 1L);
            findMember.setName("HelloChan!");

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

![](https://i.imgur.com/0s804ef.png)

![](https://i.imgur.com/mlBpojr.png)

코드를 보면 그냥 findMEmber.setName에서 이름을 저장해 준 것 만으로도 H2내의 객체를 Update해준다.
-> 자바 객체에서 값을 바꿨을 뿐인데 DB에서도 바꿨다.

그 이유는 JPA에서 자바 객체를 관리하며 이 값이 바뀌었는지 아닌지에 대해 Transaction이 끝나는 시점에서 확인을 한다.
그래서 그 값이 바뀌게 되면 그걸 캐치하여 commit해 주는 것이다.
만약 동일한 값으로 set해주고 진행하면 update가 되지 않는 것을 확인할 수 있을 것이다.

JPA의 모든 데이터 변경은 트랜잭션 안에서 실행한다.

---

### JPQL
간단히 말하자면, 내가 원하는 쿼리를 만들어 내기 위한 방법이다.

* createQuery
쿼리와 비슷하게 JPQL을 사용하여 호출할 수 있다.
이를 사용하면 쿼리와 같은 방식으로 객체를 다룰 수 있도록 해 주며 추가적인 기능을 부여할 수 있다. (**객체지향 쿼리**)
예를 들어 페이징을 하는 경우

```
public class JpaMain {
    public static void main(String[] args){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 하나의 Transaction 내에서 코드를 실행시키고, 문제가 없으면 commit까지 해 준다.
        try{
//            Member findMember = em.find(Member.class, 1L);
            List<Member> result = em.createQuery("select m from Member as m", Member.class)
                    .setFirstResult(5)
                    .setMaxResults(8)
                    .getResultList();

            for(Member member : result) System.out.println("name = " + member.getName());

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

이런 식으로 하면 
![](https://i.imgur.com/nXDzG0s.png)
이런 식으로 알아서 페이징까지 진행해 준다.

정리하자면
> JPA를 사용하면 엔티티 객체를 중심으로 개발을 하게 되는데, 검색을 할 때에도 엔티티 객체를 대상으로 검색한다.

> 여러 검색 조건이 필요한 SQL이 필요할 때에는 JPQL을 사용하게 된다.

