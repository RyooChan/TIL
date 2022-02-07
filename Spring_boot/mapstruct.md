## Mapstruct
* 클래스 간 변환을 위해 사용하는 라이브러리
* Mapstruct를 사용하면 Entity - DTO 간 매핑을 쉽게 해줄 수 있다.

### Mapstruct의 장점 -> ModelMapper와 비교하여
* ModelMapper와 달리 Reflection API를 사용하지 않는다.
* 컴파일시 미리 구현체를 만들고, 이를 사용해 Mapping한다.
* = 처리속도가 훨씬 빠르다.
    * Mapstruct는 10^(-5)m/s, ModelMapper는 2*10^(-3)m/s
* 컴파일 시 오류를 바로 확인 가능하고 디버깅이 원할하다.

### 사용하기


* Entity정의
    * 먼저 Entity는 내가 올릴 게시글에 관한 내용이다.
        * 글의 ID
        * 글의 제목
        * 글의 내용
        * 작성자 정보
```
@Entity 
@Data  
public class Board {
    @Id // id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min=2, max=30)
    private String title;

    @Length(min=20)
    @Lob
    @Column(columnDefinition="TEXT", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name="userId", referencedColumnName = "id")
    private User user;
}
```

* 작성자 정보 User이다.
```
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Board> boards = new ArrayList<>();
```

* DTO정의
    * 글 리스트를 출력할 때에는 작성글id, 제목, 작성자 이름의 3가지를 출력하면 된다. 다른 정보는 불필요하기도 하고, 닉네임이나 role같은 내용은 보여주지 않는게 바람직하다.
```
@Builder
@AllArgsConstructor
@Data
public class BoardListDto {
    private Long id;
    private String title;
    private String userName;
}
```

이 Entity를 DTO로 사용할 때에 Mapstruct를 사용하면 간단하고 빠르게 구현할 수 있다.

---

* Gradle 선언
```
implementation 'org.mapstruct:mapstruct:1.4.2.Final'
annotationProcessor "org.mapstruct:mapstruct-processor:1.4.2.Final"
annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
```

* Controller에서 사용할 때에는
>         List<BoardListDto> boardPostDtos = boardListMapper.toDtos(boardService.list(searchText));
요런 식으로 모든 List를 받아와서 진행할 것이다. 왜냐면 리스트를 하나씩 받아와서 출력하기 때문이다.
    


* Generic Mapper선언

```
public interface EntityMapper <D, E> {
    E toEntity(D dto);
    D toDto(E entity);

    // Entity업데이트 시 null이 아닌 값만 업데이트 하도록 함.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(D dto, @MappingTarget E entity);

    List<D> toDtos(List<E> entity);
}
```
이곳에서 interface를 구현하면 자동으로 구현체를 만들어 준다.
추가로 toEntity는 DTO -> Entity, toDto는 Entity -> DTO를 매핑한다.
그리고 업데이트를 진행하는 경우 딱 업데이트를 해줘야 할 값들만 변경할 수 있도록 할 때는 updateFromDto를 사용한다.
마지막으로 List형식으로 된 경우(지금처럼) toDtos를 사용하면 List형태의 Entity들을 한꺼번에 Dto의 List로 변경해 줄 수 있다.


* Entity - DTO간 데이터 변환을 해 줄 BoardListMapper선언

```
@Mapper(componentModel = "spring")
public interface BoardListMapper extends EntityMapper<BoardListDto, Board> {
    @Override
//    @Mapping(target = "userName", source = "user.name.value")
//    List<BoardListDto> toDtos(List<Board> board);
    @Mapping(target = "userName", source = "user.name")
    BoardListDto toDto(Board board);

}
```
위의 controller에서 변경해줄 때 사용할 boardListMapper이다.
Mapping을 사용하면 서로 다른 이름을 가진 column들도 매핑해 줄 수 있다.
나는 개인정보->이름의 값을 userName에 적요해 줄 것이기 때문에 적용해 주었다.
그리고 원래대로면 List를 매핑해 줄 거니까 List형식으로 된 toDtos를 implement해줘야 하는게 아닌가? 하는데 실제로는 toDto를 진행해 주면 Mapstruct에서 알아서 List에 적용해 준다.
    
---
    
Mapstruct를 사용하면 반복적인 매핑을 줄여줄 수 있고, 속도도 빠르고 디버깅도 간편하다. 
다만 단점은 mapper를 하나하나 만들어줘야해서 좀 코드가 난잡해지고 많이 써야하는 것 같기는 하다...
그래도 외국에서는 자주 쓰이는 방법이고, 속도 면에서도 좋기 때문에 배워 두는것이 좋을 것이라 생각한다.
