# java에서 String & StringBuffer & StringBuilder에 관해

## String

먼저 java에서 String을 쪼개어 보면 이렇게 나온다.  
`String -> final char[]`  
이걸 보면 자바에서 string은 final인 char의 배열로 선언되어 있다.  
즉, String의 경우는 문자를 선언하게 되면 이것이 final형식으로 불변객체로 선언되게 된다.

String에서 +연산자를 사용해 본 적은 있어도, -연산자롤 사용해 본 사람은 없을 것이다.  
이는 String의 경우는 String pool영역에 있는 불변 객체이기 때문이다.

그렇다면 +연산자는 된다면.. String에 어떠한 값을 더하면 어떻게 될까?

```
String str = "hell";
str += "o";
str += "!";
```

이렇게 구하는 경우 당연히 우리는 str이 hello! 가 됨을 알고있다.  
그런데 String은 final로 선언된 불변객체라고 하였다.  
따라서 이 경우는 실제로

![](https://i.imgur.com/L4mxkvG.png)

  
이렇게 불변의 객체가 하나씩 생기게 되며, 이 중 참조할 내용이 없는 \[ hell \], \[ hello \] 두 개는 가비지 컬렉션의 대상이 된다.

이는 당연히 자바의 메모리에 있어 좋지 않은 영향을 끼치게 될 것이다.

## StringBuilder / StringBuffer

StringBuilder / StringBuffer은 이런 String의 문제 해결에 사용된다.  
먼저 StringBuilder / StringBuffer은 String과 다르게 가변 객체이다.  
즉, 이들은 문자열의 삭제가 가능하다는 것이다.

예를 들어

`StringBuilder s = new StringBuilder("hello backend!!");`

가 주어졌을 때, hello를 지우기 위해서는  
`s.delete(0, 6);`  
를 해 주면 된다.

이는 StringBuilder / StringBuffer가 String과는 다르게 가변 객체이며, heap영역에서 바로 해당 메모리에 접근할 수 있기 때문이다.

따라서 문자열의 변경이 자주 일어나는 경우 String보다는 StringBuilder / StringBuffer를 사용하는 것이 메모리적으로 이득을 볼 수 있을 것이다.

### StringBuilder / StringBuffer 둘의 차이는 뭘까??

간단히 말하자면 StringBuffer는 Thread-safe하고 StringBuffer는 그렇지 않다는 것이다.  
이 Thread-safe에 관해서는 [이전 게시물](https://hello-backend.tistory.com/110)을 읽어 보면 이해할 수 있을 것이라 생각한다.

## 결론

String의 경우는 불변 객체이다.  
문자열의 변경 등이 자주 있는 경우 StringBuilder / StringBuffer가 당연히 String보다 효과적이지만 이 불변 객체라는 특성을 이용하면 문자열을 자주 읽어야 하는 경우 String이 가장 좋은 방법일 것이다.

참고로 String은 불변성을 가진 객체이기 때문에 당연히 thread-safe하다.

즉,  
**문자열의 변경이 적고 문자열을 읽어는 경우가 많을 때에는 String**  
**문자열의 변경이 잦고 단일쓰레드인 경우 StringBuilder**  
**문자열의 변경이 잦고 멀티쓰레드인 경우 StringBuffer**

를 사용하는 것이 좋을 것이다.
