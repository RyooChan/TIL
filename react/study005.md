# study 005
###### tags: `Tag(처음 만난 리액트)`

## React element의 특징

먼저 react elements는 굉장히 중요한 특징을 갖는다.
이는 이전에 설명한 `immutable`즉, `불변성` 인데, 말 그대로 변하지 않는다는 것이다.

따라서 한번 생성된 element는 변하지 않는다.

**다시 말해, Element 생성 후에는 children이나 attributes를 바꿀 수 없다!!!!!**

그러면 이 불변성을 갖는 element를 통해 화면의 출력 등을 어떻게 해줄까??

![](https://i.imgur.com/LBEOD7f.png)

이렇게 화면에 새로운 내용을 보여주기 위해 virtual DOM은
변경할 부분 확인 -> compute Diff -> 재 렌더링
의 과정을 수행한다.

여기서 동그란 각 원들이 element이고, 빨간색 원은 변경된 element이다.

그니까 화면을 변경하려 하면 이 element를 바꿔서 새로 달아주면 된다.

참고로 이 화면 갱신은 성능에 큰 영향을 끼치게 된다.


## elements의 렌더링

![](https://i.imgur.com/VbppqOz.png)

먼저 이 간단한 root라는 아이디를 갖는 div가 있다.
이 div태그 안에 react element들이 렌더링되며, 이를 Root DOM Node라고 부른다.
이 div태그 안의 모든 것이 react DOM에 의해 관리되기 때문이다.

모든 div태그를 이용해 만들어진 것들은 단 하나의 root DOM node를 갖는다.
반면 기존 웹사이트에 추가적으로 react를 연동하게 되면 여러개로 분리된 root DOM node를 가질 수도 있다.

![](https://i.imgur.com/GeKCBpC.png)

이렇게 가장 최상단에 있는 node이다.

![](https://i.imgur.com/l3JJv7r.png)

이 코드는

1. element를 하나 생성
2. 생성된 element를 root node에 렌더링

하는 코드이다.

렌더링을 위해 ReactDOM에 render라는 함수를 사용한다.

## 렌더링된 element를 업데이트하기

먼저 element는 `불변성`을 띈다.
element는 한번 생성되면 바꿀 수 없기 때문에 한번 이를 생성하면 바꿔야 한다.

![](https://i.imgur.com/HZtJ1uh.png)

이 함수는 tick함수를 통해 현재 시간을 포함하는 element를 만들어서 root div에 렌더링한다.
그리고 setInterval을 사용하여 이 tick함수를 매 초마다 호출한다.

이 코드의 실행 결과로는 매 초마다 새로운 element가 생성되어 바뀌게 될 것이다.
