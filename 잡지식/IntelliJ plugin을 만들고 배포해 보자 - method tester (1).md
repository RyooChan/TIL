# IntelliJ plugin을 만들고 배포해 보자 - method tester (1)

**(이 내용은 어떤 플러그인을 만들까에 대한 내용이다. 만드는 방식은 이후에 적을 예정이다.)**

개발을 하면서 프로젝트의 크기가 커지고 기능을 잘 나누어 둔다면 하나의 메서드를 여기저기서 재활용하는 경우가 참 많아지게 된다.

그리고 이를 처음에 만들 때에는 잘 만들어 두었고 더이상 변경의 여지가 없을 거라고 생각했는데, 어떠한 이유로 리팩터링을 진행할 수 있다.

근데 그렇게 되면 이게 잘 고쳐졌는지, 다른 로직에 이상이 없는지를 바로 테스트하기가 참 애매하다.

예를 들어, 다음과 같은 클래스가 있다고 가정해 보자.

```
import org.springframework.stereotype.Component;

@Component
public class ExampleMethod {
    public void methodForTest() {
        this.usedFirstDepth();
    }

    public void useFirstDepth1() {
        this.methodForTest();
    }

    public void useFirstDepth2() {
        this.methodForTest();
    }

    public void useSecondDepth() {
        this.useFirstDepth1();
    }

    public void usedFirstDepth() {
        this.usedSecondDepth();
    }

    public void usedSecondDepth() {}
}

```

내가 methodForTest() 라는 것을 확인하려고 본다면 이녀석은

- useFirstDepth1
- useFirstDepth2

메서드에서 호출된다.
그리고 다시 useFirstDepth1의 경우는

- useSecondDepth

메서드에서 호출된다.

methodForTest는 usedFirstDepth를 호출하고 있고, 이게 다시 usedSecondDepth() 를 호출한다.
논리적으로 생각했을 때에 `methodForTest` 의 영향도를 조사하고 싶다면, **이게 호출한 것이 아니라 이걸 호출한 모든 메서드에 대한 테스트를 하는게 맞을 것이라는 생각이 든다.**

그래야 변화를 직접적으로 보는 것들에의 테스트가 가능한 것이다.

```
import org.junit.Test;

public class ExampleMethodTest {
    ExampleMethod exampleMethod = new ExampleMethod();

    @Test
    public void methodForTest() {
        exampleMethod.methodForTest();
    }

    @Test
    public void useFirstDepth1() {
        exampleMethod.useFirstDepth1();
    }

    @Test
    public void useFirstDepth2() {
        exampleMethod.useFirstDepth2();
    }

    @Test
    public void usedFirstDepth() {
        exampleMethod.usedFirstDepth();
    }

    @Test
    public void useSecondDepth() {
        exampleMethod.useSecondDepth();
    }

    @Test
    public void usedSecondDepth() {
        exampleMethod.usedSecondDepth();
    }
}
```

이런 식으로 테스트가 있다면 즉

- methodForTest (자기자신의 테스트))
- useFirstDepth1
- useForstDepth2
- useSecondDepth

이렇게 4개에 대해서만 테스트가 수행되어야 한다는 것이다.

어떻게 구현할지를 생각해 보면

1. 하나의 메서드를 통해 뭔가 액션을 취할 수 있도록 해야한다.
2. 그 메서드가 직접 사용된 테스트를 찾는다.
3. 그리고 이 메서드를 사용하는 모든 메서드를 확인하고, 이것들의 테스트를 찾는다.
4. 찾은 테스트를 한꺼번에 동작시킨다.

요렇게 하면 될 것 같다는 생각이 든다.
이제 이거를 다음부터 kotlin base의 IntelliJ plugin을 통해 개발하고 배포해 보려고 한다.
