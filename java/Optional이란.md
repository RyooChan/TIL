# Optional이란??

Spring Data JPA를 이용해서 개발을 할 때에, [find메서드](https://hello-backend.tistory.com/157)를 사용하면 Optional이 기본적으로 나올 것이다.

Optional이 일단 무엇일까??

> T타입의 객체를 감싸는 generic 형태의 Wrapper클래스

Optional은 모든 타입의 참조 변수를 담을 수 있다.

```
public final class Optional<T> {
    private final T value;    // T타입의 참조변수
}
```

이런 식으로, 참조 변수들을 그냥 반환하는 것이 아니라, Optional로 한번 감싸서 보내주는 것이다.
이걸 왜 해줄까??

만약 바로 객체를 보내준다 가정하면
없는 객체를 가져와서 확인하려 하면?

1. 객체 가져옴
2. 객체가 없음
3. 사용하려 한다
4. 에러!!

이렇게 된다.
따라서 보통 객체를 바로 가져오는 경우 이게 null인지를 매번 체크해줘야 한다.

그래서 그냥 아예 Optional로 감싸주고, 여기 설정된 메서드를 통해 간단하게 처리해줄 수 있도록 하는 것이다.

## Optional 생성

`of()` 혹은 `ofNullable()`을 확용한 생성

* of
    * 매개변수의 값이 null이면 NPE발생
* ofNullable
    * 매개변수가 null이어도 괜춘

```
import java.util.Optional;

public class Ryoochan {
    public static void main(String[] args) {
        Optional<String> ryooChanNotGood = Optional.of(null);
        Optional<String> ryooChanVeryGood = Optional.ofNullable(null);
        
        System.out.println(ryooChanNotGood);    // NPE!!!
        System.out.println(ryooChanVeryGood);    // 빈 값이 나타남
    }
}
```

## 초기화!

`empty()` 메서드를 사용하여 초기화가 가능하다.
참고로 null보다는 이런 방식으로 초기화하는것이 권장된다.


```
import java.util.Optional;

public class Ryoochan {
    public static void main(String[] args) {
        Optional<String> ryooChanNotGood = null;
        Optional<String> ryooChanVeryGood = Optional.<String>empty();
        
        System.out.println(ryooChanNotGood);    // 빈 값이 나타남
        System.out.println(ryooChanVeryGood);    // 빈 값이 나타남
    }
}
```

## 값 가져오기

`get()`을 사용하여 가져온다.
null에서 가져오는 경우 NPE가 발생하므로 orElse()로 막을 수 있다.

```
import java.util.Optional;

public class Ryoochan {
    public static void main(String[] args) {
        Optional<String> ryooChanVeryGood = Optional.<String>empty();
        
        String handsome = ryooChanVeryGood.orElse("handsome!!");
        System.out.println(ryooChanVeryGood.get());    // handsome
    }
}
```

이런 식으로, 미리 없을 경우 출력될 값을 지정할 수 있다.
참고로 이거 말고도

* orElseGet
    * 람다식 지정 가능!
* orElseThrow
    * 예외 발생
        * 개발자가 예외 핸들링 가능

이런 것들이 있다~

참고로 한가지 더, `orElse`랑 `orElseGet`의 재미있는 차이가 있는데

## orElse랑 orElseGet의 차이!!

```
import java.util.Optional;

public class Ryoochan {
    public static void main(String[] args) {
        Optional<String> ryoochan = Optional.ofNullable("잘생겼다...");
        
        String chan1 = ryoochan.orElse(handsome());
        System.out.println(chan1);
        
        System.out.println("----류찬 그는 대체----")
        
        String chan2 = ryoochan.orElseGet(Ryoochan::testMethod);
        System.out.println(chan2);
        
    }
    
    static String character() {
        System.out.println("성격도 좋다..")
        return "chan!";
    }
}
```

이렇게 하면 어떻게 나올까??

```
성격도 좋다..
잘생겼다...
----류찬 그는 대체----
잘생겼다...
```

이렇게 나온다!!
뭐지? 싶은데 이거는

* orElse()
    * 해당 optional이 null이건 아니건 상관없이 집어넣은 method(여기서는 `character`)을 무조건 실행한다.
* orElseGet()
    * null일때만 method 실행!!

이다!!
이거를 잘 기억해야 할듯 싶다.
