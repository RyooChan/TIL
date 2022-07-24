# study004
###### tags: `Tag(Go언어 프로그래밍)`

## 구조체

> 여러 필드를 묶어서 사용하는 타입

![](https://i.imgur.com/V178ksm.png)

### 구조체 선언

```
type 타입명 struct {
	필드명 타입
	...
	필드명 타입
}
```

새로운 type을 선언하겠다고 한 뒤에 타입명 선언, 이후 구조체(struct)를 선언하면 된다.

ex)

```
type Student struct {
	Name 	string
	Class 	int
	No 		int
	Score 	float64
}
```

`var a Student`

이후 a라는 변수가 구조체 Student타입이라고 선언해서 사용해줄 수 있다.

### 구조체 변수 초기화

* var house House
	* 모든 필드값이 기본값으로 초기화된다.
* var house House = House{"경기도 군포시", 50, 10, "아파트"}
* var house House = House{
	"경기도 군포시",
	30,
	10,
	"아파트",  `// 마지막에 꼭 쉼표를 달아야 한다!!`
}	
* var house House = House{Size:28, Type:"아파트"}
	* 특정 필드만 값이 들어가고 나머지는 기본값으로 초기화
* var house House = House{
	Size:28, 
	Type:"아파트",
}

### 구조체를 포함하는 구조체

```
type  User struct {
	Name string
	Id string
	Age int
}
```

```
type VIPUser struct {
	Userinfo User
	VIPLevel int
	Price int
}
```

### 구조체를 필드로 갖기


```
type  User struct {
	Name string
	Id string
	Age int
}
```

```
type VIPUser struct {
	User	// embedded field
	VIPLevel int
	Price int
}
```


이러면 embedded field가 된다.

이렇게 하면 따로 User의 별칭 없이 한번에 찾아갈 수 있다.

```
vip.Name
vip.Id
vip.Age
```

이런 식으로

> 참고로 만약에 VIPUser내에 User의 변수가 있는 경우는 그게 우선되어 가져와진다.
> 이 때에는 vip.User.Name 이런 식으로 가져와야 한다.

### 구조체의 역할

결합도(의존성)은 낮게, 응집도는 높게 유지시켜준다.

* 함수는 관련 코드 블록을 묶어서 응집도를 높이고 재사용성을 증가시킨다.
* 배열은 같은 타입의 데이터를 묶어서 응집도를 높인다.
* 구조체는 관련된 데이터들을 묶어서 응집도를 높이고 재사용성을 증가시킨다.

그래서 이 구조체는 객체 지향 프로그래밍의 기반이 된다.
