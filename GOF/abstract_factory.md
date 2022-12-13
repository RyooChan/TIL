# 추상 팩토리 패턴
###### tags: `Tag(GoF디자인 패턴)`

> 서로 관련있는 여러 객체를 만들어주는 인터페이스

이걸 사용하면 어떤 클래스의 인스턴스를 사용하는지 감출 수 있다.

그래서 [팩토리 메서드](https://hello-backend.tistory.com/207)랑은 뭐가 다른데? 하면 사실 내부 구조는 거의 비슷한데 얘는 Client에서 정보를 숨기는데에 집중한다. (이거 후술)

해당 로직을 구현하는 Client에서 구체적인 class를 사용하는 대신, 추상화된 메서드를 통해 구현하도록 하는 것이다.

## 추상 팩토리 패턴 세팅

추상적인 interface를 만들어준다. -> 추상 Factory 패턴

```
public interface ShipPartsFactory {

    Anchor createAnchor();

    Wheel createWheel();

}
```

그리고 저기 Anchor, Wheel도 interface로 만들어준다.

```
public interface Wheel {
}
```

```
public interface Anchor {
}
```

위에서 만든 추상화 팩터리를 실제로 구현할 메서드를 만들어준다.

```
public class WhiteshipPartsFactory implements ShipPartsFactory {

    @Override
    public Anchor createAnchor() {
        return new WhiteAnchor();
    }

    @Override
    public Wheel createWheel() {
        return new WhiteWheel();
    }
}
```

```
public class WhiteAnchor implements Anchor {
}
```

```
public class WhiteWheel implements Wheel {
}
```

이렇게 하면

추상 팩터리 매서드 ShipPartsFactory -> 그걸 받아주는 WhiteshipPartsFactory
그리고 내부의 값들을 또 받는 WhiteAnchor, WhiteWheel 이 생겨난다.

## 사용하기

```
public class WhiteshipFactory extends DefaultShipFactory {

    private ShipPartsFactory shipPartsFactory;

    public WhiteshipFactory(ShipPartsFactory shipPartsFactory) {
        this.shipPartsFactory = shipPartsFactory;
    }

    @Override
    public Ship createShip() {
        Ship ship = new Whiteship();
        ship.setAnchor(shipPartsFactory.createAnchor());
        ship.setWheel(shipPartsFactory.createWheel());
        return ship;
    }
}
```

이거를 실제로 사용할 때에는 이렇게 써주면
Whiteship정의해주면 해당 Factory를 통해서 다른 코드를 변경하지 않고도 WhiteShip을 만드는 로직이 실행되게 된다.

## 확장

이제 이렇게 되면 만약에 WhitePartsProFactory를 만들어 추가해준다고 생각해 보자

```
public class WhitePartsProFactory implements ShipPartsFactory {
    @Override
    public Anchor createAnchor() {
        return new WhiteAnchorPro();
    }

    @Override
    public Wheel createWheel() {
        return new WhiteWheelPro();
    }
}
```

```
public class WhiteAnchorPro implements Anchor{
}
```

```
public class WhiteWheelPro implements Wheel {
}
```

이런 식으로 ShipPartsFactory를 구현하는 WhitePartsProFactory와 Anchor, Wheel을 구현하는 WhiteAnchorPro, WhiteWheelPro를 만들어주고

```
public class ShipInventory {

    public static void main(String[] args) {
        ShipFactory shipFactory = new WhiteshipFactory(new WhitePartsProFactory());
        Ship ship = shipFactory.createShip();
        System.out.println(ship.getAnchor().getClass());
        System.out.println(ship.getWheel().getClass());
    }
}
```

이렇게 저 WhitePartsProFactory를 통해서 WhiteshipFactory를 사용해 주면 Pro의 친구들이 나오게 된다.

![](https://i.imgur.com/kFYsViF.png)

이렇게 저 Pro쪽 애들이 나오게 된다.

## 만드는 방법을 보면

`ShipPartsFactory`를 통해 제품군을 만드는 방법(`createAnchor`, `createWheel`)을 정의해주었고

그거를 실제로 `WhiteshipPartsFactory` 혹은 `WhitePartsProFactory`등에서 받아주고 나서(해당 결과에 대해서도 `Wheel`은 각각 `WhiteWheel`, `WhiteWheelPro` 이런식으로 만들어준다.)

그것을 Client에서 활용하면 다른 코드의 변경 없이 Factory의 변경만으로 확장이 가능해진다.

## 팩토리 메소드 패턴과의 차이점

일단 둘 다 객체를 만드는 과정을 추상화한것은 맞다. 그런데

* 팩터리 메소드 패턴
    * 팩토리를 구현하는 방법(inheritance)에 초점을 둔다.
    * 구체적인 객체 생성 과정을 하위 또는 구체적인 클래스로 옮기는 것이 목적이다.
    * 팩터리 메소드 패턴에서는 하나의 메소드가 여러개의 객체를 생성한다. -> 한 종류의 객체를 생성시켜주는 패턴이다.
* 추상 팩토리 패턴
    * 팩토리를 사용하는 방법(composition)에 초점을 둔다.
    * 여러 객체를 구체적인 클래스에 의존하지 않고 만들 수 있게 해주는 것이 목적이다.
    * 추상 팩토리에서는 추상 팩토리를 여러 곳에서 상속받고, 여기서 각각 하나의 객체를 생성한다. -> 서로 연관된/의존적인 객체로 이루어진 여러 객체를 각각 생성시켜주는 패턴이다.

## 어디서 쓰이냐?

* java
    * javax.xml.xpath.XPathFactory#newInstance()
    * javax.xml.transform.TransformerFactory#newInstance()
    * javax.xml.parsers.DocumentBuilderFactory#newInstance()
* spring
    * FactoryBean과 그 구현체
