# static과 인스턴스 메서드

일단 얘들이 뭐냐면

```
class MyMath {
    long a, b;
    
    long add() {    // 인스턴스 메서드
        return a + b;
    }
    
    static long add(long a, long b) { // 클래스 메서드(static)
        return a + b;
    }
}
```

이렇게 되어있다.
그래서 이게 뭐가 다른걸까?

## 인스턴스 메서드

* 인스턴스 생성 후, '참조변수.메서드이름()'으로 호출
* 인스턴스 멤버(iv, im)와 관련된 작업을 하는 메서드
    * iv : instance variable
    * im : instance method
* 메서드 내에서 인스턴스 변수(iv) 사용가능

## static 메서드(클래스메서드)

* 객체생성 없이 '클래스이름.메서드이름()'으로 호출
    * 객체생성을 하지 않으니까 참조변수는 쓸 수 없고, 그래서 클래스이름을 사용한다.
* 인스턴스 멤버(iv, im)와 관련이 없는 작업을 하는 메서드
* 메서드 내에서 인스턴스 변수(iv) 사용불가

둘의 차이를 간략히 정리하자면, 인스턴스 변수(iv)를 사용하면 인스턴스 메서드, 사용하지 않으면 스태틱 메서드라고 생각하면 된다.

그러면 다시 원래 코드로 돌아가 본다.

```
class MyMath {
    long a, b;
    
    long add() {    // 인스턴스 메서드
        return a + b;
    }
    
    static long add(long a, long b) { // 클래스 메서드(static)
        return a + b;
    }
}
```

여기서 둘의 차이를 보자면, 인스턴스 메서드는 매개변수가 없다.
여기서 `a`, `b`는 iv(인스턴스 변수)이다.
얘는 클래스 전체에서 사용 가능하다.

그리고 static에서 매개변수로 사용하는 애들은 지역변수라 해당 함수 내에서만 사용 가능하다.

이걸 보면 결국
인스턴스 변수를 사용할 수 있으면 인스턴스 메서드를 사용하고, 아닌 경우 스태틱 메서드를 사용하면 된다.

## static을 언제 붙일까?

### (변수의 경우) 속성(멤버 변수) 중에서 **공통 속성**에 static을 붙인다.

```
class card {
    String kind;     // 무늬
    int number;      // 숫자
    
    static int width = 100;  // 폭
    static int height = 250; // 높이
}
```

요러면 클래스변수가 됨.

### (메서드의 경우) 인스턴스를 사용하지 않는 메서드에 static을 붙인다.

```
class MyMath2 {
    long a, b;
        
            long add()                { return a + b; }  // a, b는 인스턴스변수
    static  long add(long a, long b)  { return a + b; }  // a, b는 지역변수
}
```

당연한 거지만, static 메서드에서 인스턴스 변수를 사용하면 에러남.
근데 클래스 변수를 사용하면 에러가 나지 않는다.

## 변수 사용

예를 들어

```
class TestClass2 {
    int iv;            // 인스턴스변수
    static int cv;     // 클래스 변수     
    
    void instanceMethod() {
        System.out.println(in);    // 사용 가능
        System.out.println(cv);    // 사용 가능
    }
    
    static void staticMethod() {
        System.out.println(in);    // 사용 불가능!!!
        System.out.println(cv);    // 사용 가능
    }
}
```

이런 느낌이다.
이유는 static메서드는 객체 생성 없이 호출 가능한데, 인스턴스 변수의 경우는 객체 생성 이후에 호출 가능하기 때문이다 즉, static method의 생성 타이밍에 객체가 있을지 없을지 확신할 수 없기 때문에 아예 호출 자체가 불가능한 것이다.
다만 클래스 변수의 경우는 호출 가능한데, 이는 클래스 변수는 이미 있다는 보장이 있기 때문이다.

## 메서드 간 호출

```
class TestClass {
    void instanceMethod() {}         // 인스턴스메서드
    static void staticMethod() {}    // static메서드
    
    void instanceMethod2() {
        instanceMethod();            // 호출 가능
        staticMethod();              // 호출 가능
    }
    static void staticMethod2() {
        instanceMethod();            // 호출 가능
        staticMethod();              // 호출 가능
    }
}
```

위의 변수에서의 이유와 마찬가지로, 메서드 간 호출도 이렇게 동작한다.
