# Reflection??

> 구체적인 클래스 타입을 알지 못해도 그 클래스의 메서드와 타입, 변수들에 접근할 수 있도록 해주는 java API

이는 클래스로더를 통해 읽어온 클래스 정보를 사용하는 기술이다.

이를 사용하여 클래스를 읽어오거나, 인스턴스를 만들거나, 메서드를 실행하거나, 필드의 값을 가져오거나 변경하는 등의 행위가 가능하다.

## 사용법

* 특정 annotation이 붙어있는 필드 또는 메서드 읽어오기(JUnit, Spring)
* 특정 이름 패턴에 해당하는 메서드 목록 가져와 호출하기(Getter, Setter)

---

# Reflection

* 먼저 Spring은 어떻게 실행시점에 bean을 주입할 수 있을까?
* 또, JPA의 Entity는 왜 꼭 기본 생성자를 가져야만 할까??

상단의 두 질문을 관통하는 키워드가 리플렉션이다.

reflection이란, 반사 혹은 관통을 의미한다.
즉 실체를 반사시키는 것이라는 건데, java에서는 실체가 클래스이고 거울이 JVM메모리 영역이다.

1. 먼저 java에서는 컴파일러가 소스코드를 바이트코드로 바꾸어 준다.
2. 클래스 로더는 이 바이트코드를 읽어 JVM메모리 영역에 저장한다.
3. 리플렉션은 이 JVM메모리 영역에 저장된 클래스의 정보를 꺼내 와서 필요한 정보들(생성자, 필드, 메서드들)을 가져와 사용하는 기술이다.

참고로 C, C++, Pascal등의 언어는 reflection기능을 지원하지 않는다고 한다.

## 리플렉션이 제공하는 기능들

리플렉션에 대해 알아보기 전에 class에 대해 간단히 알아보자면

> Class란, 실행중인 자바 어플리케이션의 클래스와 인터페이스의 정보를 가진 클래스

이다.
그렇기 때문에

* 클래스에 붙은 어노테이션 조회
* 클래스 생성자 조회
* 클래스 필드 조회
* 클래스 메서드 조회
* 부모 클래스, 인터페이스 조회

와 같은 기능을 할 수 있다.
왜냐면 이 클래스와 인터페이스의 정보를 갖고 있기 때문이다.

또한 

> public 생성자가 존재하지 않는다.

class객체는 대신 JVM에 의해 자동으로 생성된다.

그래서 이렇게 자동으로 생성된 클래스 객체를 가져오는 방법은

#### 1. {클래스타입}.class 를 이용해서 가져오기

```
Class<?> clazz = Dog.class;
```

이런 식으로 ClassType.class를 이용해 가져올 수 있다.

#### 2. {인스턴스}.getClass()

```
God chan = new God("류찬");
Class<?> clazz = chan.getClass();
```

또한 이런 식으로 getClass를 사용하여 호출할 수도 있으며

#### 3. Class.forName("{전체 도메인 네임}")

```
Class<?> clazz = Class.forName("org.example.Dog")
```

이와 같이 forName 메서드를 사용할 수도 있다.

#### 주의점

위의 3가지 방법을 사용할 때에 주의할 점은

`getMethods`와 `getDeclaredMethods`를 구분해야 한다는 것이다.

둘의 차이로는

* getMethods
    * 상위 클래스와 상위 인터페이스에서 **상속한 메서드를 포함하여 public인 메서드들을 모두** 가져옴
* getDeclaredMethods
    * **접근 제어자와 관계 없이 상속한 메서드를 제외하고 직접 클래스에서 선언한 메서드들**을 모두 가져온다.

가 있다.
Class에는 `get~~~` `getDeclared~~~~` 등등이 있어서 이들을 잘 구분해서 써야한다.

### 코드로 알아보기

```
public class Dog {
    private static final String CATEGORY = "동물";
    
    private String name;
    public int age;
    
    private Dog() {
        this.name = "누렁이";
        this.age = 0;
    }
    
    public Dog(final String name) {
        this.name = name;
        this.age = 0;
    }
    
    public Dog(final String name, final int age) {
        this.name = name;
        this.age = age;
    }
}
```

여기서는 `name`, `age` 두가지 필드와, 3개의 생성자가 존재한다.

이거를 한번 객체를 생성해 보면

```
Class<?> clazz = Class.forName("org.example.Dog");

Constructor<?> constructor1 = clazz.getDeclaredConstructor();
Constructor<?> constructor2 = clazz.getDeclaredConstructor(String.class);
Constructor<?> constructor3 = clazz.getDeclaredConstructor(String.class, int.class);
```

1. 위에서 만든 Dog클래스를 일단 Class.forName으로 class타입 객체로 가져온다.
2. `getDeclaredConstructor()`를 사용하여 생성자를 Constructor라는 타입의 객체로 가져올 수 있다.
3. 여기서 constructor1,2,3이 있는데, 이들은 위에서 설정해둔 각각의 타입을 갖는 생성자를 가져와준 것이다.

이제 이를 통해 객체를 생성하면

```
Object dog1 = constructor1.newInstance();
```

이렇게 `newInstance()` 메서드를 사용하여 생성자에서 객체를 생성하려 하면?

![](https://i.imgur.com/wQlflZQ.png)

요런 에러가 발생한다.

내용을 보면 private형태여서 접근이 안된다는 것인데... 어떻게 해결할 수 있을까??

```
constructor1.setAccessible(true);
Object dog1 = constructor1.newInstance();
Object dog2 = constructor1.newInstance("호두");
Object dog3 = constructor1.newInstance("호두", 5);
```

요런 식으로 `setAccessible(true)` 를 사용하면, private생성자에도 접근이 가능하다.

![](https://i.imgur.com/da4cZnf.png)

ㅇㅇ

이제 필드 정보를 조회하는 기능인데,

```
Object dog = constructor.newInstance("호두", 5);

Field[] fields = clazz.getDeclaredFields();

for(Field field : fields) {
    field.setAccessible(true);    // private에의 접근을 위해
    System.out.println(field);
    System.out.println("value:" + field.get(dog));
    System.out.println("------------------------------");
}
```

이런 식으로 `getDeclaredFields()`메서드를 사용하면 field라는 타입의 객체로 class객체를 받아올 수 있다.

![](https://i.imgur.com/vhGrlEa.png)

이렇게 잘 출력된다.

다음으로는 reflection을 사용한 private필드의 값 변경이다.

```
Field field = clazz.getDeclaredField("name");
field.setAccessible(true);
System.out.println("기존: " + field.get(dog));
field.set(dog, "땅콩");
System.out.println("변경: " + field.get(dog));
```

![](https://i.imgur.com/A1z69V7.png)

요렇게 값의 변경도 가능하다.

다음으로 메서드에 관련된 기능이다.

그 전에 먼저 Dog클래스에 세가지 메서드를 추가해 준다.


```
public class Dog {
    private static final String CATEGORY = "동물";
    
    private String name;
    public int age;
    
    private Dog() {
        this.name = "누렁이";
        this.age = 0;
    }
    
    public Dog(final String name) {
        this.name = name;
        this.age = 0;
    }
    
    public Dog(final String name, final int age) {
        this.name = name;
        this.age = age;
    }
    
    private void speak(final String sound, final int count) {
         System.out.println(sound.repeat(count));
    }
    
    public void eats() {
        System.out.println("사료를 먹습니다.");
    }
    
    public int getAge() {
        return age;
    }
}
```

이렇게 `speak`, `eats`, `getAge`메서드를 추가해 주었다.
그래서 이제

```
Method[] methods = clazz.getDeclaredMethods();
for(Method method : methods) {
    method.setAccessible(true);
    System.out.println(method);
    System.out.println("\n---------------------");
}
```

`getDeclaredMethods()` 메서드를 사용해서 모든 메서드들을 가져와주고 내용을 본다.

![](https://i.imgur.com/5H8rLat.png)

이런 식으로 
`접근 제어자`, `return타입`, `메서드이름`, `파라미터타입` 정보를 확인할 수 있다.

```
Method method = clazz.getDeclaredMethod("speak", String.class, int.class);
method.setAccessible(true);
method.invoke(dog, "멍멍!", 5);
```

private메서드의 호출도 가능하다.

![](https://i.imgur.com/eVVHerK.png)

## 리플렉션이 사용되는 곳

리플렉션을 평소에 사용하는일이 없다보니까 생소할수 있는데, 사실 많이 쓰인다고 한다.

보통 리틀렉션은 

* 프레임워크
* 라이브러리

에서 많이 사용한다.
보통 우리는 코딩할 떄에 객체의 타입을 모르는 일이 거의 없다.

반면, 프레임워크나 라이브러리의 경우는 사용자가 어떤것을 사용하는지 컴파일 시점까지 알 수가 없다.
이런 문제를 동적으로 해결하기 위해 리플렉션을 사용한다.

그래서 이거는

* JPA
* Jackson
* Mockito
* JUnit
* 등등등

많은 곳에서 쓰인다.

그리고 intelliJ에서 제공하는 자동완성 기능도 reflection을 사용한 기술이다.

> 보통 프레임워크나 라이브러리에서는 객체의 기본 생성자를 요구한다.

![](https://i.imgur.com/1joSpOW.png)

![](https://i.imgur.com/xMdx8Kd.png)

![](https://i.imgur.com/7ccilyn.png)

요런 식으로 JPA의 entity나 RequestDTO에서도 ResponseDTO에서도 기본생성자를 필요로 한다.

이 이유도 리플렉션 떄문이다!!

* 근데 이미 생성자가 있는데 굳이 기본생성자를 왜 만들어 주나?? 
    * 기본생성자로 객체를 생성하고 필드를 통해 값을 넣어주는 것이 가장 간단한 방법이기 때문이다.
    * 만약 기본생성자가 없다면 어떤 생성자를 사용할지 고르기 어렵기 때문이다.
        * 생성자가 엄청 많으면 잘못쓸수도 있고... 생각지 않은걸 쓸수도 있다.
    * 그리고 파라미터의 타입이 같은 경우에는 필드와 이름이 다르면 값을 알맞게 넣어주지 힘들기 때문이다.

![](https://i.imgur.com/SjvSd4Q.png)

요렇게 기본생성자로 객체 생성 후 필드 이름에 맞춰 알맞은 값을 넣어주면 끝이다.

> 어노테이션의 작동 원리도 리플렉션이다.

어노테이션은 사실 그냥 주석인데, 이게 기능을 갖고 작동하는 이유도 리플렉션 덕분이다.

간단하게 원리를 살펴보면

1. 리플렉션을 통해 클래스나 메서드, 파라미터 정보를 가져온다.
2. 리플렉션의 `getAnnotation(s), getDeclaredAnnotation(s)`등의 메서드를 통해 원하는 어노테이션이 붙어있는지 확인한다.
3. 어노테이션이 붙어있다면 원하는 로직을 수행한다.

## 이를 통한 DI 프레임워크 만들기

Spring에서는 `@Autowired` 어노테이션을 사용하면 손쉽게 객체에 해당하는 의존성을 주입해줄 수 있다.

이를 사용하여 DI가 가능하도록 해본다.

> OrderService.java

```
public class OrderService {
    @Autowired
    OrderRepository orderRepository;
}
```

> OrderRepository.java

```
public class OrderRepository {
    Map<Integer, Order> orderIdToOrderMap = new HashMap<>();
    
    public Order getById(Integer orderId) {
        return orderIdToOrderMap.get(orderId);
    }
}
```

여기서 Autowired 어노테이션에 대해

```
@Target(ElementType.FIELD)
@Retention(retentionPolicy.RUNTIME)
public @interface Autowired {
}
```

이렇게 Autowired 어노테이션을 만들어준다.
런타임시 동작할 수 있도록 Retention에서 RUNTIME policy를 설정한다.

```
public class ApplicationContext {
    public static <T> T getInstance(Class<T> clazz) throws Exception {
        T instance = createInstance(class);     // 인스턴스 생성
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields) {
            if(field.getAnnotation(Autowired.class) != null) {
                Object fieldInstance = createInstance(field.getType());
                field.setAccessible(true);
                field.set(instance, fieldInstance);
            }
        }
        return instance;
    }
    
    private static <T> T createInstance(Class<T> clazz) throws Exception {
        return clazz.getConstructor(null).newInstance();
    }
}
```

Application context는 의존성 주입을 담당하는 핵심적인 부분이다.

getInstance는 특정 타입의 클래스를 인자로 받으면, 해당 타입을 return하는 메서드이다.
저기서 `createInstance()` 메서드를 사용했는데, 이는 Instance를 만들어내는 것이며 reflection을 사용한 것이다.

그래서 이제 내부에서 field를 순회하면서 Autowired라는 class가 있는지 검사한다.
이게 붙어있으면 이제 필드의 타입에 맞는 인스턴스를 `createInstance`를 사용해서 생성해 준다.
해당 필드가 private일수 있으므로 setAccessible은 true로 한다.

```
public class Application {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext();
        OrderService orderService = applicationContext.getInstance(OrderService.class);
    }
}
```

여기서 `getInstance()` 메서드를 통해 인스턴스를 얻을 수 있다.

그래서 이 DI가 제대로 동작하는지 테스트해보면

```
public class ApplicationContextTest {
    @Test
    public void getObject_OrderRepository() throws Exception {
        OrderRepository object = ApplicationContext.getInstance(OrderRepository.class);
        assertNotNull(object);
    }
    
    @Test
    public void getObject_OrderService() throws Exception {
        OrderService orderService = ApplicationContext.getInstance(OrderService.class);
        assertNotNull(orderService);
        assertNotNull(orderService.orderRepository);
    }

}
```

테스트 해보면

![](https://i.imgur.com/0KoxuZ0.png)

잘 나온다.

## reflection의 단점

* 느림
    * Reflection API는 컴파일 시점이 아니라 런타임 시점에서 클래스를 분석한다.
    * JVM을 최적화할 수 없기 때문에 성능저하 발생
* 런타임 시점에 클래스 정보를 알게 됨
    * 컴파일 시점에서 타입체크 기능을 사용할 수 없음
    * 에러 발생할 확률이 높다는거임
* 코드가 지저분해짐
* 추상화 파괴, 불변성 파괴
    * 접근할 수 없는 필드나 메서드에도 reflection을 통해 접근할 수 있고 모든 클래스의 정보를 알게 된다.

