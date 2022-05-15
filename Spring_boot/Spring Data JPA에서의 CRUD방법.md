# Spring Data JPA를 통한 CRUD방법에 관하여..(feat 변경감지)

Spring Data JPA를 사용한 CRUD방법을 구현하면 어떻게 하나?

1. 조회(Read)
find나 get을 사용한다.
이 둘의 차이는 [여기](https://hello-backend.tistory.com/157)서 알아보자
2. 저장(Create)
save를 사용한다.
이 save에 관해 조금 후에 다시 서술한다.
3. 삭제(Delete)
delete를 사용하거나, 실제로 데이터를 삭제하고 싶지 않으면 [soft delete](https://hello-backend.tistory.com/151)를 하면 된다.

이제 변경(Update)를 한다면?

## Update방식

위에서 데이터 저장에 사용된 save에 대해 먼저 알아보자

얘 코드를 살펴보면

```
/*
 * (non-Javadoc)
 * @see org.springframework.data.repository.CrudRepository#save(java.lang.Object)
 */
@Transactional
@Override
public <S extends T> S save(S entity) {

	if (entityInformation.isNew(entity)) {
		em.persist(entity);
		return entity;
	} else {
		return em.merge(entity);
	}
}
```

이런 식이다... 확인해 보면

1. 해당 엔티티가 새거면 영속화하고 이 엔티티 return
2. 해당 엔티티가 새로운 것이 아니면 merge 진행

뭔가 이상한 것이 보인다... isNew를 사용해서 해당 메소드가 새로운 것인지 확인하네??
그러면 save는 어떻게 동작하는 것일까? 한번 확인해보자

### save 실험해보기

```
@Test
public void save테스트() throws Exception {
    Continent continent = new Continent(1L, "아시아");
    Continent save1 = continentRepository.save(continent);
}
```

이 간단한 예제를 동작시켜본다.
save를 한번 해보면

![](https://i.imgur.com/LVKuTF0.png)

이렇게 나온다...
분명히 save만 했는데 여기서는 select를 해버리네??

---

이번에는 한번 ID값을 빼보고 다시 save해본다.
ID 생성 방식은 Auto_increment를 사용했다.

```
@Test
public void save테스트() throws Exception {
    Continent continent = new Continent("아시아");
    Continent save = continentRepository.save(continent);
}
```

이런 느낌으로 ID를 제거한 엔티티를 저장해준다.

![](https://i.imgur.com/7UrF8QM.png)

이번에는 select 없이 저장됐네?? 

---

위의 방식과 아래 방식의 차이는 save에 사용되는 ID가 있는지 없는지도 구분된다.

위에서 말했듯 save메소드는 현재 들어온 엔티티가 새거인지 아닌지 isNew 메소드를 통해서 확인하는데, 이녀석의 판별 방식은 이와 같다.

* 기본 타입(int, long, char 등)
    * 0이 들어왔는지 확인한다.
* 레퍼런스 타입(String, Long 등)
    * null로 들어왔는지 확인한다.

저런 기준이 있는 이유는 update와 insert를 하나의 레퍼런스를 통해 진행하려 하기 때문이라고 생각한다.
보통 insert를 할 때에 어떤 아이디를 가지고 진행하는 경우는 없으니 ID가 있는 경우를 Update라고 생각했을 것이다.

이 내용을 살펴본 결과,

ID를 가지고 save하는 경우는 merge()로, ID없이 진행한 경우는 persist로 적용되었음을 확인 가능하다.

---

이제 merge가 어떤 식으로 update를 진행시키는지 천천히 알아보도록 하자

먼저 영속상태와 준영속 상태에 관해 간단히 말하자면

영속 상태 : 영속성 컨텍스트가 자동으로 해당 엔티티를 영속화해주는 것
준영속 상태 : 영속성 컨텍스트의 관리 밖에 있는 상태

이다.
다른 내용 없이 정말 간단하게 설명하면 준영속 상태이면 값을 바꾸든 뭘 하든 영속성 컨텍스트가 관리하지 않으므로 실제로 적용이 되지 않는 상태라고 생각하면 편하다.

이 때 준영속 상태를 -> 영속 상태로 변경하는 두 가지 방법 중 하나가 바로 merge()이다.


### merge() 방식

== 병합 사용

merge의 동작 방식은 다음과 같다.

1. 1차캐시 엔티티에서 해당 엔티티를 찾는다.
2. 여기서 찾지 못하면 DB에서 엔티티를 검색한다.
3. DB에서 가져온 엔티티의 값에 준영속 상태의 값들을 하나씩 채워넣는다.
4. 이렇게 채워넣은 엔티티를 return한다.

정확히 말하자면, **준영속화 되어 있는 엔티티를 영속화 해주는게 아니라, 영속화 된 새로운 엔티티에 값을 끼워넣어 주는 것이다.**

이 방식에는 한 가지 큰 문제점이 있다.
바로 준영속 상태의 값들 중 null인 값이 있다면 기존의 데이터에서도 그게 null이 된다는 것!!

예를 들어



| 이름 | 나이 | 키 |
| -------- | -------- | -------- |
| 류찬     | 26     | 178     |

으로 이미 저장되어 있고, 이 사람이 키가 커서 183이 되었다고 가정했을 때

키만 183으로 설정한 엔티티를 merge하면


| 이름 | 나이 | 키 |
| -------- | -------- | -------- |
| null     | null     | 183     |

이 될 수도 있다는 것이다!!

### Dirty Checking 방식

그러면 어떻게 할까?
아까 영속화하는 방식이 두가지 있다고 했는데, 다른 한 가지 방법이 바로 그것이다.

사실 구현 방식은 별로 다르지 않다.

1. 해당 ID를 통해 DB에서 엔티티를 조회한다.
2. 이 엔티티는 영속성 컨텍스트의 영향 아래에 있다(영속화 된 엔티티이다.)
3. 해당 엔티티에 집어 넣어줄 값을 하나씩 넣어준다.
4. 그러면 나중에 값이 변경된 엔티티는 영속화 되어 있으므로 변경이 감지되어 update된다.

다른 점은, merge를 사용해서 맡기지 않고, 필요한 값을 직접 검색해서 해 주는 것이다.

---

## 마무리

변경 감지 즉 Dirty Checking은 Spring Data JPA에서 Update를 하는 좋은 방법이다.
나중에 Bulk로 update하거나, Soft Delete를 구현할 때에도 이를 생각해서 해주자!

