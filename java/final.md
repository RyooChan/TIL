# final

이거 은근 많이 쓰이는데 생각보다 아무생각 없이 쓰게된다.

일단 final은 클래스, 메소드, 멤버변수, 지역변수에 사용될 수 있는데 이걸 쓰면

* class
    * 변경, 확장될 수 없는 클래스가 된다.
    * 다른 클래스의 조상이 될 수 없다!
* method
    * 변경될 수 없는 메소드가 된다.
    * 오버라이딩을 통한 재정의가 불가능하다.
* 멤버/지역변수
    * 값을 변경할 수 없는 상수가 된다.
        * 단 한번의 초기화만 가능하다!!

```
public final class Test {                  // 조상이 될 수 없는 클래스 
    final int max_size = 100;              // 값을 변경할 수 없는 멤버 변수 (상수)
    
    final void getMaxSize() {              // 오버라이딩 할 수 없는 메소드
        final int localValue = max_size;   // 값을 변경할 수 없는 지역 변수 (상수)
    }
}
```

