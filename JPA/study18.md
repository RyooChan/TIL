## 변경 감지와 병합(merge)

> 정말정말정말 중요한 내용이니 꼭 완벽하게 이해해야 한다!!

### 준영속 엔티티
영속성 컨텍스가 더이상 관리하지 않는 엔티티를 말한다.
ItemController의 updateItem처럼
새로운 객체를 생성하였지만, ID를 기존의 값을 가져와서 저장한 객체(Id가 세팅된 객체) -> 즉 JPA에 한번 들어갔다 나온 객체를 준영속 상태의 객체라 한다.

Book객체는 이미 DB에 한번 저장되어 식별자가 존재한다.
이렇게 임의로 만들어 낸 엔티티도 기존 식별자를 가지고 있으면 준영속 엔티티로 볼 수 있다.

본래 JPA과 관리해주는 영속상태 엔티티와 달리 준영속 엔티티는 따로 관리의 대상이 아니다. 즉, 변경을 해도 따로 업데이트를 해주지는 않는다는 것이다.

그렇다면 이런 준영속상태 엔티티를 어떻게 수정해 줄 수 있을까?

* 변경 감지 기능 사용
* 병합(merge) 사용

### 변경 감지 기능 사용(Dirty Checking)

```
@Transactional
public void updateItem(Long itemId, Book bookParam){
    Item findItem = itemRepository.findOne(itemId);
    findItem.setPrice(bookParam.getPrice());
    findItem.setName(bookParam.getName());
    findItem.setStockQuantity(bookParam.getStockQuantity());
    // ... 이렇게 필드를 채워서 진행해주면 findOne을 통해 영속상태로 가져와준 값이 param으로 세팅되게 된다.
    // 그러면 따로 업데이트를 해주지 않아도 영속성 컨텍스트의 관리대상이 되어서 Dirty Checking을 해준다.
    // 그래서 이렇게 하면 바로 업데이트 한다!!!
}
```

간단히 말해서 내가 변경할 값들이 세팅되면, DB에서 해당 ID를 통해 검색을 진행해 주고(이 때 바로 영속화된 findItem이 존재한다.) 이 findItem의 필드를 변경할 값들로 채워주면 영속성 컨텍스트가 바로 DirtyChecking해주어 변경을 감지한다. 그리고 변경된 값이 있으면 Transactional 내에서 알아서 업데이트 시켜준다!!!

### 병합 사용(Merge)
이는 준영속 상태를 영속으로 바꾸어주는 것이다.

`em.merge();`

이렇게 merge를 써주면 위의 코드를 JPA가 짜줘서 동작하게 된다.
다만 둘 간의 차이가 있다.

![](https://i.imgur.com/oGLfT2v.png)

merge의 동작방식은
1. 먼저 1차캐시 엔티티를 찾고, 없으면 DB에서 엔티티를 가져온다.
2. 가져온 엔티티에 준영속상태인 값들을 하나씩 채워넣어준다.
3. 그렇게 채워넣어준 엔티티를 return한다.

이렇게 위의 코드에서 return Item 같은 식으로, 영속화된 엔티티를 return시켜주는 것이다.

그러니까 즉, **이 방식은 준영속화 되어있는 엔티티를 영속화로 바꾸어 주는것이 아니라, merge를 통해 영속화 되어있는 새로운 엔티티를 반환**시켜 주는 것이다!

#### 병합의 주의점
변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이 변경된다.
그래서 이제 병합할 때에 값이 없으면 'null'로 업데이트 될 위험성이 있다.
즉 병합은 모든 필드를 변경한다는 것이고, 이는 매우 위험하다!!

### 결론
그냥 병합은 쓰지 말고 Dirty Checking을 쓰자.

---
