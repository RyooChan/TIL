# this

## 생성자 this()

> 생성자에서 다른 생성자를 호출할 때 사용
> 다른 생성자 호출시 첫 줄에서만 사용가능

## 예시

```
class Car2 {
    String color;     
    String gearType;
    int door;
    
    
   Car2() {
       this("white", "auto", 4);
   } 
   
   Car2(String color) {
       this("color", "auto", 4);
   }
   
   Car2(String color, String gearType, int door) {
       this.color = color;
       this.gearType = gearType;
       this.door = door;
   }
}
```

요런 식으로 생성자에서 같은 클래스 안의 생성자를 호출할 때 사용한다.
-> 사실 이거는 걍 Car2("color", "auto, 4"); 를 호출해주는것이다.

위에 적혀있듯 이거 첫줄 아니고 다른줄에 쓰면 에러난다.

* 이걸 통해 코드의 중복을 없애줄 수 있다.

## 참조변수 this

근데 이거 참조변수 this는 위의 생성자 this랑은 아에 다르다.
둘은 연관이 없음!!

> 인스턴스 자신을 가리키는 참조변수
> 인스턴스 메서드(생성자 포함)에서 사용가능
> 지역변수와 인스턴스 변수를 구별할 때 사용

```
Car(String color, String gearType, int door) {
    this.color = color;
    this.gearType = gearType;
    this.door = door;
}
```

이렇게 구분지을때 사용 ^~^

## 참조변수 this와 생성자 this()

### 참조변수 this

* 인스턴스 자신을 가리키는 참조변수. 인스턴스의 주소가 저장되어 있다.
* 모든 인스턴스메서드에 지역변수로 숨겨진 채로 존재한다.

### 생성자 this()

* 생성자, 같은 클래스의 다른 생성자를 호출할 때 사용한다.
