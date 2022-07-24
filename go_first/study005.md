# study005
###### tags: `Tag(Go언어 프로그래밍)`

## 포인터

> 포인터는 C나 C++에서 지원하는 친구인데, Go도 이걸 지원한다.
> 참고로 java, python, JS등에서도 포인터를 쓰고는 있는데, 이걸 개발자가 직접 쓰는게 아니라 암묵적으로 내부에서 사용하는 것이다.

포인터는, 메모리 주소를 값으로 갖는 타입이다.

```
var a int
var p *int
p = &a		// a의 메모리 주소를 포인터 변수 p에 대입
```

이거는 var p가 int타입의 메모리 주소값을 값으로 가지고 있다는 것이고, a의 메모리 주소를 p가 저장한다.


`*p = 20`

이건 p가 a의 주소를 가지고 있었는데, 그 공간의 값을 20으로 넣어라 라는 것이다.
이렇게 되면 a의 값이 20이 될 것이다.

---

여러 포인터 변수가 하나의 변수를 가리킬 수 있다.

![](https://i.imgur.com/psS0HAJ.png)

```
package main

import "fmt"

func main() {
	var a int = 10
	var b int = 20

	var p1 *int = &a
	var p2 *int = &a
	var p3 *int = &b

	fmt.Printf("p1 == p2: %v\n", p1 == p2)
	fmt.Printf("p2 == p3: %v\n", p2 == p3)
}
```

![](https://i.imgur.com/ubQnZJl.png)

---

포인터 변수의 기본값은 nil이다.
이거 다른데에서의 null이랑 같은거다.

nil은 말하자면 '아무것도 아니다', '무효하다'라는 것이다.
따라서 포인터 변수는 아무 메모리 공간을 가리키고 있지 않다면 무효하다는 것이고, 이것이 nil이다.

nil이 아니라면 p는 유효한 메모리 주소를 가리킨다.

그리고 **GO에서 포인터 변수는 무조건 변수의 주소값만 받을 수 있다.**
한마디로

`p = *int(100)`

이런거 안됨

---

### 포인터 그래서 왜씀?

```
package main

import "fmt"

type Data struct {
	value int
	data  [200]int
}

func ChangeData(arg Data) {
	arg.value = 999
	arg.data[100] = 999
}

func main() {
	var data Data

	ChangeData(data)
	fmt.Printf("value = %d\n", data.value)
	fmt.Printf("data[100] = %d\n", data.data[100])
}
```

요런 식으로 data의 값을 변경하는 함수를 통해 값을 저장해준다고 하자

여기서 ChageData라는 function을 호출해서 값을 변경해줄 것인데, data[100]을 999로 바꾸게 된다.

![](https://i.imgur.com/qJyDtMM.png)

그러면 변경하면 value는 999, data[100]도 999가 나와야 할 것 같은데... 어째 0이 나온다 왜일까??

ChangeData는 arg로 Data타입을 가진 data를 받아왔다.
그래서 그 data를 가지고 값을 바꿨는데, 이거 뭐... 여기서 바꾼 데이터는 실제 main쪽에 있는 data랑은 관계가 없을 것이다.

**이걸 해결하기 위해서 포인터를 써준다**
어떻게 포인터로 바꾸느냐?

*Data 라는 포인터 변수로 &data인 주소값을 받아오게 되면 바꾸어 줄 수 있을 것이다.

```
package main

import "fmt"

type Data struct {
	value int
	data  [200]int
}

func ChangeData(arg *Data) {
	arg.value = 999
	arg.data[100] = 999
}

func main() {
	var data Data

	ChangeData(&data)
	fmt.Printf("value = %d\n", data.value)
	fmt.Printf("data[100] = %d\n", data.data[100])
}
```

![](https://i.imgur.com/CfdfpOW.png)

요렇게

> 그리고 이런 식으로 포인터를 쓰면 무슨 장점이 있을까??

```
만약 값을 그대로 복사한다면 어떻게 될까
data는 int 200개의 크기, value는 int만큼의 크기를 가지고 있다.
크기로 치면 1608바이트 정도 된다.
그러면  ChangeData를 호출할 때 마다 1608바이트의 복사가 이루어 질 것이다.

그럼 포인터를 쓰면??
1608바이트가 아니라 그냥 메모리 주소값만 복사된다.
이거는 8바이트이다.

1608바이트 -> 8바이트
엄청나게 이득이 된다!!!
```

### 구조체 포인터 초기화

![](https://i.imgur.com/XFs5jYf.png)

구조체를 만들면서 그 주소를 바로 가져와서 쓸 수도 있다는 것이다.

### 인스턴스

인스턴스란, 메모리에 할당된 데이터의 실체이다.

![](https://i.imgur.com/IqymS2n.png)

![](https://i.imgur.com/SDbcYXl.png)

Data가 공간에 만들어졌고, 그 공간의 주소를 p가 가리키고 있다고 한다면
instance는 그 실체를 뜻한다.

#### 1개의 인스턴스

![](https://i.imgur.com/bW0B2mC.png)

여기서는 1개의 인스턴스가 만들어진다.
p1만이 Data의 주소를 가진다.
그리고 p2, p3는 p1의 값을 가진다.
그러면 p1, p2, p3는 모두 Data라는 인스턴스를 가리키게 된다.

#### 3개의 인스턴스

![](https://i.imgur.com/d748gXQ.png)

이 때에는 포인터가 아니고, 값 형태고 가지게 된다.
data1, data2, data3는 그냥 각각의 인스턴스를 가지고 있는 것이다.
왜냐면 data1이 하나의 Data를 가지고, data2는 data1을 복사, data3도 data1을 복사해서 가져왔기 때문에 다른 공간을 가지기 때문이다.

이 인스턴스라는 개념이 나오면서 Struct(객체)의 Life cycle이 생긴다.

#### new 내장함수

![](https://i.imgur.com/3vdvqAp.png)

위의 방식은 이미 설명했듯 Data의 주소값을 가져오는 것이다.
아래의 방식에서 new 내장함수를 사용하는 것도 위와 동일한 효과를 가진다.

둘의 차이점은 위의 방식은 필드에 초기값을 넣어줄 수도 있고, 아래의 방식은 필드값의 초기화가 불가능하다는 것이다.(즉 아래의 방식은 기본값으로의 초기화밖에 되지 않는다.)

#### instance는 언제 사라지나??

instance는 어쨌든 메모리를 가지고 있기 때문에 언젠가 없어져야 한다... 그럼 언제 이거를 없애야 할까??

> 인스턴스는 아무도 찾지 않을 때 사라진다.

```
func TestFunc() {
	u := &User{}
	u.Age = 30
	fmt.Println(u)
}
```

여기서 u가 가리키는 공간의 Age를 30으로 바꾸어 준다.
그리고 내부 변수u는 이 func이 끝날 때 사라질 것이다.

그런데 instance는 아직 남아있기는 한데, 더이상 이 instance를 요구하는 친구가 없을 것이다.
그래서 이 instance는 함수가 끝난 후 다음번 garbage collector가 동작할 때에 없애버린다.

### stack메모리와 heap메모리 - 포인터의 외부 사용

```
package main

import "fmt"

type User struct {
	Name string
	Age  int
}

func NewUser(name string, age int) *User {
	var u = User{name, age}
	return &u
}

func main() {
	userPointer := NewUser("AAA", 23)
	fmt.Println(userPointer)
}
```

이런 코드가 있다고 하자
여기서 NewUser이라는 함수는 &u를 반환한다.

그리고 u는 User이라는 struct이다.
c, c++에서는 지역 변수를 stack에 넣어주는데, 이는 해당 사용 지역 외로 넘어가게 되면 바로 없어진다.
그리고 변수는 함수가 끝날 때에 없어지므로 없는 애의 주소를 반환하면 dangling에러가 발생하지 않을까? 하지만 여기서는 

![](https://i.imgur.com/koqjRC9.png)

이렇게 잘 반환이 된다.

**그 이유는 Go에서는 Escape Analysing(탈출 분석) 것이 있는데, 컴파일러에서 코드를 분석하여 어떤 instance가 코드에서 탈출하여 사용되는 경우 stack이 아닌 heap에 저장해주기 때문이다!!**

heap은 프로그램 전체에서 사용되는 친구인데, 이 친구는 쓰임이 있는 한 사라지지 않는다.
그렇기 때문에 해당 지역 외부에서 이 값이 사용되기 때문에 heap에 있는 애가 사라지지 않아서 외부에서 쓸 수 있게 되는 것이다.
