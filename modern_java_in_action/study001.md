## 동적 파라미터화

여러 요구사항에 효과적으로 대응 가능한 방법.
아직은 어떤 식으로 동작할지가 결정되지 않은 코드 블록이고, 나중에 프로그램에서 실행된다.

## 문제 상황

### 1. 녹색 사과 필터링

```
enum Color { RED, GREEN }
```

빨강, 초록 사과가 있다.

여기서 만약에 녹색 사과를 필터링하려 하면

```
if(GREEN.equals(apple.getColor())) {
    result.add(apple);
}
```

요렇게 쓸 것이다.

* 단점
    * 만약 빨간사과, 노란사과, 검은사과 등등... 필터링 개수가 많아진다면? 저 if문이 계속해서 늘어나거나 또 빼짐에 따라 줄어들 수 있을 것이다.

### 2. 색의 파라미터화

위의 1에서의 단점을 해결하기 위함이다.

```
public static List<Apple> filterApples(List<Apple> inventory, Color color) {
    if(color.equals(apple.getColor())) {
        result.add(apple);
    }
}
```

이런 느낌으로 color를 파라미터로 받아서 그와 apple을 비교한다.
이렇게 하면 거를 색들을 함수로 불러와서 걸러줄 수 있다.

* 단점
    * 색이 아니라 무게를 더한다면? 벌레먹은 횟수? 등등등등 그 때마다 파라미터가 늘어야 한다.
        * 그리고 그 때마다 다른 함수로 변경해야 하거나 루트함수가 많아질 것이다.

### 3. 가능한 모든 속성으로 필터링

그냥 간단하게 파라미터에서 그걸 싹다 넣어주는 것이다.
대충 생각해보면 알겠지만 굉장히 귀찮아지고 분기처리도 고민해야한다(유지보수 망)

## 동적 파라미터화

위의 1~3의 단점을 해결하기 위해서 동적 파라미터를 도입할 수 있다.

동적 파라미터를 사용하는 방법은 간단한데, 어쨌든 사과를 고르려면 "내가 원하는게 맞는지/아닌지"를 가지면 된다. 즉 필터의 결과는 true/false를 받으면 되는 것이다.

[predicate](https://hello-backend.tistory.com/221)라는것이 있는데, 얘는 참/거짓을 반환한다.
이를 한번 사용해보자.

![](https://hackmd.io/_uploads/SkAFs1sKn.png)

참고로 프레디케이트의 내부는 요렇게 되어있다.

이와 비슷한 ApplePredicate를 만들어 줘보자

```
public interface ApplePredicate {
    boolean test (Apple apple);
}
```

이 인터페이스 내의 test를 직접 구현하여 쓰면 될 것이다.

```
public class AppleHeavyWeightPredicate implements ApplePredicate {
    public boolean test(Apple apple) {
        return apple.getWhight() > 150;
    }
}
```

이러면 150g 이상의 apple의 경우 true를 보내주는 내부 기능이 구현된다.
이런 식으로 함수형 인터페이스 predicate를 구현받는 것들을 여러 개 만들어 놓을 수 있을 것이다.


```
public class AppleGreenColorPredicate implements ApplePredicate {
    public boolean test(Apple apple) {
        return GREEN.equals(apple.getColor());
    }
}
```

이렇게 만들수도 있다.

그래서 이거를 내가 원할 때에 써주면 된다.

#### 4. 추상 조건 필터링

위의 ApplePredicate들을 활용하는 방법이다.


```
public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p) {
    List<Apple> appleList = new ArrayList();
    if(p.test(apple)) {
        appleList.add(apple);
    }
}
```

이런 식으로, 여기서는 ApplePredicate를 가져오고, 미리 구현된 조건에 따라 사용만 해주면 된다.
저기 파라미터 P를 받을 때에 초록 사과를 원하면 `AppleGreenColorPredicate`, 150g 이상을 원하면 `AppleHeavyWeightPredicate`를 파라미터로 써주면 될 것이다.

* 장점
    * 하나의 파라미터를 통한 다양한 동작이 가능하다.
        * 즉, 한 메서드를 통해서 여러가지 동작(색을 따른 구분, 무게에 따른 구분 등..)이 가능하다!
* 단점
    * 근데 이거 매번 저 interface의 내부를 하나하나 구현해주어야 한다. 좀 귀찮을 것이다.

## 익명 클래스

자바의 local class와 비슷한 개념
말 그대로 이름이 없는 클래스이고, 선언과 동시에 인스턴스화(구현)을 해낼 수 있다.

### 5. 익명 클래스 사용

위에서 interface의 구현을 하나하나 해둔 상태로 써야 한다고 했다.
이를 해결하기 위해서 익명 클래스를 통해 필요할 때에 해주어도 된다.


```
List<Apple> redApples = filterApples(List<Apple> inventory, new ApplePredicate() {
    public boolean test(Apple apple) {
        return RED.equals(apple.getColor)
    }
});
```

이런 식으로 predicate를 선언함과 동시에 내부 기능을 구현하고, 사용할 수 있을 것이다.

* 장점
    * 쓸것만 선언해서 써주면 된다.
* 단점
    * 쓸데없이 반복되는게 많다.

예를 들어 여기서 초록사과를 구하려 하면?

```
List<Apple> greenApples = filterApples(List<Apple> inventory, new ApplePredicate() {
    public boolean test(Apple apple) {
        return GREEN.equals(apple.getColor)
    }
});
```

위에랑 비교해서 다른거는 딱 RED/GREEN 하나밖에 없다.
즉 필요없는 코드가 많이 남아 있다는것.

#### 6. 람다 표현식 사용

[람다](https://hello-backend.tistory.com/222)를 사용하면 간단하게 표현 가능하다.

```
List<Apple> result = filterApples(inventory, (Apple apple) -> RED.equals(apple.getColor()));
```

이렇게 하면 진짜 간단하게 해결할 수 있다.

#### 7. 리스트 형식 추상화

이제는 모든 조건에 대해서, 모든 클래스(사과, 바나나, 오렌지 등...) 에 대해서도 한번에 필터링 할 수 있도록 해본다.

```
public static <T> List<T> filter(List<T> list, Predicate<T> p) {
    List<T> result = new ArrayList<>();
    list.stream().forEach(
        object -> {
            if(p.test(object)) {
                result.add(object);
            }
        }
    )
}
```

대충 이렇게 만들어 두면 모든 종류를 받고, 모든 조건을 만들어서 사용할 수 있다.

```
List<Apple> redApples = filter(inventory, (Apple apple) -> RED.equals(apple.getColor()));
```


```
List<Banana> redApples = filter(inventory, (Banana banana) -> SWEET.equals(banana.getTaste()));
```


이런 식으로!!
이렇게 하면 유연하게 대응할 수 있고, 또 코드도 필요에 맞춰 쓰면 되니까 매우매우 간결해진다!!

## 마치며

동적 파라미터화를 사용하여 여러 요구사항에 대응할 수 있는 깔끔한 코드를 만들 수 있다.
