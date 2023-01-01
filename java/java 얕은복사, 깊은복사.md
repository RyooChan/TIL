# 깊은복사 vs 얕은복사(feat.clone)

간단하게 말하면

* 얕은복사
    * 객체의 주소값을 복사함
* 깊은복사
    * 객체의 실제 값을 복사함

이거임.
솔직히 뭐 딱히 쓸말이 없긴하다...

그래도 좀 상세히 알아보아요

## 얕은복사

먼저 얕은복사이다.

```
@Getter
@Setter
@AllArgsConstructor
public class Person {
    private String name;
    private int iq;
}
```

이런 식으로 `Person`이라는 객체를 선언해 줄 것이다.
그리고

```
    @Test
    public void 복사() {
        Person person = new Person("류찬", 200);
        Person ryoochan = person;

        ryoochan.setIq(300);

        System.out.println("본래의 Object 류찬의 IQ = " + person.getIq());
        System.out.println("복사한 Object 류찬의 IQ = " + ryoochan.getIq());
    }
```

이렇게 본래의 person과 이를 복사한 ryoochan을 정의한다.
여기서 

* person
    * iq를 200으로 초기화함.
* ryoochan
    * person을 받아서 iq를 300으로 변경함.

이제 결과를 보면 예상대로면

* person
    * iq 200
* ryoochan
    * iq 300

으로 나와야 할 것 같은데

![](https://i.imgur.com/ul72TJn.png)

이렇게 둘다 300의 iq를 갖게 된다!!

아니 이게 뭐지? 싶...지는 않을것이다. 왜냐면 이미 위에서 얕은복사는 참조값을 복사한다고 했거든.
즉, 

```
    @Test
    public void 복사() {
        Person person = new Person("류찬", 200);
        Person ryoochan = person;

        ryoochan.setIq(300);

        System.out.println("본래의 Object = " + person);
        System.out.println("복사한 Object = " + ryoochan);
    }
```

이렇게 각각의 객체를 출력시켜보면

![](https://i.imgur.com/pCiY86A.png)

이렇게 같은 주소값이 출력된다.

## 깊은복사

먼저 깊은복사를 하려면

일단 Cloneable이라는 interface가 있는데 그 내용을 잠깐 들여다보자.
아무것도 없이 주석으로 설명이 쭈르르륵 나와있을 것이다.

![](https://i.imgur.com/3Lohpwa.png)

요거를 해석하면

클래스는 Cloneable 인터페이스를 구현하여 Object.clone() 메서드에 해당 메서드가 해당 클래스 인스턴스의 필드 간 복사본을 만드는 것이 적법함을 나타냅니다.
Cloneable 인터페이스를 구현하지 않는 인스턴스에서 객체의 복제 메서드를 호출하면 CloneNotSupportedException 예외가 발생합니다.
규칙에 따라 이 인터페이스를 구현하는 클래스는 공용 메서드로 Object.clone(보호됨)을 재정의해야 합니다. 이 메서드 재정의에 대한 자세한 내용은 Object.clone()을 참조하십시오.
이 인터페이스에는 복제 방법이 포함되어 있지 않습니다. 따라서 이 인터페이스를 구현한다는 사실만으로는 개체를 복제할 수 없습니다. 복제 메소드가 반영적으로 호출되더라도 성공한다는 보장은 없습니다.

이렇게 나온다.
여기 써있는대로면 Object.clone을 재정의해서 써야한다는 것이다.

자.. 그럼 한번 재정의해서 써보자

```
@Getter
@Setter
@AllArgsConstructor
public class Person implements Cloneable{
    private String name;
    private int iq;
    
    @Override
    protected Person clone() throws CloneNotSupportedException {
        return (Person) super.clone();
    }
}
```

이렇게 clone메서드를 재정의해서 사용할 것이다.
이제 테스트를 한번 해준다.

```
    @Test
    public void 복사() throws CloneNotSupportedException {
        Person person = new Person("류찬", 200);
        Person ryoochan = person.clone();

        ryoochan.setIq(300);

        System.out.println("본래의 Object 류찬의 IQ = " + person.getIq());
        System.out.println("복사한 Object 류찬의 IQ = " + ryoochan.getIq());
    }
```

이렇게 Test를 진행해주면

![](https://i.imgur.com/aDqfIb8.png)

이렇게 각각의 객체는 다른 값을 갖게 되고

```
    @Test
    public void 복사() throws CloneNotSupportedException {
        Person person = new Person("류찬", 200);
        Person ryoochan = person.clone();

        ryoochan.setIq(300);

        System.out.println("본래의 Object = " + person);
        System.out.println("복사한 Object = " + ryoochan);
    }
```

주소를 확인해 보아도

![](https://i.imgur.com/j7jVyiT.png)

다름이 확인된다!!
