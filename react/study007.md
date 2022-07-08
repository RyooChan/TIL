# study 007
###### tags: `Tag(처음 만난 리액트)`

## State and Lifecycle

매우매우 중요한 부분이다!!

이는 React component중 주로 Class Component와 관련된 내용이다.

물론 State는 Function component에서도 활용된다.
lifecycle은 그냥 배워두면 좋을정도이다.

**State는 React의 핵심 중의 핵심이다.**

### State

State는 '상태'이다.

리액트에서의 `State는 리액트 Component의 상태`를 의미한다.
-> 실질적으로는 리액트 Component의 데이터라는 의미에 가깝다.

즉 State란, 리액트 Component의 변경 가능한 데이터라는 것이다.

이 state는 사전에 미리 정해진 것이 아니라 개발자가 정의하는 것이다.

> 중요한 것은 꼭 렌더링이나 데이터 흐름에 사용되는 값만 state에 포함시켜야 한다는 것이다.

> state가 변경될 경우 컴포넌트가 재렌더링되기 때문에, 렌더링 혹은 데이터 흐름에 관계없는 것을 포함하면 불필요한 경우에 컴포넌트가 재렌더링되어 성능을 저하시킬 수 있기 때문이다.

위의 것처럼 렌더링이나 데이터 흐름과 관계없는 것들은 instance build로 정의하면 된다.

이 state는 JS의 객체이다.

![](https://i.imgur.com/HzUvyTq.png)

이 클래스는 LikeButton을 정의하는 React class Component이다.
모든 class Component에는 constructor라는 생성자 함수가 존재하는데, 이는 class component가 실행될 때 생성된다.

이 생성자를 보면 `this.state`라는 부분이 나오는데 이 부분이 현재 state를 정의하는 부분이다.
class state는 state를 생성자에서 정의한다.

이렇게 **한번 정의된 state는 직접 수정 할 수 없다 -> (할 수는 있지만)수정하면 안된다!**

![](https://i.imgur.com/w9ks3vp.png)

state를 직접 수정을 할 수는 있다.
하지만 이렇게 직접 수정하면 원하는 방법대로 동작하지 않을 수 있기 때문에 변경할 일이 있을 때에는 반드시 setState함수를 사용해서 변경해야 한다.

### LifeCycle

생명주기라는 뜻이다.
즉 Component가 생성되는 시점과 사라지는 시점이 정해져 있다는 의미이다.

![](https://i.imgur.com/r96yHAZ.png)

리액트 클래스 컴포넌트의 생명주기인데, 이는 출생, 인생, 사망으로 나뉘어 있다.

아래쪽 초록색 애들은 생명주기의 상황에 따라 호출되는 함수이다.
이 함수들을 LifeCycleMethod라고 부르며 생명주기함수라고 한다.

---

먼저 컴포넌트가 생성되는 시점, 즉 출생을 Mount라고 부르는 때에 constructor(생성자) 가 생성된다.
이 constructor(생성자)에서는 component의 state를 정의하게 된다.
또한 Component가 렌더링되며 이후 componentDidMount함수가 호출된다.

리액트 컴포넌트는 생애동안 변화를 겪으며 여러 번 렌더링된다.
이 과정을 Update라고 한다.
이 Update과정에서는 Component의 props가 변경되거나 setState에 의해 state가 변경되거나 forceUpdate 강제 업데이트 함수로 인해 컴포넌트가 다시 렌더링된다.
그리고 렌더링 이후에 componentDidUpdate함수가 호출된다.


마지막으로 리액트 컴포넌트는 언젠가 사망하게 되는데, 이를 unmount라고 부른다.
이 사망은, 상위 컴포넌트에서 현재 컴포넌트를 더이상 사용하지 않게 될 때 이루어진다.
이 때 unmount직전에 componentWillUnmount함수가 호출된다.

`component는 계속 존재하는 것이 아니라 시간의 흐름에 따라 생성되고 업데이트 되다가 사라진다.`
