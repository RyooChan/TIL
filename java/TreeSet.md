# TreeSet!!

```
public class TreeSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable {}
```

TreeSet의 구조를 살펴보면 다음과 같다.
이는 `AbstractSet`, `NavigableSet`인터페이스를 구현하여 사용하고 있다.

TreeSet은 이름처럼 Tree와 같은 구조를 가지고 원소들을 저장한다.
특징을 나열하자면

* 중복 불가
* 원소의 순서 보존되지 않음
    * 집어넣은대로 들어가는게 아니라 알아서 트리 구조로 정렬시킨다.
* 요소를 오름차순으로 정렬한다.
* Thread-safe하지 않다.

## Red-Black-Tree

TreeSet은 내부적으로 Red-Black-Tree를 사용하고 있다.
이게 뭘까...?

### Red-Black-Tree 알고리즘

얘는 일종의 자기 균형 이진탐색 트리이다.

이진탐색트리란 `자신의 왼쪽 서브 트리에는 현재 노드보다 작은 것, 오른쪽 서브 트리에는 큰 것만을 가질 수 있다.`

이 덕분에 이진탐색트리에서는 조회를 할 때에 O(log n)의 시간 복잡도를 갖는다.

그런데 만약

![](https://i.imgur.com/zKKVtF1.png)

요런 식으로 조회하면 O(n)의 시간 복잡도를 갖는다.

Red-Black-Tree 알고리즘은 이런 문제를 해결하기 위해 도입되었다.

[이 글](https://hello-backend.tistory.com/248)을 보면 조금 레드블랙트리 알고리즘에 대해 알 수 있을 것이다.

### 참고로

내부적으로 저렇게 값이 삽입될 때 마다 굉장히 복잡한 알고리즘을 사용한다.
그렇기 때문에 이는 데이터의 삽입/삭제 시 오랜 시간이 걸린다.
주로 검색할 때에 유리한 구조이다.

### TreeSet의 데이터 저장

왜 저장이 오래 걸릴지 깊숙히 함 보자.
TreeSet은

`boolean add(Object o)`

add를 사용해서 객체를 저장한다.
이게 왜 굳이 boolean을 썼을까?
Set은 중복을 허용하지 않기 때문에, add를 할 때에 `equals()`와 `hashcode()` 메소드를 호출한다.
그래서 이미 있는애인 경우 저장이 실패(false반환)해야 해서 boolean을 사용하는 것이다.

그래서인데 이거 매번 값을 저장할 때마다 아래로 쭈루루루룩 가면서 값을 비교한다.
보면 알겠지만 계속 비교를 진행하면서 해야돼서... 값이 늘어날 때마다 비교도 많아지고 시간도 오래 걸리게 된다..

## 주의점

### null저장

java7 이전에는 TreeSet에 `null`이 저장 가능했다.
근데 당연한건데 null을 저장하면 문제가 발생할 여지가 너무 많다.
(null을 어떻게 비교해서 넣을건데...)

그래서 아예

```
    @Test
    public void TreeSetTest() {
        TreeSet<Integer> ts = new TreeSet<>();
        ts.add(null);
    }
```

![](https://i.imgur.com/R9z7JRq.png)
NPE가 발생해버리게 된다.

## 종합

TreeSet은 위의 설명에서 보았을 때

* 값을 바로바로 `정렬`하면서 보관한다.
    * 이 정렬은 레드블랙트리 알고리즘을 사용한다.
        * 매우 복잡쓰
* 검색에 좋은 알고리즘이다.

## 시간 복잡도

* 삽입
    * O(logN)
* 삭제
    * O(logN)
* 검색
    * O(logN)
