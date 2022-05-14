# Spring Data JPA에서 get 과 find의 차이

Spring Data JPA에서 ID를 통하여 어떤 엔티티를 조회하고 싶을 때, 우리는 getById나 findById를 사용한다.

둘 간에 어떤 차이가 있을까??

## getById

```
@Override
public T getById(ID id) {
    Assert.notNull(id, ID_MUST_NOT_BE_NULL);
    return em.getReference(getDomainClass(), id);
}
```

getById의 내부를 살펴보면 이렇게 나온다.
이 getById는 em.getReferecnce를 이용해서 엔티티를 조회..하는데, 사실 이거는 엔티티가 아니라 프록시를 반환한다는 것이다.
즉, getById를 통해 바로 DB에 검색하는게 아니라, 이를 실제로 사용할 때에 DB에 접근한다는 것이다.

한마디로 Lazy형태로 값을 가져온다 생각하면 된다.(사실 Lazy로 가져온 엔티티가 프록시임)

그리고 이거 `일단 프록시로 만듬 -> 검색함` 이런 식으로 동작하기 때문에, 만약에 없는 데이터를 조회하면 실제로 접근할 때에 `EntityNotFoundException` 이 터진다.

## findById

```
@Override
public Optional<T> findById(ID id) {

    Assert.notNull(id, ID_MUST_NOT_BE_NULL);

    Class<T> domainType = getDomainClass();

    if (metadata == null) {
        return Optional.ofNullable(em.find(domainType, id));
    }

    LockModeType type = metadata.getLockModeType();

    Map<String, Object> hints = new HashMap<>();
    getQueryHints().withFetchGraphs(em).forEach(hints::put);

    return Optional.ofNullable(type == null ? em.find(domainType, id, hints) : em.find(domainType, id, type, hints));
}
```

다음으로 findById인데, 이놈은 프록시가 아니라 실제로 DB에서 값을 찾아온다.
추가로 Optional로 한번 감싸서 값을 가져오는데, 얘는 해당 ID의 객체가 없다면 Null을 반환한다.

## 비교하면??

간단하게 말하면 get의 방식이 더 성능이 좋은데, 기본적으로 프록시 형태로 가져오기 때문에 DB에의 접근을 최소화할 수 있기 때문이다.

물론 이런 경우가 엄청나게 많이 발생하지는 않기는 한데, 개인적으로는 후처리에서도 find의 경우 optional을 또 처리해주어야 하기 때문에 귀찮다... 그래서 get을 조금 더 애용하는 편이다.
