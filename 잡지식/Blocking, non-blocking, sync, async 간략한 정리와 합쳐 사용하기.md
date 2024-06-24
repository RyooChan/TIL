## Blocking, non-blocking, sync, async 간략한 정리와 합쳐 사용하기

### 간단한 정리

- Blocking
    - 작업이 완료될 때 까지 호출한 쓰레드가 대기한다.
- Non-blocking
    - 작업이 완료되지 않아도 쓰레드가 대기하지 않는다.
        - 즉, 응답이 돌아오지 않아도 다른 작업을 할 수 있다.
- Sync
    - 작업이 완료될 때 까지 호출한 함수의 실행을 블로킹.
        - 제어권 반환 X
- Async
    - 작업이 완료되기 전에 제어권을 반환하여 호출한 함수의 실행을 블로킹하지 않는 방식.

뭐 이렇다고 한다.
이렇게 보면 뭔소리야? 싶은데 조금 더 상세히 알아보자.

### 세부적인 차이

저 둘의 차이는 결국 "호출된 함수" 와 "호출한 함수" 에서 온다고 생각한다.

- Blocking / Non-blocking
    - 이 둘은 호출한 쪽이 대기하는지 여부가 중요하다.
    - 즉, 호출한 쪽이 응답을 받을 때까지 대기하고 있는지, 혹은 다른 작업을 하고 있는지이다.
- Sync / Async
    - 이 둘은 호출된 쪽에서 제어를 돌려주는지 여부가 중요하다.
    - 즉, 호출된 쪽에서 작업이 마무리될 때 까지 호출한 쪽의 함수 실행을 브로킹하는지 여부이다.

약간 감이 잡히는데, 이거는 예시를 들어 설명하는게 더 좋다는 생각이다.

### 예시

예시를 들어보자.
호텔에 전화해서 빈 방이 있는지를 물어본다고 한다면?

- Sync
    - 빈 방이 있는지 물어보면 호텔 접수원이 예약 가능한 시간을 찾아주면서 대기시키고, 조회 후에 방 결과를 보여준다.
- Async
    - 빈 방이 있는지 물어보면 호텔 접수원이 빈 방을 찾아보겠다고 하고 전화를 끊는다. 그리고 찾은 후에 전화한다.
- Blocking
    - 물어본 후에 빈 방 여부를 받기 전까지 가만히 있는다.
- non-blocking
    - 물어본 후에 빈 방 여부를 받기 전까지 다른짓을 하고 있고, 빈 방 여부 응답이 돌아왔는지 한번씩 확인한다.

이렇게 한다면 조금 이해가 잘 되는 것 같다.
그리고 각 내용을 섞어서 하는것도 이해가 간다.

### 복합 예시

- Sync + Blocking
    - 빈 방이 있는지 물어보고 응답이 돌아올 때까지 딴짓하지 않고 전화에 온 신경을 집중한다.
    - 접수원은 빈 방을 찾을때까지 전화를 끊지 말라 하고 찾아서 알려준다.
- Sync + Non-blocking
    - 빈 방이 있는지 물어보고 응답이 돌아올 때까지 딴짓하고 있는다. ~~나는 주로 이때 핸드폰으로 온라인 폐지를 줍는다.~~
    - 접수원은 빈 방을 찾을때까지 전화를 끊지 말라 하고 찾아서 알려준다.
    - 딴짓하면서 중간중간 전화를 듣가자 접수원이 고객님? 하면 오 네 하고 들으면 된다.
- Async + Blocking
    - 빈 방이 있는지 물어보고 응답이 돌아올 때까지 딴짓하지 않고 전화에 온 신경을 집중한다.
    - 접수원은 빈 방 찾아오겠슈 하고 끊는다. 찾고나면 전화한다.
    - 온 신경을 전화하다가 전화가 울리면 바로 전화를 받고 확인한다.
- Async + Non-blocking
    - 빈 방이 있는지 물어보고 응답이 돌아올 때까지 딴짓하고 있는다.
    - 접수원은 빈 방 찾겠다 하고 끊는다. 찾고나면 전화한다.
    - 딴짓하다가 전화벨이 울리는 소리를 캐치하면 전화를 받고 확인한다.

이와 같은 방식으로 예를 들 수 있을 것이다.
음 괜찮네...

### Async + Blocking 이거 맞아?

여기서 다른것들은 분명 이유가 있어 보이는데(괜찮아 보인다) Async + Blocking 이거는 뭐하는 짓이지? 하는 생각이 든다.
그래서 나도 흠 이게 쓰이는 경우가 있을까? 하고 좀 찾아봤었는데...

[여기서](https://keyurramoliya.com/posts/Asynchronous-Programming-in-C-with-async-await/) 몇몇 안티패턴이 소개되었는데

![image](https://github.com/RyooChan/TIL/assets/53744363/6cd378de-f3a6-4ae0-afde-359956ee4d24)

```
Some of the common asynchronous programming anti-patterns are:

Async over sync: This anti-pattern is used when you want to wrap a synchronous method in an async method without actually making it asynchronous. You can avoid this anti-pattern by using existing asynchronous methods or creating your own asynchronous methods that use Task.Run or other mechanisms.

```

이런 이야기가 있었다.
간략하게 말하자면 비동기적이지 않은 동기 메서드를 비동기처럼 감싸는게 안티 패턴이라는 것이다.
근데 이게 딱 여기 맞는 이야기였던 것 같다. 왜냐면 비동기 메서드를 사용해놓고 실제로는 비동기로 동작하지 않기 때문이다.

==> 즉 이거는 걍 안쓰면 될 것 같다.

### 결론

- 사실 blocking / non-blocking 이랑 sync / async 둘이 차이가 있다는건 알고 있었는데 뭐 큰 차이가 있나..? 했었다. 근데 차이가 생각보다 크다.
- 위의 내용을 조금씩 본다면 어떤 상황에는 무엇이 더 효율적인지를 따져서 개발이 가능해 보인다.
    - Async + Blocking 빼고.
