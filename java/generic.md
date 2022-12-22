# java 제네릭(generic) type?

> Data type을 일반화한다는 의미.

generic을 사용해서 컴파일 하는 동안 클래스 혹은 메서드에서 사용할 내부 데이터 타입을 지정할 수 있다.
-> 해당 클래스에서 사용할 데이터의 타입을 외부에서 지정한다.

## 데이터 타입을 미리 지정한다??

한번 보여드림ㅇㅇ
일단 위에서 설명한것처럼 generic은 `클래스` 혹은 `메서드` 에서 사용하는 친구이다.

```
class RyooChan<T> {
    private T intelligence;
    
    void setIntelligence(T intelligence) {
        this.intelligence = intelligence;
    }
    
    T getIntelligence() {
        return intelligence;
    }
}
```

참고로 이거는 Type이라 T를 썼는데, 뭐 아무거나 해도 된다.
일단 이런 식으로 class내에서의 generic type의 intelligence를 선언해준다.

이제 이거를 사용하면

```
public class mainGe {

	public static void main(String[] args) {
		
		man<String> man1 = new man<>();
        RyooChan<Integer> ryoochan = new RyooChan<>();
		
		ryoochan.setIntelligence(200);
		
		System.out.println(ryoochan.getIntelligence());
	}

}
```

이렇게 하면

```
200
```

으로 류찬이 가진 지능지수가 출력될것이다.

## 그럼 이거 왜쓰는데?

저렇게 하면 클래스 내부 데이터의 타입을 컴파일 시에 검사하게 될 것이다.
그러면

* 클래스나 메소드 내부에서 사용되는 객체의 타입 안정성을 높일 수 있다.
    * 그 이유는 실제로 코드를 사용하기 전에 그곳에서 선언을 해주어서 잘못된 타입이 들어갈 문제를 줄일 수 있다.(예를 들어 set으로 "Hello!"가 들어가는 경우 Object면 실제 사용 전까지 Integer값이어도 에러가 보이지 않는다.)
* 반환값에 대한 타입 변환이나 검사를 할 때 코드량을 줄일 수 있다.
    * 매번 변환할 때마다 Object는 바꿔줘야 함... generic쓰면 한번만 바꿈 된다!
* 프로그램의 소소한 성능 향상
    * 타입 변환을 불필요하게 막 할필요 없이 처음부터 타입이 정해지므로
