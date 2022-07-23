# study003
###### tags: `Tag(Go언어 프로그래밍)`

## 배열

배열은 자료구조 중 한 종류이다.

`var 변수명 [요소 개수]타입`

이렇게 배열을 선언해줄 수 있다.

ex) `var t[5]float64`

```
package main

import (
	"fmt"
)

func main() {
	var t [5]float64 = [5]float64{24.0, 25.9, 27.8, 26.9, 26.2}

	for i := 0; i < 5; i++ {
		fmt.Println(t[i])
	}
}
```

![](https://i.imgur.com/RWpueVT.png)

### 다양한 배열 변수 선언

var nums [5]int

days := [3]string{"monday", "tuesday", "wednesday"}

var temps [5]float64 = [5]float64{24.3, 26.7}

var s = [5]int{1:10, 3:30}
0 10 0 30 0 -> 기본값은 0으로 채워지고 `1번인덱스:10` `3번인덱스:30` 으로 된다.

x := [...]int{10, 20, 30}
이러면 그냥 크기가 3개로 알아서 맞춰진다.

> 배열 선언시 개수는 항상 상수이다!!

### 다중 배열

Go도 다중배열을 지원한다.

`var b [2][5]int`

### 배열 복사

Go도 얕은복사와 깊은복사를 지원한다.
