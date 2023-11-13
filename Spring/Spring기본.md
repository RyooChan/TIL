## IoC 컨테이너

> Inversion of Controll

말하자면 `제어의 역전` 이라는 것이다.
그래서 그게 뭔데? 라고 하면

말하자면 "주도권을 뺴앗겨서, 그 주도권이 Spring에게 있다."는 뜻이다.

예를 들어

* Class
    * 설계도
* Object
    * 실체화가 가능한 것
    * 예를 들어 abstract class처럼 실제로 존재하지 않는 것들은 Object가 될 수 없다.
* Instance
    * 실체화 된 것

이런 3개에 대하여 생각해 보았을 때에

보통은 java에서

```
Public void MakeHandsomeMan() {
    Ryoochan ryoochan = new Ryoochan();
}
```

이런 식으로 직접 heap영역에 만들었을 것이다.
그러면 `MakeHandsomeMan` 에서만 `ryoochan`이 존재하고, 다른 곳에서는 사용할 수 없을 것이다.

지금처럼 하면 기존에 만들어진 Instance를 다른 메서드에서 사용하기 힘들 것이다.
그래서 Spring에서는 IoC를 통해 다양한 Object를 미리 heap영역에 띄어준다.

## DI

> Dependency Injection

-> 위의 IoC에서, Spring은 여러 Object를 미리 Heap영역에 띄어준다고 했는데, DI를 통해 원하는 곳에서 바로 공유해서 쓸 수 있게되는 것이다.
-> Singleton으로 사용!!

이거는 사실 지금처럼 객체를 생성하는것뿐 아니라, Setter / Getter이나 혹은 Service, Repository같은 것의 주입 등에서 많이 보아 알게 될 것이다.

참고로 만약 우리가 직접 뭔가 새로운 객체를 만들 때에

* 류찬
    * 외모
    * 성격

이렇게 있다고 치고

* 외모
    * 키
    * 몸무게
* 성격
    * 의지력
    * 집중력

등등, 하나의 객체가 다른 객체를 받고, 또 이 객체가 뭔가를 받고 ..... 이런 식으로 반복되면 하나의 객체를 만들 때에 모든 객체에 대한 의존관계가 필요해질 것고, 많은 객체가 중복생성되게 될텐데, 이거를 `Bean`을 만들어 외부 주입이 가능하게 될 것이다.

## Bean

> Spring IoC Container를 통해 관리되는 객체(Object)

말하자면, 어떻게 Bean을 등록하면(XML, Config, annotation 등..) 그거를 IoC가 보관하고, 필요할 때에 꺼내서 쓸 수 있는 것이다.

이는 Singleton으로 값을 주게 되는데,

* Singleton
    * 인스턴스가 오직 1개만 생성된다.
    * 즉, 누군가가 Bean을 통해 하나의 객체를 보내준다.
        * 그렇기 때문에 객체는 항상 Stateless해야한다.(상태나 값의 변경이 일어나서는 안된다. -> 아예 그런 필드를 만들지 않아야 한다)
            * 근데 `scope(prototype)`를 쓰면 Stateful하게 만들 수 있다. 
                * IoC Container에서 관리하지 않는다.
                * 매번 요청할 때에 생성되고, 이것은 생명 주기를 Spring이 아니라 해당 메서드에서 관리하게 된다.
        * 가급적 read-only
