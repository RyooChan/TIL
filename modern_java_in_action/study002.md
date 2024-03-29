## 람다 표현식

* 람다 표현식이란?
    * 메서드로 전달할 수 있는 익명 함수를 단순화한 것

람다 표현식은 일단 간결하게 코드를 전달하는데에 쓰인다.
-> 말하자면 사실 이전에 못하던 기능을 람다를 통해 할 수 있다기 보다는, 그냥 코드가 깔끔해지고 알아보기 쉬운 것이다.

저기 위의 설명에서 그 이유를 대충 짐작할 수 있는데

* 익명
    * 이름이 따로 없다. 이름을 만들 필요가 없으니 구현이 간단해진다.
* 함수
    * 메서드와 다르게 특정 클래스에 종속되지 않는다.
    * 파라미터 리스트, 바디, 반환 형식, 예외 리스트는 포함된다.
* 전달
    * 람다 표현식을 메서드 인수로 전달하거나 변수로 저장할 수 있다.
* 간결성
    * 익명 클래스처럼 코드를 막 구현할 필요는 없다.

## 람다에 대해서

`(Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());`

여기서 화살표를 기점으로 **왼쪽이 파라미터 리스트** 이고, **오른쪽이 람다 바디** 이다.

각각에 대해 설정하자면

* 파라미터 리스트
    * 메서드 파라미터
    * 즉 람다식에서 파라미터로 쓰일 애들을 저기 적어준다.
* 람다 바디
    * 실제 표현식(반환값에 해당) 한다.
    * 무게를 비교하는 계산식이 저기 적혀있다.

람다식의 사용법은 아래와 같다.

```
(parameters) -> expression
```

```
(parameters) -> {statements;}
```

이렇게이다.

[이걸]([https:/](https://hello-backend.tistory.com/262)/) 보고 대충 정리하자면

* {} 가 없는 경우
    * 하나의 표현식이 들어가면 된다. (하나의 계산)
    * 명시적, 묵시적 모두 가능하다.
    * 근데 그렇다고 구문이 들어갈 수 없는것은 아닌데, Runnable처럼 결과가 없는 경우는 구문으로도 잘 동작한다.
        * 사실 이거는 근데 구문이 들어갈 수 있다!! 라기 보다는 그냥 결과가 void형태라... 이에 관해서는 후술한다.
* {}가 있는 경우
    * 구문이 들어간다.
    * 하나만 들어갈필요는 없다. 많은 동작 구문을 넣어도 된다.
    * 표현식을 넣어줄 때에는 그거만 띡 넣는게 아니라 명시적으로 return등의 행동을 적어야 한다.

## 람다의 사용법

[앞](https://hello-backend.tistory.com/261)에서 보았던 필터 메서드에 람다를 활용할 수 있다.

```
List<Apple> greenApples = filter(inventory, (Apple a) -> GREEN.equals(a.getColor()));
```

## 함수형 인터페이스

[이거](https://hello-backend.tistory.com/221)를 보면 대충 왜 썼는지 알것이다.
대충 설명하자면 인터페이스 내에 하나의 메서드만을 정의시키고, 그거를 내가 알아서 구현해서 쓰는 느낌이라 생각하면 편하다.

각 함수형 인터페이스는 return형태나 parameter에 따라 다른 이름을 가지니까 상황에 맞게 갖다 쓰면 된다.

그래서 이걸 뭐 어쩌라고 생각할 수 있는데, 람다를 통해 함수형 인터페이스의 추상 메서드 구현을 쓰면서 바로 전달해줄 수 있다!!
그니까 즉 람다 표현식 자체가 함수형 인터페이스의 인스턴스로 취급될 수 있는 것이다.

### 함수 디스크립터

함수 디스크립터는, 람다 표현식의 시그니처를 서술하는 메서드를 의미하는데, 예를 들어
Runnable 인터페이스의 추상 메서드 `run`의 경우 

![image](https://github.com/RyooChan/TIL/assets/53744363/6c85b326-01f2-4958-8e20-7fe8a64eb391)

인수와 반환값이 없으므로

Runnable 인터페이스는 인수와 반환값이 없는 시그니처로 생각하면 된다.

아까 위에서 중괄호가 없는 람다식에서도 ` Runnable처럼 결과가 없는 경우는 구문으로도 잘 동작` 한다고 적어놓았을 것이다.
이게 말하자면

```
process(() -> System.out.println("류찬은 최고다!!"));
```

와 같은 형태인데, 분명 표현식이 들어가야 했는데 왠지 구문이 들어갔다.
대충 표현하자면

![image](https://github.com/RyooChan/TIL/assets/53744363/198d696e-8911-4a06-8bb9-df390bbe2283)


이런 식인데, 이거 동작시키면

![image](https://github.com/RyooChan/TIL/assets/53744363/b170baea-8fae-43dc-9fa2-a27583c8d550)

잘 된다.

이는 자바에서 void 반환하는 메서드 호출에 관한 특별한 규칙을 정하고 있기 때문인데, 한 개의 void 메서드 호출은 중괄호가 필요없다.
-> 한개의 void 메서드 호출은 알아서 구문을 사용해도 되는 것.

## 람다 활용

자원 처리(DB의 파일 처리 등등)에서 순환 패턴이라는 것을 사용한다.
이는

1. 자원을 연다.
2. 처리한다.
3. 자원을 닫는다.

로 실행하는데 여기서 1, 3 설정과 처리 과정은 대부분 비슷하다.

즉 2번 처리 과정을 위해 설정과 정리 과정이 이를 둘러싸고 있는 형태인데, 이런 형식의 코드를 **실행 어라운드 패턴** 이라 한다.

이를 처리할 때에 람다를 쓰면 간단히 해결 가능하다.

## 함수형 인터페이스

함수형 인터페이스는 **오직 하나의 추상 메서드를 지정**한다.
java에서는 Comparable, Runnable, Cllable 등의 함수형 인터페이스를 가진다.
이외의 내용들은 [여기](https://hello-backend.tistory.com/221) 서 확인 가능하다.

### 기본형 특화 함수형 인터페이스

여기서 살펴본 애들은 다들 reference(참조형) 으로 되어 있다.
그래서 만약 primitive(기본형)을 사용하게 되면 자바에서 이를 박싱하여 전달하고, 또 저기서는 언박싱하여 참조형을 기본형으로 만드는 등의 리소스 낭비가 발생하게 된다.
이를 막기 위해 기본형 특화 함수형 인터페이스가 존재한다.

뭐 대충

`DoublePredicate`, `IntConsumer`, `IntFunction` 등등... 앞에 기본형 형식을 붙여주는 느낌이다.
이렇게 하면 특정 형식의 기본 입력을 받을 수 있다.

참고로 Function처럼 return이 존재할 때에 `ToIntFunction<T>` 이렇게 하면 기본형 Return도 가능하다.

## 형식 검사, 형식 추론, 제약

람다 표현식에는 이게 어떤 함수형 인터페이스를 구현하는지의 정보가 포함되어 있지는 않다.
근데 람다를 통해 함수형 인터페이스의 인스턴스를 구현할 수 있다.

이를 이해하기 위해서는 람다의 실제 형식을 파악해야 한다.

### 형식 검사

람다가 사용되는 컨텍스트를 이용해서 람다의 형식을 추로할 수 있다.
컨텍스트는 그 람다가 쓰이게 되는 문맥인데, 즉 람다가 할당되는 변수나 쓰이는 메서드의 파라미터 등을 의미한다.

예를 들어

```
List<Apple> heavierThan150g = filter(inventory, (Apple apple) -> apple.getWeight() > 150);
```

요런게 있을때 저기 뒤에 `apple.getWeight() > 150` 파라미터는 pedicate 형식임을 알 수 있다(boolean)

그래서 람다식은 apple을 인수로 받아 boolean을 반환하는 코드일 것이다.

### 같은 람다, 따른 함수형 인터페이스

같은 람다 표현식이여도 다른 함수형 인터페이스로 사용될 수 있다.

```
Callable<Integer> c = () -> 42;
PrivilegedAction<Integer> p = () -> 42;
```

뒤의 람다 표현식은 같지만, 다른 함수형 인터페이스가 이를 받고 있다.
Callable, PrivilegedAction 모두 T를 반환하는 함수를 정의하고 있어서 둘 다 맞는 표현식이다.

### 형식 추론

위에서 람다는 대상 형식을 통해 함수 디스크립터를 추론할 수 있다는 것을 알았다. (표현식을 보고 어떤 함수형 인터페이스인지 알 것이다.)
그리고 함수 디스크립터를 알기 때문에 당연히 람다의 시그니처도 추론할 수 있을 것이다.
왜냐면 어떤 함수형 인터페이스인지 알기 떄문에 어떤 형식의 파라미터와 return이 나올지는 자연스레 알 수 있기 때문이다.

그렇다면 이를 통해 따로 형식을 적지 않고도 자바가 이를 추론하게 할 수도 있을 것이다.

```
Comparator<Apple> c = 
    (a1, a2) -> a1.getWeight().compareTo(a2.getWeight());
```

보면

1. `a1.getWeight().compareTo(a2.getWeight())`는 ` int compare(T o1, T o2);` 을 기반으로 `int compare(Apple a1, Apple a2)`를 디스크립터로 가질 것이다.
2. 위에서 함수 디스크립터는 Apple 두개를 받고, int를 return한다. 
3. 그러면 시그니처는 `(Apple a1, Apple a2) -> int` 일 것이다.
4. 굳이 저거를 적어주지 않아도 추론을 통해 알 수 있는 것이다.

그런 식으로 형식 추론을 통해 코드를 단순화 할 수도 있다.

### 지역 변수 사용

[이거](https://hello-backend.tistory.com/225)를 참고하면 좋은데, 간단하게 말하자면 지역 변수는 final의 값만을 사용할 수 있다는 것이다.

## 메서드 참조

메서드 참조를 사용해서 람다처럼 전달할 수 있고, 어떨 때에는 이게 더 가독성이 좋을 수도 있다.
한번 보자

### 요약

말하자면 `특정 메서드를 호출하는 람다`의 축약형이다.
즉 람다의 목적이 메서드를 호출하는 것이라면, 그걸 그냥 직접 참조시키는 것이다.

`Apple::getWeight` 와 같은 식으로 getWeight 라는 메서드를 그냥 참조시키면 된다.

## 생성자 참조

`ClassName::new` 처럼 클래스명과 new 기워드를 이용해서 기존 생성자의 참조를 만들 수 있다.

```
Supplier<Apple> c1 = Apple::new;
Apple a1 = c1.get();
```

위의 코드는

```
Supplier<Apple> c1 = () -> new Apple();
Apple a1 = c1.get();
```

과 같다.

Functional interface Supplier의 get 메서드를 새로 생성하는 new로 선언해 둔다면, 바로 사용할 수 있게 되는 것이다.

그리고 만약 생성하면서 바로 값을 설정하려 한다면(파라미터가 존재한다면)

```
Function<Integer, Apple> c2 = Apple::new;
Apple a2 = c2.apply(110);
```

이렇게 하면 된다.
이는 


```
Function<Integer, Apple> c2 = (weight) -> new Apple(new);
Apple a2 = c2.apply(110);
```

이와 같다.

이런 식으로 하면 인스턴스화하지 않고도 생성자에 접근할 수 있는 다양한 기능을 응용할 수 있다.
예를 들어 Map으로 생성자와 문자열값을 관련시킬 수 있다.

```
static Map<String, Function<Integer, Fruit>> map = new HashMap<>();
```

이런 식으로 String 형식의 Key와 Function<Integer, Fruit> 형식의 value를 갖는(즉, value로 Integer가 들어오면 Fruit 객체를 내뱉는) HashMap map을 정의하고

```
static {
    map.put("apple", Apple::new);
    map.put("orange", Orange::new);
}
```

값을 넣어준다.
이 때 매번 Fruit의 종류를 하나하나 만들어 줄 필요 없이 Integer를 통해 바로 new 생성자를 넣어준다.
이후

```
public static Fruit giveMeFruit(String fruit, Integer weight) {
    return map.get(fruit.toLowerCase())
            .apple(weight);
}
```

이렇게 giveMeFruit를 만들어주면
저 giveMeFruit를 통해 map에 해당하는 객체를 만드는 코드를 만들 수 있는 것이다.

