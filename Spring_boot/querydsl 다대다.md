# querydsl에서 다대다 구현하기. 일대다-다대일(feat. Result Aggregation)

보통 다대다 매핑을 할 때에는 일대다 - 다대일로 매핑하도록 시킨다.
이 이유는 

* 어차피 JPA에서 만든 후에 이를 처리해주기 위한 중간 테이블을 생성하게 된다.
    * 근데 이 테이블을 사용하는 동안 정체모를 쿼리가 발생할 가능성이 있다.
* 중간 테이블에도 메타 데이터 등의 추가 쿼리가 필요할 수 있는데, 이를 다대다에서는 개발자가 넣어줄 수 없다.

암튼 그래서... 일대다 다대일로 하는데, 여기서 하나의 파트에서 반대 파트의 데이터를 가져오는 방법을 살펴보도록 한다.

## 상황

먼저 프로젝트 **[밈위키](https://github.com/memewiki/memewiki-api-core)** 에서는

![](https://i.imgur.com/DzD1QuG.png)

대충 뭐 이런 느낌으로 "밈 - 태그" 를 갖고 있다.
이는 생각해보면

![](https://i.imgur.com/xcT5MUZ.png)

이렇게 밈과 태그가 다대다로 묶여 있어서

밈-밈태그가 일대다
밈태그-태그가 다대일로 묶여 있게 된다.

이를 querydsl을 통해 한꺼번에 가져와서 보여주는 코드를 작성해 보도록 한다.

### Meme Entity

```
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Meme extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memeUrl;
    private Integer memeHit;
    private Integer memeDownload;

    @OneToMany(mappedBy = "meme")
    private List<MemeTag> memeTagList = new ArrayList<>();
}
```

밈은 다음과 같이 작성되었다.
추가로 일대다 검색을 위해 `@OneToMany` 어노테이션을 붙여 주었다.

### MemeTag Entity

```
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemeTag extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Meme meme;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tag tag;
}
```

다대다의 중간 테이블 역할을 해주는 MemeTag Entity이다.
[Lazy Join을 기본](https://hello-backend.tistory.com/165)으로 갖도록 설정해 주었다.

### Tag Entity

```
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tag extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tagName;

    public Tag(String tagName) {
        this.tagName = tagName;
    }
}
```

Tag Entity이다.
이제 이들을 연결해주는 querydsl 코드를 한번 살펴보도록 한다.

### Meme Response DTO

```
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class MemeRecentResponse {
    private Long memeId;
    private String memeUrl;
    private Integer memeHit;
    private LocalDateTime createdAt;

    private final List<TagMemeRecentResponse> tagMemeRecentResponses = new ArrayList<>();

    @QueryProjection
    public MemeRecentResponse(Meme meme, List<Tag> tagMemeRecentResponsesIn){
        this.memeId = meme.getId();
        this.memeUrl = meme.getMemeUrl();
        this.memeHit = meme.getMemeHit();
        this.createdAt = meme.getCreatedAt();
        tagMemeRecentResponsesIn.forEach(
                tag -> this.tagMemeRecentResponses.add(
                        TagMemeRecentResponse.builder()
                                .id(tag.getId())
                                .tagName(tag.getTagName())
                                .build()
                )
        );
    }
}
```

알다시피 Entity를 client에게 바로 return하는건 별로고 [DTO를 사용해서 전달](https://hello-backend.tistory.com/119)하게 된다.
위의 코드가 그 때에 전달용 Response코드이며, 이를 바로 querydsl 측에서 projection해줄 것이다.

### Tag Response DTO

```
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class TagMemeRecentResponse {
    private Long id;
    private String tagName;

    public TagMemeRecentResponse(Tag tag) {
        this.id = tag.getId();
        this.tagName = tag.getTagName();
    }

}
```

참고로 tag쪽은 다음과 같이 되어있다.
tag를 받으면 Meme Response쪽에서 [Stream](https://hello-backend.tistory.com/229)이랑 [Builder 패턴](https://hello-backend.tistory.com/240)을 사용해서 이걸로 변경시켜준다.

### querydsl 코드

```
    public List<MemeRecentResponse> findMemesWithPageable(Long pagingNum){
        return queryFactory
                .selectFrom(meme)
                .leftJoin(meme.memeTagList, memeTag)
                .leftJoin(memeTag.tag, tag)
                .where(
                        memeIdBetween(pagingNum)
                )
                .distinct()
                .transform(
                        groupBy(meme.id).list(
                                Projections.constructor(MemeRecentResponse.class
                                        , meme
                                        , list(tag)
                                )
                        )
                );
    }
    
    private BooleanExpression memeIdBetween(Long pagingNum){
        return pagingNum != null ? meme.id.between(pagingNum, pagingNum + 30) : null;
    }
```

참고로 이거 import는 꼭!!!!

```
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
```

얘들로 해주자. 이상한거 들고오면 안된다.

사실 querydsl쪽 코드는 간단하다.
나는 검색 위치 ~ 30개 이후를 검색하도록 하였다.
살펴보자면

* selectFrom
    * meme을 가져오고, 이게 중심이 되어 값들을 들고옴
* leftJoin
    * meme전체 -> 해당 밈에 종속된 다대다용 테이블들 가져옴
        * 없으면 null
            * meme전체를 들고오기 위해 leftJoin 사용
    * memeTag(다대다테이블) -> 해당 테이블 아래의 Tag들 가져옴
        * 없으면 null
            * 하위가 없다고 memeTag를 사용하지 않는건 아니므로 leftJoin 사용
* transform
    * 사실상 querydsl쪽에서 중요한 부분이다.
    * 가져온 값들을 바로 변환시켜 사용하도록 한다.
* groupBy
    * meme.id를 기준으로 값들을 그루핑한다.
    * list에서는 여러 값들을 통합시켜 List형태로 만들어준다.
* Projections.constructor
    * 여기서 위에 정의해준 Meme Response를 사용하도록 projection해 주는데, 위에서 나는 meme과 List\<Tag>를 생성자로 사용해 주어서 이를 사용했다.

요렇게 하면

![](https://i.imgur.com/Rj5Pyvr.png)

대충 요런 식으로 값들을 들고오게 되고,

```
{
  "memeId": 1,
  "memeUrl": "test-url",
  "memeHit": 0,
  "createdAt": "2023-02-21T23:27:54",
  "tagMemeRecentResponses": [
    {
      "id": 1,
      "tagName": "화남"
    },
    {
      "id": 2,
      "tagName": "슬픔"
    },
    {
      "id": 4,
      "tagName": "퇴근"
    }
  ]
},
{
  "memeId": 2,
  "memeUrl": "test-url",
  "memeHit": 0,
  "createdAt": "2023-02-21T23:27:54",
  "tagMemeRecentResponses": [
    {
      "id": 1,
      "tagName": "화남"
    },
    {
      "id": 2,
      "tagName": "슬픔"
    },
    {
      "id": 4,
      "tagName": "퇴근"
    }
  ]
},
{
  "memeId": 3,
  "memeUrl": "test-url",
  "memeHit": 0,
  "createdAt": "2023-02-21T23:27:54",
  "tagMemeRecentResponses": [
    {
      "id": 1,
      "tagName": "화남"
    },
    {
      "id": 2,
      "tagName": "슬픔"
    },
    {
      "id": 4,
      "tagName": "퇴근"
    }
  ]
}
```

이런 식으로 원하는 값들을 잘 가져오게 된다.
