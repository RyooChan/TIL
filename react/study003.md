# study 003
###### tags: `Tag(처음 만난 리액트)`

## JSX

영어로 `A syntax extension to JavaScript`라는 뜻을 가진다.
쉽게 말해서 JS의 문법을 확장시킨 것이라고 볼 수 있다.

JavaScript + XML/HTML 이라고 생각하면 편할 것이다.

![](https://i.imgur.com/qlJWM0F.png)

간단하게 이 코드를 살펴보면, \<h1> 이라는 테그를 가진 값을 변수로 선언해준다.
즉, javascript와 HTML이 결합되어 있다.

## JSX의 역할

그래서 이거 왜 쓰는걸까?
이 JSX는 내부적으로 XML/HTML코드를 JS로 변환하는 작업을 거치게 된다.
그래서 실제로 우리가 JSX로 코드를 작성하면 최종적으로는 JS코드가 나오게 된다는 것이다.

![](https://i.imgur.com/MNuWxja.png)

여기서 JSX코드를 JS로 변환하는 역할을 하는 것이 바로 React의 createElement라는 함수이다.

### JSX를 사용한 코드

![](https://i.imgur.com/wlcOSyE.png)

먼저 해당 코드는 JSX로 만들어진 코드이다.

이곳에서 Hello 라는 이름의 class가 있고, 이 Component내에서는 javascript와 HTML이 결함된 JSX를 사용하고 있다.
그리고 이렇게 만들어진 Component를 ReactDOM의 render함수를 사용해서 실제 화면에 렌더링하고 있다.

### JSX를 사용하지 않은 코드

![](https://i.imgur.com/DvewrEQ.png)

이번에는 JSX를 사용하지 않고 순사 JS로만 만든 코드이다.
여기서는 기존에 JSX를 사용하던 곳이 React.createElement로 대체된 것을 확인 가능하다.
결국 JSX문법을 사용하면 React에서 내부적으로 createElement를 사용한다는 것이다.
최종적으로 이 createElement를 호출한 결과 JS 객체가 등장하게 된다.

![](https://i.imgur.com/lzYJh7g.png)

이 두 함수는 동일한 역할을 한다.

![](https://i.imgur.com/HAvX9It.png)

그리고 두 함수의 결과는 이렇게 나온다.
React는 이 객체들을 읽어서 DOM을 만들어서 사용하고 항상 최신으로 유지해 준다.

이 객체는 element라고 부른다.


### createElement 파라미터 구조

![](https://i.imgur.com/iQVJVlQ.png)

먼저 첫 번째 파라미터는 element의 유형을 나타낸다.
이 유형이란 것은 `div, span 등등이나 react component`가 들어간다.

다음으로는 `props`가 들어가게 된다.
이에 대해서는 지금은 속성이 들어간다고만 생각한다.

마지막으로는 `children`이 들어간다.
이 children은 자식 엘리멘트라고 보면 된다.

React는 이런 식으로 JSX를 모두 createElement 형태로 변환한다.

리액트에서 JSX를 쓰는것이 필수는 아니다.
왜냐면 그냥 createElement로 사용하면 되기 때문인데, 이게 더 보기 편하고 쉬워서 쓴다.

## JSX의 장점

### 간결한 코드

아까 봤던 것처럼, JSX를 사용하지 않으면 createElement를 사용해야 한다. 즉 뭔가 쓰기 귀찮아진다.

### 가독성 향상

당연히 간결하고 보기도 쉽다... 이 가독성이 높으면 유지 보수 측면에서도 장점이 있는데, 버그를 발견하기 쉽기 때문에 문제의 파악도 가능해진다.

### Injection Attacks 방어

[Injection Attacks](https://hello-backend.tistory.com/162)를 막을 수 있다!!!

![](https://i.imgur.com/2mm31WX.png)

이렇게 문제가 있는 값을 넣었을 때에, JSX를 사용해주면 막아줄 수 있다.
JSX는 값이 입력되었을 때에 괄호를 사용해서 값을 삽입한다.
기본적으로 react DOM은 들어온 값을 모두 문자열로 변환해준다.

그렇기 때문에 명시적으로 선언되지 않은 값은 괄호 사이에 들어갈 수 없다.
이를 통해 XSS의 공격을 막아준다.

## JSX사용법

기본적으로 JSX는 JS를 확장시킨 것이기 때문에 모든 javascript문법을 지원한다.

그리고 XML / HTML을 사용 중에 중간에 JS를 쓰고 싶으면 {} 요렇게 중괄호로 묶어서 써주면 된다.

![](https://i.imgur.com/8GgGbFk.png)

이런 식으로

### JSX children정의

![](https://i.imgur.com/Xl46MZg.png)

이렇게 div의 children h1, h2를 선언하는 식으로 해주면 된다.

