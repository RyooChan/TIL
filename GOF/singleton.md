# 싱글톤 패턴

> 인스턴스를 오직 한개만 제공하는 클래스

* 인스턴스를 오직 한개만 만들 수 있어야 한다.
* 그렇게 만들어진 하나의 인스턴스에 글로벌하게 접근할 수 있어야 한다.

이제 설정이나 그런것들은 싱글톤으로 해주면 될것이다.

```
private Settings() {}

private static Settings instance;

public static Settings getInstance() {
    return instance
}

```

이렇게 해주면 instance를 필요할 때에 getInstance를 호출해서 만들어 줄 것이다.
근데 싱글톤은 하나의 인스턴스만을 가질수 있다고 했다.
어떻게 이 싱글톤 패턴을 구현해 줄까?

## 1. private 생성자에 static메소드를 사용해서 싱글톤 구현

```
private Settings() {}

private static Settings instance;

public static Settings getInstance() {
    if(instance == null){
        instance = new Settings();
    }
    return instance
}

```

일단 이렇게 해서 구현해주면

1. 현재 instance가 없으면 만들어줌
2. 있으면 있는거 return

이긴 한데, 이거는 멀티쓰레드 환경에서는 안전하지 못하다.

그 이유는 여러 쓰레드가 동시에 해당 함수에 접근할 때

1. 처음 쓰레드가 if문에 접근할 때 아직 쓰레드가 없어서 instance를 새로 만든다.
2. 두번째 쓰레드 if문에 접근할 때 아직 쓰레드가 없어서 instance를 새로 만든다.

이렇게 되면 오직 한개의 인스턴스만을 만드는 싱글톤 규칙에 위배되게 된다.

## 2. synchronized 키워드를 통한 구현

그럼 어떻게 해결할까?
먼저 `synchronized` 키워드를 사용한 구현이다.

```
private Settings() {}

private static Settings instance;

public static synchronized Settings getInstance() {
    if(instance == null){
        instance = new Settings();
    }
    return instance
}

```

이 `synchronized`를 쓰면 이게 쓰인 함수가 동기화되어서 한번에 하나의 쓰레드만 들어올 수 있게 된다.
그러면 동시에 여러 쓰레드가 들어오지 않기 때문에 하나의 인스턴스만 가져온다.

근데 문제는 getInstance라는 메소드를 가져올 때마다 동기화를 진행하게 되는데, 여기서 성능에 문제가 생기게 된다.
이 동기화 메커니즘은 `lock`을 잡아서 사용하는 것이기 때문에... 좀 쓸데없이 낭비가 있는것이다.

## 3. eager synchronized 키워드를 통한 구현


```
private Settings() {}

private static final Settings INSTANCE;

public static Settings getInstance() {
    return INSTANCE
}

```

이거는 미리 INSTACE를 만들어두고(final이라 대문자로했음 -> 한번 만들고 변경되지 않으니 final) 그거를 그냥 매번 호출해서 쓰는 방식이다.
이렇게 하면 그냥 이미 있는거를 가져다가 쓰면 될것이다.

이것도 thread-safe하다. 어차피 만드는건 최초 1번이니까

근데 이게 미리 만드는게 좀 문제가 될수가 있는데... 실제로 instance를 사용하지도 않는데 굳이 만들게 될수도 있다는 것이다.
그리고 이 instance를 만드는 데에 오래 시간이 걸리고 메모리도 많이 들면 손해가 너무 큼..(쓰지도 않는데 낭비를 막 한다)

## 4. double checked locking 사용

> java1.5 이상부터 동작함...근데 엥간하면 그거 이상 쓸거같긴함 1.5아래가 있긴했냐
> 이거 일단은 추천 1번임!

```
private Settings() {}

private static volatile Settings instance;

public static Settings getInstance() {
    if(instance == null){    // 여기서 1차 체크
        synchronized(Settings.class) {    // class를 synchronized해준다.
            if(intsnace == null){    // 여기서 2차 체크
                instance = new Settings();
            }
        }
    }
    return instance
}

```

그냥 이거는 맨위 if에서 check -> 아래 if에서 check : double checked locking임 ㄹㅇㅋㅋ

이게 좋은점은 대규모 트래픽이 오는 경우(멀티쓰레드)

1. 1번 쓰레드가 1차체크 if문을 통과함
2. 2번 쓰레드가 1차체크 if문을 통과함
3. 1번 쓰레드가 if문에 들어감
4. 2번 쓰레드는 synchronized때문에 동기화가 걸려서 1번 쓰레드 동작 이전까지는 대기
5. 1번 쓰레드가 instance생성
6. 2번 쓰레드가 2차체크 if에서 이미 instance가 만들어져 있으니 그냥 그거 씀

요런 식으로 해서 좋은게 뭐냐면 `synchronized`는 첫번째 if아래에 있으니까 한번 instance가 만들어진 다음에는 동기화가 걸리지 않는 것이다.
그니까 처음에 instance가 아직 없는 타이밍에만 동기화가 걸리는거임ㅇㅇ

그리고 이거는 필요한 타이밍에 instance를 만들어준다.

이게 근데 `volatile`를 여기서 적어주게 되는데, 그 이유는 [여기](https://hello-backend.tistory.com/203)서 확인해보면 알 수 있듯 바로 main에 변경을 반영하면 다른 곳에서 해당 변수를 가져올 때에 변경을 바로바로 알 수 있어서이다.

단점은

1. java 1.5이상에서만 동작(의미없을듯ㅋㅋ)
2. 좀 복잡쓰...

## 5. static inner 클래스 사용

> 권장하는 방법 2

```
private Settings() {}

private static class SettingsHolder{
    private static final Settings INSTANCE = new Settings();
}

public static Settings getInstance() {
    return SettingsHolder.INSTANCE;
}
```

이러면 멀티쓰레드에서도 안전하고 필요할때 인스턴스를 만들수 있다!!
왜냐? 저걸 호출할때 만들고, 그다음은 final로 있는거 가져오니까

그리고 뭔가 복잡하지도 않다~

---

근데 문제는 지금 이 방법들은 다 깨트릴수 있는 코드가 있다!!

만약에 이거를

```
public static void main(String[] args) {
    Settings settings1 = Settings.getInstance();
    Settings settings2 = Settings.getInstance();
    System.out.println(setting1 == setting2);
}
```

### Reflection을 이용해서 싱글톤 깨트리기.

이렇게 하면 당연히 잘 되겠지만

```
public static void main(String[] args) {
    Settings settings1 = Settings.getInstance();
    
    Constructor<Settings> constructor = Settings.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    Settings settings2 = constructor.newInstance();
    // 이거는 그냥 new를 사용해서 만든거랑 비슷하다. 그냥 새로운 생성자로 만드는거지ㅇㅇ
    // 그럼 새거를 만든거네?
    
    System.out.println(setting1 == setting2);
}
```

이러면 둘이 다름... (false return 됨)

### 직렬화 & 역직렬화로 싱글톤 깨트리기.

Object를 file형태로 저장(직렬화)했다가 다시 읽어들이는(역직렬화)것이다.


```
public class Settings implements Serializable{
    private Settings() {}

    private static class SettingsHolder{
        private static final Settings INSTANCE = new Settings();
    }

    public static Settings getInstance() {
        return SettingsHolder.INSTANCE;
    }
}
```

일단 이친구를 Serializable하게 해준다.


```
public static void main(String[] args) throws IOException, ClassNotFoundException {
    Settings settings1 = Settings.getInstance();
    Settings settings2 = null;
    
    try (ObjectOutput out = new ObjectOutputStream(new FileOutputStream("settings1.obj"))) {
        out.writeObject(settings);
    } 
    
    try (ObjectInput in = new ObjectInputStream(new FileInputStream("settings1.obj"))) {
        settungs2 = (Settings) in.readObject();
    }
    
    System.out.println(setting1 == setting2);
}
```

요렇게 하면 

1. settings2는 일단 걍 null
2. settings1을 직렬화해서 파일로 만듬
3. 그거를 역직렬화해서 읽어옴
4. 읽어온걸로 settings2를 채움
5. 그럼 setting1이랑 setting2는 다름

이렇게 하면 싱글톤이 깨지기는 함...

근데 이거 역직렬화 막는방법이 있긴함.

### 직렬화 & 역직렬화 막기

```
public class Settings implements Serializable{
    private Settings() {}

    private static class SettingsHolder{
        private static final Settings INSTANCE = new Settings();
    }

    public static Settings getInstance() {
        return SettingsHolder.INSTANCE;
    }
    
    // 읽어올때 getInstance를 호출시킴 -> 역직렬화할때 getInstance를 쓰겠네?
    protected Object readResolve() {
        return getInstance();
    }
}
```

요래 해주면 역직렬화할때 저 readResolve를 가져오게되어서 Object 역직렬화할때 원래 있던 칭구를 데려옴.

### 그럼 Reflection으로 싱글톤 깨는건 어케해결함?

자바에서 제공하는 `enum`을 사용한다.

참고로 자바에서 `enum`은 '완벽한 싱글톤'이라 불린다ㄷㄷ...
어케하는데??

```
public enum Settings {

    INSTANCE;
    
    // default private임
    Setting() {
    
    }
    
    private Integer number;
    
    public Integer getNumber() {
        return number;
    }
    
    public void setNumber(Integer number) {
        this.number = number;
    }
    
}
```

이렇게 해주는데... 참고로 이거 위에 코드 걍


```
public enum Settings {
    INSTANCE;
}
```

요거임..
-> 참고로 enum은 Serializable도 기본적으로 구현하고 있다.

이게 근데 reflection에 안전한 코드이다.
이게 사실 안전한 이유가... 그냥 저거 `constructor.setAccessible(true);` 이게 불가능함. 즉 private을 외부에서 어떻게 접근 자체가 안되는 것이다.

그리고 얘는 직렬화&역직렬화도 
```
    protected Object readResolve() {
        return getInstance();
    }
```
이거 굳이 안써도 잘 됨.

## 결론

사실 근데 enum 이넘도 단점이 있다.

* 미리 만들어지게 된다.
    * lazy하게 못씀.
        * 그래서 필요없는게 생성될수도 있다는것이다.
* 상속을 쓰지 못한다.
    * enum은 enum만을 상속받을 수 있다.
        * 상속이 필요하면 static inner 클래스를 쓰는게 좋을듯?

그러니까 알아서 잘 파악해서

> `enum`이나 `static inner 클래스(홀더쓰는방식)`를 사용하는게 좋을것 같당!!

## 야 그래서 싱글톤 이거 어떻게 쓰이냐??

> 실무에서 어떻게 쓰는지

* 스프링에서는 빈의 스코프 중에 하나로 싱글톤 스코프
* 자바 java.lang.Runtime
* 다른 디자인 패턴(빌더, 퍼사드, 추상 팩토리 등) 구현체의 일부로 쓰이기도 한다.

### Runtime

자바에서 제공하는 `Runtime`친구는 싱글톤으로만 만들 수 있다.

이친구는 어플리케이션이 실행되는 환경에 관한 것인데, 예를 들어서 실행중 환경의 메모리 정보같은거를 출력할수 있는 친구다.

### Spring

`@Bean` 으로 선언해준 친구는 singleton으로 구현되어 하나의 instance를 return해준다.

그래서 이거 singleton scope라고 부르는데, 사실 이거 근데 실제로는 싱글톤 아니기는 한데 업무를 할때 '유일한 객체가 필요한 경우' 보통 scope로 @Bean을 등록해서 써서 이거를 어쨌든 싱글톤 쓰는거라고 한다.
