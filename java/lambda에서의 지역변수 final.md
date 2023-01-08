# lambda에서의 매개/지역 변수

> lambda식에서 매개변수와 지역 변수는 final의 특성을 가져야 한다.

즉, 람다식에서 매개변수/지역변수를 읽어오는 것은 가능하지만, 값을 변경하는 것은 불가능해야 한다는 것이다.

## final, Effectively Final

한번 일단 작성해보자

```
@FunctionalInterface
public interface SumInterface {
    public int sum(int x, int y);
}
```

이런 FunctionalInterface가 있고

```
public void test() {
    int a = 5;
    int b = 10;
    SumInterface si = (x, y) -> (x+y);
    int ans = si.sum(a, b);
    System.out.println(ans);
}
```

이런 식으로 sum을 해주는 내용을 만들어 줬다고 생각해 보자.
근데 여기서 만약에

```    
public void test() {
    int a = 5;
    int b = 10;
    SumInterface si = (x, y) -> ((x+y)+b);
    int ans = si.sum(a, b);
    System.out.println(ans);
}
```

이런 식으로 해준다면
sum의 로직은 x와 y를 더해준 다음에 b를 무조건 더해주는 것이다.

근데 만약에 이 로직이 끝나고

```
public void test() {
    int a = 5;
    int b = 10;
    SumInterface si = (x, y) -> ((x+y)+b);
    b = 15;
    int ans = si.sum(a, b);
    System.out.println(ans);
}
```

이렇게 변경하면 에러가 난다.

![](https://i.imgur.com/43vYxIy.png)

이런 식으로...

여기 에러 메세지를 보면

`람다 내에서 사용되는 변수는 final이거나 effectively final이어야 한다`

라는 것이다.

* final은 그냥 어차피 final이니까 변경 못함
* final이 선언되지 않아도 변경할 수 없어야 한다.

라는 것이다.
즉, 변경하지 말라는거다.

왜일까?

## 람다 캡쳐링

lambda의 매개 변수가 아니라, 외부에서 정의된 변수를 자유 변수라고 한다.
그리고 lambda에서 이러한 자유 변수를 참조하는 것을 람다 캡쳐링이라고 한다.

이거를 어떻게 사용할까??

* instance 변수
    * 힙 영역에 있는걸 꺼내 씀
* static 변수
    * 힙 영역에 있는걸 꺼내 씀
* local 변수
    * 지역 변수를 람다식이 존재하는 Thread스택에 복사해서 사용함.

이렇게 한다.

근데 instance나 static은 어차피 공통 영역에서 꺼내쓰면 되는데, local변수는 좀 느낌이 다르다.
람다의 Thread내 Stack영역에 복사해서 사용하는데 만약에 멀티 쓰레드 환경에서 계속해서 local변수가 변경된다면 동기화 문제가 발생하게 될 것이다.

### final

그렇기 때문에 local변수의 값이 변경되지 않음을 확신해야 하고, final이거나 만약 그렇지 않다면 다른 곳에서 변경이 일어나지 않음을 확신(effectively final)할 수 있어야 한다.
