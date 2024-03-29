# Default, Static method in java(feat. JDK8, 그 이후)

인터페이스를 공부하다 보면 자주 나오는 메서드들이다.
어떤 것인지, 어떻게 쓰이는지 차이를 알아보자.

## java interface

먼저 인터페이스에 관한 기본적인 설명은 [얘](https://hello-backend.tistory.com/137)랑 [얘](https://hello-backend.tistory.com/208)를 보면 대충 감이 잡힐 것이라 생각한다.

기존에는 interface를 만들고, 이를 구현하는 부분을 따로 implements하여 작성해 주어야 했다.

## JDK 8에서의 변화

JDK8부터 interface에서 default, static 메서드의 사용이 가능해졌다.

### default method

Default가 뭔데? 싶을 수 있을 것이다.
사실 이름 그대로 **기본적으로** 기능을 갖고 있는 메서드를 뜻한다.

예를 들어, 류찬의 `외모 설명` 이라는 메서드를 생각해 보자.
사람에 따라 "장난아니다", "멋지다", "나이스하다" 등 여러 설명을 할 수 있을 테지만, 기본적으로 그는 "잘생겼다"를 가지고 있을 것이다.

그래서 누군가가 '류찬의 외모 설명'을 호출하면, 따로 정의하지 않은 상태에서는 "잘생겼다"가 호출되도록 하는 것이다.

#### Default Method 정리하자면

* 특징
    * 재정의 자체는 가능하다. (\@Override를 사용한 재정의)
    * 따로 재정의하지 않고 사용하면 기본으로 작성된 결과가 사용됨
* 장점
    * 매번 상속받을 때마다 귀찮게 메서드를 재정의할 필요가 없게 된다.
* 단점
    * Default Method가 Abstract Method를 사용 중인 경우, 문제가 생길 수 있다.
        * 예를 들어 `류찬의 bmi`가 Default Method이며 하위 Abstract Method를 상속받을 때
            * `류찬의 몸무게` <- 상속받는 체중계마다 다르게 구현
            * `류찬의 키` <- 상속받는 줄자마다 다르게 구현
                * 줄자가 2m가 안되는 경우(류찬의 키보다 짧은 경우) 입력 불가 <- null
                    * 이 경우 NPE 발생!!


### Static Method

이제는 아마 Static이 감이 잡힐 것이다.
그냥 재정의 없이 사용하는 **정적**인 메서드가 바로 static method이다.

예를 들어, 류찬의 `IQ수치` 라는 메서드를 생각해 보자.
류찬의 IQ수치는 200이다. 이는 변할 수 없는 사실이다.
따라서 누군가가 '류찬의 IQ수치'를 호출하면, 무조건 "200"이 불리게 되는 것이다.

#### Static Method 정리하자면

* 특징
    * 재정의가 불가능하다.
* 장점
    * 한번 정의하고 사용하면 된다.
* 단점
    * 한번 정의하면 변경이 안됨.

## JDK8 다음 버전부터...

위에서 설명한 인터페이스의 기능들은 기본적으로 public이다.
어쩌면 당연할 수도 있는데, 인터페이스의 메서드들은 설계도의 역할을 갖기 때문에 외부에서 사용할 것을 고려했기 때문이다.

그런데 JDK8 이후 static, default를 사용해서 인터페이스 내에서 기능을 구현하게 되었다.
그렇기 때문에 이러한 기능들을 내부에서 구현하고, 이 내용들은 외부로 굳이 보여줄 필요가 없을 것이다.
이들을 helper, 혹은 utility라고 부르는데 얘들을 노출시키지 않으려고 private으로 만들면 좋을 것이다.

JDK8에서는 이 기능이 없었는데, JDK9부터 interface내에서 private의 사용이 가능해졌다.

그래서 이제

[대충 요런](https://hello-backend.tistory.com/207)식으로 기능을 바로바로 구현해 줄 수 있게 되었다.

## 결론

어지간하면 JDK11버전 이후로 쓰자.
근데 JDK17이 제일 지원이 오래 된다 하니 걍 JDK17쓰자.

끗.
