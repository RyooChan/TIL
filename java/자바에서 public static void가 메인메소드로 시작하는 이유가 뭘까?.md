# 자바에서 public static void가 메인메소드로 시작하는 이유가 뭘까?

요즘 GoLang을 공부중인데 이친구는 main.go에서부터 시작한다.

근데 그러다가 자연스럽게 java는 `public static void main(String[] args)` 에서부터 시작하는데 그 이유가 뭘까 궁금하여 찾아보게 되었다.

```
package ryoochan;

public class RyoochanHandsome {
	public static void main(String[] args) {
	
	}
}
```

처음에 프로젝트를 시작하면 이렇게 초기 코드가 생성된다.
그래서 이게 뭔데....? 싶을 것이다. 내가 그랬음.

일단 하나씩 까보자

## public

이거는 뭐 다들 알것이다.
Spring으로 개발을 해봤으면 알텐데 접근 제어자중 하나이다
접근 제어자는

private -> protected -> public

요렇게 3개가 있는데 public이면 어디서든 이걸 참조할 수 있다든 것이다.
말하자면 어플리케이션 어디서든 요 메인메소드를 실행시킨다는 것이다.

## static

이름을 보면 알 수 있듯 정적 함수라는 것이다.
이거는 자바가 컴파일 되는 시정에 정의되고, 이러한 static함수를 non-static에서 호출할 수는 없다.

이게 뭔소리냐면 main 메서드는 가장 먼저 수행되기 때문에 객체 생성 이전에 작업을 수행하는 것이다.
그래서 static으로 설정함

## void 

반환 타입이 없다.
