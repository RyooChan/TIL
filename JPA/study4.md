# 인프런 스터디 4
###### tags: `Tag(인프런)`


DDL자동생성 사용
![](https://i.imgur.com/PDYwFV9.png)
1. create
기존 테이블 삭제 후 다시 생성
즉, 기존에 있던 해당 이름을 가진 테이블을 모두 삭제하고, 다시 Entity로 Table을 생성해 준다.
2. create-drop
어플리케이션이 종료되는 시점에 Table이 을 drop한다.
보통 Test Case사용할 때 끝나고 걍 다 지워버리려고 사용한다.
3. update
변경되는 경우, 이를 적용해 준다.(참고로 추가나 변경은 가능한데, 지우는건 안된다.)
4. validate
정상 매핑이 되었는지 확인하는 용도로만 쓴다.
5. none
auto기능을 쓰기 싫으면 걍 이거 쓴다.
암거나 써도 되는데 관례상 이거 씀.

## 엔티티 매핑
### 객체와 테이블 매핑
@Entity가 붙은 클래스는 JPA가 관리하고, 엔티티라 한다.
JPA를 사용해서 테이블과 매핑할 클래스는 @Entity가 필수
* 주의
    * 기본 생성자 필수(파라미터가 없는 public 또는 protected 생성자)
    * final 클래스, enum, interface, inner클래스 사용X
    * 저장할 필드에 final 사용X

#### @Table
> 엔티티와 매핑할 테이블 지정

![](https://i.imgur.com/522NUOt.png)


---

Member클래스를 이렇게 변경하고

```
import javax.persistence.*;
import java.util.Date;
public class Member {

    @Id
    private Long id;

    @Column(name = "name")
    private String username;

    private Integer age;

    // JAVA에서 ENUM으로 만들었을 때, DB에는 보통 enum타입이 없을 것이다.
    // 그 때에 Enumerated타입을 통해 만들어낼 수 있다.
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    // 날짜 관련은 Temporal을 사용하고, 3가지 타입이 있다.
    // DATE : 날짜, TIME : 시간, TIMESTAMP : 두개 다 포함
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    // 굉장히 큰 크기를 가진 것을 사용하기 위해서 사용
    @Lob
    private String description;
}
```

동일 패키지에 RoleType 생성
```
public enum RoleType {
    USER, ADMIN
}
```

그리고 기존 JpaMain코드에서 문제가 될 부분을 없애 주고 

persistence에서 해당 내용 주석 해제
```
<property name="hibernate.hbm2ddl.auto" value="create" />
```

이후 실행해 주면

![](https://i.imgur.com/VCioPyS.png)

이렇게 알아서 만들어 준다.

사용 가능한 기능은
![](https://i.imgur.com/f7vn6bF.png)
다음과 같다.

1. @Column
![](https://i.imgur.com/NvC4AvC.png)

* name
    * 이름
* insertable / updatable
    * 등록 / 변경 가능 여부
        * 만약에 updatable = false 이러면 여기서의 변경이 반영되지 않는다.
* nullable
    * null허용 여부를 넣는다.
        * false -> not null
* unique
    * unique 제약조건을 넣어준다.
        * 근데 이거는 잘 안쓰고 다른 방식을 사용한다. Table에서 선언하곤 함.
* columnDefinition
    * 컬럼 정보를 직접 선언해줄 수 있다.
* length
    * 길이 제약조건을 주고, String에서만 사용 가능
* precision, scale
    * BigDecimal에서 아주 큰 숫자 등에서 사용 가능

2. @Enumerated
Enum타입에서 값들을 넣어 준다.
근데 이게 두가지 방법이 있는데, 기본은 ORDINAL이다.
* EnumType.ORDINAL
    * 값을 순서대로 0, 1, 2, ....이렇게 세어서 순서로 저장함.
        * 사용하지 말자!
            * 중간에 새로운 값이 enum에 추가되면 이거 큰일남.
* EnumType.String
    * 갑의 이름을 고대로 저장한다.
        * 이걸로 쓰자.

3. @Temporal
날짜 타입을 매핑할 때 사용.
근데 이거 요즘 잘 안쓰는데.. 그 이유는 java8이후로 현재 지원해주는
LocalDate(연월) / LocalDateTime(연월일) 요렇게 쓰면 알아서 해준다.

4. Lob
DB의 BLOB, CLOB과 매핑된다.
따로 설정을 해줄수는 없고, 필드 타입에 맞춰서 해준다.
* BLOB
    * byte[], java.sql.BLOB
* CLOB
    * String, char[], java.sql.CLOB

5. Transient
매핑 하기 싫을때 사용.
걍 메모리에서 임시로 값을 보관할 때에 사용.
