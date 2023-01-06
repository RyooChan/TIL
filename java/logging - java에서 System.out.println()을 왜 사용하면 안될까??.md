# logging - java에서 System.out.println()을 왜 사용하면 안될까??

보통 현업에서 일할때에 **절대** `System.out.println()`을 쓰지 말라는 말을 듣는다.
-> 가급적 쓰지말자~ 가 아니라 절대다!!

대신 로깅을 진행할 때에는 주로 [얼마전에 문제](https://hello-backend.tistory.com/109)가 있었던 Log4j를 사용하고는 한다.

왜일까??

## Sync문제 발생

한번 System.out.println()의 구현 코드를 살펴보도록 하자.

![](https://i.imgur.com/F8NtN5y.png)

![](https://i.imgur.com/9ttXcII.png)

![](https://i.imgur.com/MMrrw7y.png)

다양한 내부 코드들이 있는데, 이거는 보면 동작 과정에서 `synchronized`를 사용해서 동기화를 걸어준다.(writedln도 물론 `synchronized`가 걸려있는것을 확인 가능하다.)
즉, 해당 코드가 작동할 때에 다른 쓰레드는 작동이 불가능하다는 것이다!!

따라서 이 System.out.println()을 사용하는 순간 성능에 제약이 생겨버리게 된다는 단점이 있다.

## Log4j의 장점

### 정보 제공의 다양성

Log4j는 다양한 정보를 제공하는데

* 문제 발생 시각
* 문제 수준
* 로그 발생 위치

등등등... 로깅에서 필요로 하는 다양한 정보를 제공해 준다.

### 상황 설정 가능

또한, log4j는 로그의 출력 레벨을 설정해줄수 있다.
보통 디버깅을 할 때에는 환경에 맞춰서

* TRACE
* DEBUG
* INFO
* WARN
* ERROR
* FATAL

와 같은 식으로 설정해 줄 수 있기 때문에 로그를 직관적으로 파악할수 있다!!

## 결론

System.out.println대신에 log4j와 같은 로거 프레임워크를 사용하면

* 성능상으로 좋음
    * Thread-safe하니까 써도됨~
* 로그 정보를 잘 전달할 수 있음.
* 로그를 상황에 맞춰서 나누어 사용 가능

등등의 이점이 있으므로 그냥 로거 프레임워크를 쓰자!
