# Equals and HashCode(feat. HashMap)

보통 Spring boot를 사용할 때에 class에서 equals랑 hashcode를 같이 정의하고는 한다.
그리고 lombok에는 아예 `@EqualsAndHashCode` 라는 애가 있어 두개를 같이 사용할수도 있다.

그럼 이녀석들이 무엇일까?

### Default

![](https://i.imgur.com/vYr1qbV.png)

먼저 이런 class를 정의해 주자.
해당 class는 RyooChan의 intelligence와 look을 인자로 갖고 있다.

![](https://i.imgur.com/heybVFg.png)

이런 식으로 Object를 가져와서 값이 동일한지를 검사해 준다.

![](https://i.imgur.com/FnszrF8.png)

Test결과 두 객체는 서로 다르다.

참고로 저거 equals를 조금 공부해 봤으면 아니 얘는 값을 비교하는건데 왜 달라?? 라고 생각할 수 있을거같은데 Object를 equals를 비교하면

![](https://i.imgur.com/V7eQ8wM.png)

요렇게 그냥 ==으로 비교함. 즉, 기본적으로 equals는 객체간의 동일성 검사에 사용된다는것을 알 수 있다.

## Equals

![](https://i.imgur.com/1hv71qu.png)

요런 식으로 해당 class의 동일성 검사에 사용될 equals를 구현한다(intelliJ에서 자동 generate)

![](https://i.imgur.com/9asENlI.png)

Object.equals도 마찬가지로 기본 ==연산이다.

이렇게 작성해 주면 본래 동등성 검사에 사용되던 Equals를 동등성 검사에 사용하도록 변경시켜주게 된다.

### Test

이제 테스트를 해보자

![](https://i.imgur.com/heybVFg.png)

예를 들어 이렇게 두 객체를 생성하고 각각 값을 동일하게 넣어준다면 둘은 각각 다른 객체이지만 동일한 값을 가지고 있다. 즉, 동등하다.

그렇게 결과는

![](https://i.imgur.com/tnbXDBT.png)

이런 식으로 나온다!

당연하지만 ==연산자는 동일성을 검사하고 있기 때문에 false가 return된다.

## HashMap, HashSet...

java에서는 HashSet, HashMap등을 사용하여 key를 통한 구현을 진행할 수 있다.

![](https://i.imgur.com/qIn1Iq2.png)

이런 식으로 `IP200에 잘생긴 ryooChan -> godChan` 을 만족하는 hashSet이 있다고 하자.
그런데 key로 사용될 ryooChan1, ryooChan2는 각각 다른 class에서 생성되었지만 실제로는 동등한 값을 갖는 ryoochan에 대한 설명이다.

따라서 테스트 결과로는 아마 1이 return될 것이라고 예상할 것이다.

![](https://i.imgur.com/lRR4Al7.png)

그런데 류찬의 수는 2가 return되었다.
[이거](https://hello-backend.tistory.com/210)를 보고 오면 좀더 이해가 쉬울 것 같은데

HashSet의 `add` 메서드는 

![](https://i.imgur.com/4QuIOL6.png)

이렇게 동작한다.
여기서 map.put은

![](https://i.imgur.com/sN57OuC.png)

이거고, hash는 

![](https://i.imgur.com/U9vnoEX.png)

요렇게 hashCode를 사용해서 Object형식인 key를 저장시킨다.

그리고 저기 쓰이는 hashCode를 보면

![](https://i.imgur.com/p43HVvZ.png)

요런 식으로 되어있다.

저기서 hash함수의 내용으로 보았을 때에 hashcode한 결과가 같으면 두 클래스는 동일한 key로 간주될 것이다.

### 구현

![](https://i.imgur.com/73w8idL.png)

이제 이런 식으로 hashCode를 재정의해준다.

![](https://i.imgur.com/YLPEdIK.png)

![](https://i.imgur.com/nCTXKGp.png)

해당 메서드들을 타고가서 확인해보면 알겠지만, 이제 같은 값들을 넣어주게 되면 동일한 hash값이 return될 것이다.

그러니까 hashCode를 적용한 hashSet, hashMap등등에서도 이걸 재정의하면 같은 값으로 판단하게 될 것이라는거다.

### Test

![](https://i.imgur.com/qIn1Iq2.png)

얘를 다시 테스트해보자

![](https://i.imgur.com/8xxIn9L.png)

이렇게 IQ200에 잘생긴 류찬은 1명인 것이 확인되었다!!

## 결론

사실상 Hash를 사용하는 것에서의 연장이라고 생각이 된다.

HashMap이나 HashSet등에서 class를 key로 사용해 본적이 없었는데, 이에 관한 공부를 해보면서 `equals`와 `hashCode`를 사용해야 하는 이유를 알 수 있었고, 둘을 같이 재정의할 필요성이 무엇인지를 알았다.

또 단편적으로 알고있던 class생성, `==`, `equals`등에 추가로 `hashSet`, `hashMap`의 구현을 상세히 보면서 그동안 배운 것을을 활용해서 더 많은 것에 적용할 수 있음이 신기했다.
