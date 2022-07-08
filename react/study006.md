# study 006
###### tags: `Tag(처음 만난 리액트)`

## Components and Props

**매우 중요한 내용이니, 반드시 이해하고 넘어갈것!!**

### Component

먼저 React는 `Component-Based` 즉 컴포넌트 기반이라는 특징이 있다는 것을 배웠다.
리액트는 여러 개의 컴포넌트로 구성되어 있고, 하나의 컴포넌트는 또 다른 컴포넌트의 조합으로 구성될 수 있다.

![](https://i.imgur.com/SVx4yol.jpg)

![](https://i.imgur.com/WGO3bl8.png)

이것은 에어비엔비의 웹사이트인데, A와 B로 표시된 부분들이 각각 사이트의 컴포넌트이다.
그리고 이러한 컴포넌트를 여러 번 반복적으로 사용해서 하나의 페이지를 만들어내고 있다.

리액트가 컴포넌트 기반이라는 것은 여기서 볼 수 있듯, 작은 컴포넌트들이 하나의 컴포넌트를 구성하고, 또 그 컴포넌트들이 모여 사이트를 구성하기 때문이다.

그리고 이런 컴포넌트들은 재사용성이 뛰어나 자연스레 유지보수와 개발 시간의 단축이 가능해진다.

![](https://i.imgur.com/33SjQZk.png)

개념적으로 React componenet는 JS의 함수와 비슷하다.
함수가 입력을 받아 출력을 내뱉는것처럼 component도 마찬가지로 입력 -> 출력이 이루어진다.

다만 이 React component의 입력과 출력은 JS의 그것과는 많이 다르다.

![](https://i.imgur.com/H4I6XXV.png)

리액트 컴포넌트에서의 입력은 `Props`, 출력은 `React element`이다.
결국 이 리액트 컴포넌트의 역할은 어떤 속성들을 받아서 이를 React element로 변환해주는 것이다!!

![](https://i.imgur.com/rOCyFxV.png)

이런 느낌으로 붕어빵 기계에 반죽을 부어서 붕어빵을 만드는 것이라 생각하면 편하다.
component는 붕어빵 틀, element는 붕어빵이다.

이거는 java 객체지향에서 class와 instance의 개념과 비슷하다

### Props

위의 component에서 이게 입력으로 들어갔었다.

먼저 prop은 `property == 속성`의 줄임말이다.

이 '속성' 은 Component의 속성을 가리킨다.

위의 붕어빵에서 props는 재료를 의미한다.

![](https://i.imgur.com/eExUWw3.png)

이렇게 팥, 슈크림, 고구마 등등을 넣어주면 같은 붕어빵 모양이지만 내용물이 다르다.
이렇게 props는 Component의 속재료이다.

![](https://i.imgur.com/bdMcInx.png)

아까의 에어비앤비 화면을 다시 가져와서 보면

총 4개의 여행지의 경우 모양은 같지만 내부의 이미지, 색상 등은 모두 다르다.
즉 같은 component에서 생성되었지만, 다른 props가 들어갔다는 것이다.

정리하면 Props는

> 컴포넌트에 전달할 다양한 정보를 담고 있는 자바스크립트 객체

이다.

### Props의 특징

Props의 중요한 특징은 바로 `Read-Only`라는 것이다.
읽을 수만 있다 == 값을 변경할 수 없다.

`즉 이 props는 한번 사용되면 element 내부에서 변경할 수 없다는 것이다!!`

JS함수의 속성에 대해 먼저 말하자면

![](https://i.imgur.com/LcLR1RL.png)

여기서 a와 b의 값을 여기서는 변경하지 않는다.
이렇게 a와 b에 변경 없이 이를 활용하는 함수를 우리는 Pure하다라고 한다.
말 그대로 순수하다는 것으로 `입력값을 변경하지 않으며, 같은 입력값에 대해서는 항상 같은 출력값을 리턴한다는 것이다.`

![](https://i.imgur.com/t3yifd8.png)

그리고 여기 있는 함수는 순수하지 않다.
account와 amount를 받아서, 그 account에 있는 total을 변경해주게 된다.
여기서는 입력으로 받은 account를 변경했는데, 이런 경우 impure하다는 것이다.

그리고 React에서 컴포넌트의 특징을 기술할 때에

`All React components must act like pure functions with respect to their props.`

`모든 리액트 컴포넌트는 그들의 Props에 관해서는 pure함수 같은 역할을 해야한다.`

라고 한다.
즉!! 모든 리액트 컴포넌트는 Props를 직접 바꿀 수 없고, 같은 props에 대해서는 항상 같은 결과를 보여주어야 한다는 것이다.

### Props의 사용법

컴포넌트의 Props를 전달하려면??

![](https://i.imgur.com/Fs2Gw8G.png)

먼저 JSX를 사용하는 경우에는 이렇게 key-value의 형태로 컴포넌트에 props를 넣을 수 있다.
여기서 profile에 name, introduction, vieCount의 세 가지 속성을 넣어 주었다.

결과적으로 Props는

![](https://i.imgur.com/LmaAcjM.png)

이런 형태의 javascript 객체가 된다.

![](https://i.imgur.com/9oTZcKD.png)

추가로 props에 중괄호 \{} 를 사용하여 이런 식으로 내부에 component를 넣어줄 수도 있다.
즉 이 layout props의 값으로는 높이 너비와 header, footer가 들어오게 된다.

만약 JSX없이 이를 쓴다면

![](https://i.imgur.com/oJZ3ooK.png)

이렇게 해주어야 할 것이다.
참고로 이거는 어지간하면 JSX를 쓰자


### Component 만들기

![](https://i.imgur.com/dIfPGBL.png)

리액트에서 Component는 이렇게 크게 Function Component와 Class Component로 나뉜다.
초기 버전에서는 주로 Class Component를 사용했다.
하지만 이게 사용하기가 좀 불편해서 이후에는 함수컴포넌트(Function Component)를 개선해서 쓰게 되었다.

요즘은 Function component의 훅을 주로 사용한다고 한다...

#### Function Component

앞에서 Props에 대해 설명할 때에 모든 react component는 pure함수같은 역할을 해야한다고 했다.
이 말은 결국 react의 component를 간단한 함수라고 생각하면 된다는 것이다.

![](https://i.imgur.com/qt9BTCO.png)

여기서 이 함수는 하나의 props 를 받아서 인사말이 담긴 element를 return하기 때문에 컴포넌트라고 할 수 있다.
그리고 이런걸 함수 컴포넌트라고 한다.

함수 컴포넌트는 간단하다는 장점을 갖는다.

#### Class Component

이거는 javascript ES6의 Class를 이용해서 만들어진 Component이다.
이거는 Function Component에 비해 몇 가지 추가적인 기능을 갖고 있다.
그 내용은 이후에 사용한다.

![](https://i.imgur.com/UIdxZWa.png)

여기서 위의 Function Component로 만든 것과 동일한 기능을 갖는 친구를 Class Component로 만들어 준다.

Function Component와의 가장 큰 차이는 여기 Class Component는 모두 React.component를 상속받아서 쓴다는 것이다.

그리고 **Componenet의 이름은 항상 대문자로 시작해야 한다.**

![](https://i.imgur.com/s9i3c3V.png)

이런 식으로, 만약 대문자가 아니라 소문자를 쓰면 리액트는 얘를 DOM 태그라고 인식하기 때문이다.
여기 컴포넌트 이름을 소문자로 쓰면 DOM 태그로 판단할텐데, 이거 실제로는 없는 것이라 아마 에러가 발생할 것이다. 혹은 생각과는 다른 기능을 수행할수도 있다.

### Component 렌더링

참고로 Component는 실제로 렌더링되지 않는다. 이 Component를 통해 만들어진 element가 렌더링되게 되는 것이다.
따라서 렌더링을 위해서는 가장 먼저 Component를 통해 element를 만들어야 할 것이다.

![](https://i.imgur.com/TCK37V7.png)

![](https://i.imgur.com/M2olVEv.png)

이것은 위의 코드를 실제로 렌더링할 때에 사용하는 코드이다.

여기서 보면 Welcome이라는 Component class에 인제라는 값이 들어간다.
이후 ReactDOM에서 이 element를 생성한 뒤, 렌더링한다.

### Component 합성

이는 여러 개의 Component를 합쳐서 하나의 Component를 만드는 것이다.

리액트에서는 Component 안에 또 다른 Component를 쓸 수 있기 때문에, 복잡한 화면을 여러 개의 화면으로 나누어서 사용할 수 있다.

![](https://i.imgur.com/j9NujtO.png)

이런 식으로, 하나의 Component내에 Welcome Component를 여러 개 넣어줄 수 있는데, 이렇게 하나의 Component내에 여러 개의 다른 Component를 넣어주는 것을 Component합성이라고 한다.

![](https://i.imgur.com/QjJWfs2.png)

여기 그림처럼 App Component는 3개의 welcome Component를 갖고 있고, 이 Welcome Component들은 각기 다른 props를 갖고 있다.

### Component 추출

위와는 반대로 복잡한 Component를 쪼개서 여러 개의 Component로 나눌 수도 있다.
이런 걸 Component추출이라고 한다.

큰 컴포넌트에서 일부를 추출해서 새로운 컴포넌트를 만드는 것인데, 이를 통해 재사용성을 향상시킬 수 있다.
그리고 이 재사용성을 통해 개발 속도를 올려줄 수 있다.

![](https://i.imgur.com/gZkFS0n.png)

여기서 Comment라는 Component는 내부에 작성인의 이름, 프로필, 작성일 등등을 포함하고 있다.
그리고 이 Component의 props는 오른쪽의 내용일 것이다.

이제 여기서 Component를 하나씩 추출해 본다.

#### 1. Avatar 추출하기

![](https://i.imgur.com/NIdfwDH.png)

첫번째로 Avatar 추출이다.
여기서 Comment는 image를 이용해서 사용자의 아바타를 표시하고 있다.

이 부분을 추출해서 

![](https://i.imgur.com/0bPRPCc.png)

이렇게 별도의 Avatar component를 만들어 준다.

참고로 위의 author props대신 여기서는 user라는 이름의 props를 사용했는데, 이는 다른 Component에서 쓸 때에는 user가 더 보편적이기 때문이다.
즉, 재사용성을 고려한 상황이다.

![](https://i.imgur.com/4QObQ4t.png)

이렇게 추출된 Avatar component를 실제로 적용한 코드이다.

#### 2. UserInfo 추출하기

![](https://i.imgur.com/9PnU7iW.png)

사용자 정보를 담고 있는 부분을 추출해 본다.

![](https://i.imgur.com/kH1tKkP.png)

이런 식으로 UserInfo라는 Component로 추출해 준다.
참고로 여기 있는 Avatar은 이전에 만든 Component이다.

![](https://i.imgur.com/3nzpLjz.png)

이렇게 추출된 UserInfo component를 실제로 적용한 코드이다.

![](https://i.imgur.com/arhio1d.png)

이 Component를 표현하면 이와 같다.

Comment가 UserInfo를, Avatar를 또 가지고 있는 구조이다.

> 이런 식으로 기능별로 재사용이 용이하도록 추출하면 된다.
