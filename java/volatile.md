# java의 volatile

> java변수를 Main Memory에 저장하겠다는 것이다.

이게 뭐냐...하면 말그대로 main memory에 값을 저장한다는거다.
사실 이거 이해하려면 [cache](https://hello-backend.tistory.com/200)에 대해서 조금 알고오면 편하다.

자바는 사실 값을 저장하거나 읽어올 때에 이거를 CPU cache를 통해 진행한다.
이렇게 되면 속도에 이득이 필요하지만... 멀티 쓰레드 환경에서 좀 문제가 있을 수 있다.


## 멀티 쓰레드에서의 CPU cache 문제

MultiThread에서는 보통 Task동안 Main Memory에 있는 값을 CPU에 Caching한다.
그리고 이거를 읽거나, 쓰거나 등등의 연산을 취하게 되는데

1. Main Memory에 특정 값이 저장되어 있다. (RyooChan = "Genius")
2. 1번 쓰레드가 Main Memory에서 해당 값을 가져와서 변경한다. (RyooChan = "Handsome") -> 근데 이거는 1번 쓰레드 내에서만 변경되고 Main에서의 변경은 이루어지지 않은 상태이다.
3. 2번 쓰레드가 Main Menory에서 해당 값을 가져와 읽는다. (RyooChan = "Genius")

이렇게 되면 RyooChan의 값은 1번 쓰레드를 통해 이미 `Handsome`으로 변경되었음에도 불구하고, 2번 쓰레드는 아직 캐싱된 값이 적용되기 전인 `Genius`를 가져와 읽게 된다.
이렇게 각 쓰레드에서의 값의 불일치가 발생하게 되는 것이다.

## volatile을 쓰면??

바로바로 Main에 쓰거나 읽는다면?

1. Main Memory에 특정 값이 저장되어 있다. (RyooChan = "Genius")
2. 1번 쓰레드가 Main Memory에서 해당 값을 변경한다. (RyooChan = "Handsome")
3. 2번 쓰레드가 Main Menory에서 해당 값을 가져와 읽는다. (RyooChan = "Handsome")

이런 식으로 바로바로 변경하면, 값의 불일치가 없다!!

그럼 이거 단점은 뭘까?

### 단점

뻔하겠지만, Cache를 전혀 안쓰니까 성능이 안좋아유...

그리고 한가지 더, 좀 치명적인 문제가 있는데

만약에 두개의 쓰레드가 다 값을 변경한다면??

1. Main Memory에 특정 값이 저장되어 있다. (RyooChan = "Genius")
2. 1번 쓰레드가 Main Memory에서 해당 값을 변경한다. (RyooChan = "Handsome")
2. 2번 쓰레드가 Main Menory에서 해당 값을 변경한다. (RyooChan = "Smart")
3. 지금 위의 내용이 동시에 이루어진다면?

1번 쓰레드에서는 `RyooChan`이 `Handsome`하다는 값을 저장하고 싶었고
2번 쓰레드에서는 `RyooChan`이 `Smart`하다는 값을 저장하고 싶었는데

동시에 행해져서 둘중 하나만 적용되게 된다...

## 그럼 언제쓰는게 좋아요?

* 성능이 안좋음
    * 꼭 필요할때만 쓰자
* 두 값이 동시에 변경되는 경우 하나만 적용됨
    * 그럼 하나만 값을 변경하는 경우에 쓰면 되겠네?

이 두개를 통해 알수 있는건

> 하나의 쓰레드는 `write`, 다른 쓰레드들은 `Read`하는 경우에 쓰는게 좋을 것이다.
