# Java의 Optional 클래스

자바 사용중 가장 많이 만나는 에러 중 하나가 바로 널 에러이다.(NPE - Null Pointer Exception)
개인적으로 SPringboot의 JPA를 사용해서 값을 가져올 때에 이 null처리에 많은 신경을 써야 한다고 생각한다.

> Java8 이전까지 NPE를 해결하기 위해서는 두 가지들 중 하나를 택해야 했다.
* 예외 출력
    * 예외를 던지는 것인데, 이것은 정말 예외가 발생하는 상황에 사용하도록 한다.
* null 반환
    * null을 실제로 받아오는 것으로, 클라이언트에서 별도로 처리해야 한다.

이런 식으로 처리하였는데, 이 로직을 수행하는 데에 코드가 복잡해 질 수도 있기 때문에 초기값을 사용하는 경우도 있었다.

> Java8 버전부터 Optional클래스를 사용하여 이 NPE를 해결할 수 있도록 도와준다.

## Optional 클래스란?

null처리를 기존에 비해 간편하게 할 수 있도록 도와주는 클래스

```
public final class Optional<T> {

    private Optional() {
        this.value = null;
    }
}
```
Optional클래스를 살펴보면, 이는 final 클래스임을 알 수 있다.
즉, 이는 다른 객체의 부모 클래스가 되지 못한다.
또 내부의 Optional() 생성자가 private로 되어 있어 객체 생성이 제한된다.

## Optional 클래스 객체 만들기


```
import java.util.Optional;

public class optionalTest {
    public static void main(String[] args) {
        Optional<Object> emptyOption = Optional.empty();   
        Optional<String> nullString = Optional.ofNullable(null);     
        Optional<String> notNullString = Optional.of("exist!");  
    }
}
```

이렇게 optional기능을 테스트하기 위한 세팅을 해보자
처음의 emptyOption에서는 Optional.empty()를 통해 완전히 빈 데이터를 생성했다.
다음으로 nullString에서는 ofNullable()을 사용했는데, null이 추가될 수 있는 상태이다.
마지막 notNullString은 데이터의 존재를 확신할 때 사용한다. -> Stirng값이 무조건 있다.

## Optional 클래스 값 꺼내기
Optional은 값을 꺼내기 위해
get, orElse, orElseGet, orElseThrowable 를 사용한다.

* get
    * 가장 기본적인 메소드로서 데이터를 가져오거나, 없는 경우 null을 return한다.
* orElse
    * 저장된 값이 존재하면 해당 값을, 존재하지 않는 경우 인수로 전달된 값을 return한다.
* orElseGet
    * 저장된 값이 존재하면 해당 값을, 존재하지 않는 경우 인수로 전달된 람다 표현식의 결과값을 return한다.
* orElseThrowable
    * 저장된 값이 존재하면 해당 값을, 존재하지 않는 경우 인수로 전달된 예외 발생

## Optional 사용시 주의사항
Optional은 이렇게 null을 처리하는 다양한 방법을 제공하고, 기존의 null에서 생기는 문제점을 해결하는 방법이다.

다만, 이 Optional클래스는 null을 감싸서 처리하는 Wrapper클래스이기 때문에, 메모리의 낭비가 발생할 여지가 크다.
또 NPE대신에 NoSuchElementException이 발생할 수도 있다.

그렇기 때문에 Optional클래스는 여기저기 남발하지 말고 필요할 때에만 사용해야 한다.
또 사용에 있어 몇 가지 주의사항이 있다.

* Optional변수에 Null 할당하지 말기
    * Optional변수에 null을 할당하게 되면 이 변수가 null인지 다시 검사하게 되는데, 낭비가 심해지기 때문
* 명확한 값이 있으면 Optional 사용하지 않기
    * null의 발생 가능성이 있고, 시스템에 영향이 있을 때에 사용하도록 하자.
* 반환 타입으로만 사용하기
    * 반환 중 Null에러 발생 -> 결과 없음
        * 사실상 이 역할로 사용하는 것이기 때문에 반환 타입으로 사용해주면 된다.
* Collection에서는 사용하지 않기
    * 그냥 빈 Collection을 반환하면 되는데 Optional을 쓸 이유가 없다.

등등...

좋은 기능이지만, 모르고 쓰면 없으니만 못하다.
특히 요즘은 IntelliJ를 사용했을때 자동 완성을 하면 Optional을 자주 만들어 주는데, 이걸 만들어준다고 무턱대고 쓰면 안된다.
