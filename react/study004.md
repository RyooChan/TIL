# study 004
###### tags: `Tag(처음 만난 리액트)`

## Rendering Elements

createElement 함수를 이전에 배웠는데, 이는 이름 그대로 element를 생성해 주는 함수이다.

그렇다면 element가 어떤 것이고, 어떤 역할을 하는지 알아야 할 것이다.

### Elements란?

이는 '어떤 물체를 구성하는 성분' 이라는 뜻을 가진다.

마찬가지로 리액트 엘리먼트도 리액트를 구성하는 요소를 의미한다.
리액트 공식 홈페이지에서는 이를
`Elements are the smallest building blocks of React apps.`
으로 정의해 두었는데, 리액트 앱의 가장 작은 빌딩 블록이라는 것이다.

본래 element는 웹 사이트의 모든 정보를 담고 있는 DOM에서 사용하는 용어이다.
그래서 기존에는 DOM element를 의미했다.

![](https://i.imgur.com/688L0zg.png)

저 탭 '요소'가 이미 element를 뜻한다 즉 DOM은 elements를 모아둔 것임을 알 수 있다.
즉 여기 DOM elements는 실제로 우리가 화면에서 볼 수 있는 친구들이다.

React element와 DOM element의 차이는 무엇일까??

### React element와 DOM element의 차이

`화면에 나타나는 내용을 기술하는 자바스크립트 객체`를 처음에 <기술하다> 라는 뜻을 가진 Descriptor라는 것으로 불렀는데, 최종적으로 이것이 나타나는 형태는 DOM element였기 때문에 DOM과의 통일성을 위해 React에서도 element라고 부르기로 결정하였다.

![](https://i.imgur.com/2oXFyel.png)

해당 그림은 React element와 DOM element를 나타낸 것이다.
실제 react의 DOM에 존재하는 element는 DOM element가, virtual DOM에 존재하는 element가 바로 React element가 되는 것이다.

결국, react element는 DOM element의 가상 표현이라고 볼 수 있다.
그리고 DOM element는 react element에 대비해 많은 정보를 담고 있기 때문에 상대적으로 크고 무겁다.

앞으로 사용될 element라는 표현은 일반적으로 react element를 의미할 것이다.

`React element는 화면에서 보이는 것을 기술한다.`

![](https://i.imgur.com/VqHhokS.png)

앞에서 JSX를 배울 때에 본 코드를 다시 살펴보자

이 코드는 JSX를 사용해서 작성된 코드이다.
이전에 말했듯, JSX를 통해 만들어진 코드는 createElement를 사용하여 만들어지게 된다.
결국 이렇게 생성된 것이 바로 react element가 된다.

이것을 사용해서 실제 우리가 활용할 DOM element가 만들어지게 된다.

### Elements의 생김새

이 element가 화면을 구성하는데, 이게 실제로 어떻게 생겼을까???
리액트 elements는 **JS객체 형태로 존재**한다.

element는 컴포넌트 유형과 속성 및 내부 자식의 모든 정보를 포함하고 있는 일반적인 JS객체이다.
이 객체는 불변성을 갖는다(마음대로 바꿀 수 없다)

![](https://i.imgur.com/1Sq9v0X.png)

이 코드는 버튼을 나타내기 위한 코드이다.
여기처럼 type에 html태그 이름이 문자열로 들어가는 경우, element는 해당 태그를 가진 DOM node를 나타내고 props는 속성에 해당한다.

위 element가 실제로 렌더링되면

![](https://i.imgur.com/pemhHuv.png)

요런 DOM element가 된다.

그러면 element에 html이 문자열로 들어가지 않으면 어떻게 될까???

![](https://i.imgur.com/3KwkXwc.png)

이렇게 react의 component element를 나타내는 경우가 있다.
여기는 type에 문자열이 아닌, react component의 이름이 들어간다.

element는 JS의 객체 형태로 존재한다.
그리고 이 객체를 만드는 역할을 하는 것이 createElement이다.

![](https://i.imgur.com/XuYdZhM.png)

이전에 본 것처럼 createElement를 할 때에 3 가지의 파라미터가 들어가는데,

첫 번째로 type이 들어간다.
이곳에 만약에 react component를 넣는다면?

react component는 최종적으로는 HTML 태그를 사용하도록 되어 있다.
하나의 컴포넌트는 여러 개의 자식 컴포넌트를 가질 수 있고, 자식 컴포넌트를 모두 쭉 분해하면 결국 HTML태그가 나오는 것이다.


두 번째 파라미터는 props가 들어간다.
간단하게, 이 부분은 element의 속성이라고 생각하면 된다.
개발자 도구에서 HTML태그에서 그 안에 class, style등등의 속성 즉 `attribute`를 이곳에서 선언 가능하다.
props는 사실 이 attribute보다 상위의 복잡한 것이지만, 여기서는 그냥 attribute라고 이해하고 넘어가도록 한다.


세 번째 파라미터는 children이 들어간다.
해당 element의 자식 element들이 이 부분에 들어가게 된다.
실제 개발자 도구의 내용에서는 하나의 HTML태그의 하위에 여러 개의 HTML태그가 나오는 것을 확인 가능하다.
이러한 HTML태그들이 결국 자식 element가 된다.

### createElement의 실제 동작

![](https://i.imgur.com/a1HKtHJ.png)

이 코드에는 Button과 ConfirmDialog 컴포넌트가 있으며 confirmDialog컴포넌트가 Button 컴포넌트를 포함하고 있다.

여기에서 confirmDialog 컴포넌트의 element는 

![](https://i.imgur.com/7Dr43VR.png)

이런 형태가 될 것이다.

첫 번째 children은 type이 \<p> 태그인 형태여서 바로 렌더링이 가능하다.

두 번쨰 children은 HTML태그가 아닌 react component의 이름은 Button이다.
이 경우 react는 Button component의 element를 생성해서 합치게 된다.
그래서 최종적으로 element는

![](https://i.imgur.com/moqceFJ.png)

이러한 형태가 될 것이다.
