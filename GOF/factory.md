# 펙토리 메소드 패턴
###### tags: `Tag(GoF디자인 패턴)`

> 구체적으로 어떤 인스턴스를 만들지는 서브 클래스가 정한다.
> 그니까 그 껍데기를 추상적인 Interface 껍데기로 감싸는 것이다.

1. Factory역할을 할 interface를 만들어준다.
2. 이 interface내의 추상화 메소드를 하위 클래스에서 구체적으로 만들어준다.
3. 그리고 이거의 결과로 나오는것도


## 문제 상황

```
public class Client {
    public static void main(String[] args) {
        Client client = new Client();
        
        Ship whiteship = ShipFactory.orderShip("WhiteShip", "fbcks97@naver.com");
        
        Ship blackShip = ShipFactory.orderShip("blackShip", "fbcks97@naver.com");
        
    }
}
```

이런 식으로, 하나의 ship 생성에서 여러 배를 만든다고 가정하면
저거 내부 구현에 있어서 

* whiteship일때는 어떠어떠한 방법으로 구현한다
* blackship일때는 어떠어떠한 방법으로 구현한다.

요런 식으로 매번 각 분기에 맞춰서 if같은 느낌으로 분기를 해줘야 할 것이다.
그리고 뭔가 추가될 때에도 그에 맞추어 적용을 해줘야 한다.

그렇다면 이런 코드는 변경에 닫혀있지 않은 상태이다.
이 [SOLID원칙](https://hello-backend.tistory.com/186)에서 Open-Closed원칙에 따르면 코드는 `확장에는 열려있고, 변경에는 닫혀있는` 상태여야 하는데 이녀석은 확장에는 열려있지만 변경에도 열려있게 된다.

## 해결 방안

interface내에서 추상화시키는데, 거기서

```
public interface ShipFactory {

    default Ship orderShip(String name, String email) {
        validate(name, email);
        prepareFor(name);
        Ship ship = createShip();
        sendEmailTo(email, ship);
        return ship;
    }

    void sendEmailTo(String email, Ship ship);

    Ship createShip();

    private void validate(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("배 이름을 지어주세요.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("연락처를 남겨주세요.");
        }
    }

    private void prepareFor(String name) {
        System.out.println(name + " 만들 준비 중");
    }

}
```

=> 참고로 java8버전부터는 default를 사용해서 interface에서 바로 로직을 만들어줄수 있다.
=> 그리고 java9버전부터는 저 아래의 private를 통해 interface에서 바로바로 하위 로직도 만들어줄수 있다.
=> 그니까 java11을 애용하도록 하자 :)

이렇게 각각의 로직을 따로 메소드로 구현한 interface를 만들어준다.

그리고 이것들을 구현해줄 애들을

```
public class BlackshipFactory extends DefaultShipFactory {
    @Override
    public Ship createShip() {
        return new Blackship();
    }
}
```

```
public class WhiteshipFactory extends DefaultShipFactory {

    @Override
    public Ship createShip() {
        return new Whiteship();
    }
}
```

요렇게 만들어준다.

그리고 다시 이것이 가져다줄 Class도 지금과 같이

```
public class Ship {

    private String name;

    private String color;

    private String logo;

    private Wheel wheel;

    private Anchor anchor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public String toString() {
        return "Ship{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", logo='" + logo + '\'' +
                '}';
    }

    public Wheel getWheel() {
        return wheel;
    }

    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }
}
```

얘를 상속받아줘서 사용해서

```
public class Blackship extends Ship {

    public Blackship() {
        setName("blackship");
        setColor("black");
        setLogo("⚓");
    }
}
```

```
public class Whiteship extends Ship {

    public Whiteship() {
        setName("whiteship");
        setLogo("\uD83D\uDEE5️");
        setColor("white");
    }
}
```

이런 식으로 가져와서 쓰면 된다.

이제 그러면 이걸 활용할 때에는

```
public class Client {

    public static void main(String[] args) {
        Client client = new Client();
        client.print(new WhiteshipFactory(), "whiteship", "keesun@mail.com");
        client.print(new BlackshipFactory(), "blackship", "keesun@mail.com");
    }

    private void print(ShipFactory shipFactory, String name, String email) {
        System.out.println(shipFactory.orderShip(name, email));
    }

}
```

이렇게 해준다.

이 장점은

* 기존에 만들어 두었던(예를들어 처음에는 whiteship만 있었을 때에) 내용에서 새로운 것을 추가할 때에(blackship추가) 원래 코드를 변경하지 않는다.
    * 그냥 기존의 ShipFactory랑 Ship을 받아서 사용하는 것을 쓰면 되기 때문이다!!
        * 이렇게 하면 기존에 완성해둔 코드는 확장에는 열려있고 변경에는 닫혀있게 된다.

## Client에서의 factory적용

```
public class Client {

    public static void main(String[] args) {
        Client client = new Client();
        client.print(new WhiteshipFactory(), "whiteship", "keesun@mail.com");
        client.print(new BlackshipFactory(), "blackship", "keesun@mail.com");
    }

    private void print(ShipFactory shipFactory, String name, String email) {
        System.out.println(shipFactory.orderShip(name, email));
    }

}
```

이런 식으로 해주면 매번마다 그 코드를 구현하지 않아도 되긴한다...
저 WhiteshipFactory랑 BlackshipFactory는 이미 ShipFactory interface를 받아서 구현한 애들이기 때문에 바로바로 적용이 가능해서임.

근데 좀 궁금한거는 이거를 어차피 client에서는 분기해서 적어줘야 하는게 아닌가 하는거...?

> 추가로 interface내의 private method들은 java8이하 버전에서는 abstract class를 이용해 또 추상 메서드로 만들어주어야 한다.

* 장점
    * Factory method를 사용하면 기존 코드의 변경 없이 확장성을 얻을 수 있다.
    * 느슨한 결합
* 단점
    * 뭔가 이것저것 엄청 많아짐... 역할에 따라 Class의 수가 많아질것이다.

## 이거 그래서 어디서 쓰이는가?

* java의 Calendar
    * getInstance
        * 그냥 `getInstance()`를 쓰면 GregorianCalendar
            * 저게 기본시간
        * `getInstance(Locale.forLanguagaTag("th-TH-x-lvariant-TH"))`를 쓰면 BuddhistCalendar
            * 대만쪽 시간
        * 이런식으로 class가 다르게 가져와진다.
* BeanFactory
    * 이것도 가서 보면 들어가는거에 따라서 막 다르게 설정됨!
    * 이거 Object만들때 BeanFactory Creator임
