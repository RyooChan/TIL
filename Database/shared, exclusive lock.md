# Shared lock과 Exclusive lock에 관하여

> 해당 lock들은 DB자체에서 걸어주는 lock으로, InnoDB의 Row-level lock이다.

[이거](https://hello-backend.tistory.com/110)랑 [이거](https://hello-backend.tistory.com/213)랑 [이거](https://hello-backend.tistory.com/214)를 읽고오면 좀더 이해가 쉬울 것이다.

간단히 예를 들어 설명하겠다.

예를 들어 DB에

![](https://i.imgur.com/qNUAUWv.png)

다음과 같은 데이터가 있다고 가정해 보자

그런데 만약 

* A는 류찬의 외모를 읽음
* B는 류찬의 외모를 `잘생김`에서 `멋짐`으로 변경
* C는 류찬의 외모를 읽음

한다고 생각해보자.

![](https://i.imgur.com/x642FDG.png)

1. A Transaction에서 류찬의 외모를 가져오는 중(잘생김)
2. B Transaction에서 류찬의 외모를 멋짐으로 변경(멋짐으로 변경)
3. C Transaction에서 류찬의 외모를 가져옴 (멋짐)
4. A Transaction에서 류찬의 외모를 가져옴 (잘생김)

이렇게 되는 이유는, A Transaction이 해당 데이터를 선점했음에도 B Transaction이 이를 변경했고, C Transaction은 이 변경된 데이터에 접근하여서이다.

이 때문에 데이터의 일관성이 깨지게 된다.
그럼 이걸 어떻게 해결할 수 있을까?

## Shared lock(공유 잠금)

> Read lock

읽기 잠금이라고도 불리는데, 말 그대로 하나의 트랜잭션에서 데이터를 읽어오는 동안 `다른 트랜잭션에서 변경할 수 없도록` 잠금을 걸어주는 것이다.

위의 예시에서 생각한다면, A Transaction에서 읽어오는 것이 완료되기 전까지, B는 막아주는 것이다.
근데 이는 변경은 막는데, 읽는건 막지 않는다.
즉 C Transaction은 그대로 시행된다.

이 이유는 [이거](https://hello-backend.tistory.com/203) 를 보면 알 수 있다(사실 둘은 거의 비슷한 내용을 다루고 있다.)

어차피 변경은 되지 않고, 여러 트랜잭션을 한번에 수행함으로서 성능 향상을 꾀하는 것이다.

## Exclusive lock(배타 잠금)

> Write lock

읽기 잠금이라고도 불리는데, 말 그대로 하나의 트랜잭션에서 데이터를 변경하는 동안 `다른 트랜잭션에서 읽거나 변경할 수 없도록` 잠금을 걸어주는 것이다.

참고로 이거는 변경하는 동안 다른 모든 내용이 차단된다.
이유는 굉장히 당연하다.

* A는 류찬의 외모를 잘생김 -> 멋짐으로 변경한다.
* B는 류찬의 외모를 읽어온다.
* C는 류찬의 외모를 읽어온다.

라고 가정해 보면

![](https://i.imgur.com/aAluIhI.png)

1. A Transaction이 류찬의 외모를 변경하는 중(잘생김 -> 멋짐)
2. B Transaction이 류찬의 외모를 확인함(잘생김)
3. C Transaction이 류찬의 외모를 확인함(멋짐)

같은 데이터를 확인했는데도, 언제 변경이 이루어졌냐에 따라 원하는 값을 얻지 못할 수 있다.

그렇기 때문에 Exclusive lock은 이름 그대로 배타적으로, 다른 Transaction의 접근 자체를 막아버린다.
