# java의 HashMap과 해시 충돌(collision) 관련

[이거](https://hello-backend.tistory.com/210)랑 [이거](https://hello-backend.tistory.com/110)를 먼저 읽고오는것도 괜찮다.

## HashMap의 collision

위에 글을 읽으면 알겠지만, hashMap은 key를 해싱시켜 저장한다.
즉 어떤 key가 들어왔을 때에 이를 변환시키게 되고, 당연한 일이지만 이 때에 같은 변환값을 갖는 해시 충돌은 반드시 일어날 수밖에 없다.

그러므로 이를 해결하기 위해 어떤 방식을 도입했는지에 대해 알아본다.
-> 해결이란, 충돌을 아예 없애는 것이 아니라 최대한 줄이는 방법이다.

### 개방 주소법(open addressing)

간단하게 말하면 겹치면 다른곳에 저장하는 것이다.
만약 충돌이 발생하면 다른 주소에 값을 저장하는 식이다.

그리고 이 주소를 찾아가는 알고리즘도 여러 개가 있다.

* 선형 탐사법 (Linear probing)
    * 말 그대로 그냥 선형으로 검색해서 빈곳에 값을 넣는 알고리즘
    * 24 -> 25 -> 26 -> 27 ... 대충 이런식으로 충돌이 발생하면 균등하게 옆으로 찾아가면서 확인하고 값을 넣는다.
    * 해시 충돌이 값 전체에 균등하게 발생하는 경우 유용하다.
    * 다만 만약 충돌난 지점을 기준으로 균등하게 찾을 때에 값들이 균일하게 있으면 성능이 확 떨어진다.
* 제곱 탐사법 (Quadratic probing)
    * 선형 탐사법이랑 비슷한데, 이름처럼 제곱으로 빈곳을 찾아가는 알고리즘
    * 1 -> 3 -> 7 -> 15 -> 31 ... 이런 식으로 제곱으로 빈칸을 찾아간다.
    * 데이터의 밀집도가 선형 탐사법에 비해 낮아진다.(겹치면 멀리 가니까) 따라서 이 때에 충돌 발생 가능성이 적다.
    * 다만 배열의 크기가 커져서 캐시 성능이 낮아진다.
* 이중 해싱 (Double hashing)
    * 해시 충돌이 발생하면 다시 한번 해싱을 돌린다. (기존과는 다른 해싱함수)
        * 이 때에 (처음 키, 최초해시 위치) 도 포함해서 돌린다.
            * 다시 찾아갈 때에는 최초 위치서부터 찾아가고 -> 찾았을 때에 그 key가 원하는게 맞는지 -> 아니면 다시 두번째 해싱함수를 돌려서 찾아가기 ... 이렇게 찾는것이다.
    * 이거는 충돌 가능성은 아주 낮다.
    * 근데 문제는 성능이 무지개같다.
        * 해싱함수를 또 돌려야함 -> 연산이 많음
        * 대체 어디에 저장될지 알수없고 array의 크기가 엄청 커질수 있다.

### 분리 연결법(separate chaining)

이거는 간단히 말해서 해시 슬롯에 여러 값이 들어갈 수 있도록 하는 것이다.
(슬롯에는 linkedList나 tree를 사용한다. -> java에서는 둘 다 사용)

말하자면 해시 충돌이 일어나면 따로 다른곳에 보관하는게 아니라 둘다 연결해서 쫘르륵 넣는것이다.
장점은 충돌이 일어났을 때에 굳이 index를 바꾸지 않아도 되고 데이터를 걍 넣으면 된다는 것이다.
그러나 같은곳에 여러 값이 들어가면(충돌이 많이 발생해서 슬롯이 커지는 경우) 성능이 떨어지게 된다.

### 간략 정리

* 개방 주소법
    * 해시 발생시 다른곳에 보내는 방법
    * 선형 탐사법 : 하나씩 다음으로 빈공간 찾아가기
    * 제곱 탐사법 : 제곱으로 다음 빈공간 찾아가기
    * 이중 해싱 : 다른 해싱 한번더 돌리기
* 분리 연결법
    * 해시 발생시 그냥 linkedList나 tree에 추가하기

## java에서의 Hash collision 해결법

> 기본적으로 분리 연결법을 사용한다.

위에서 java는 기본적으로 분리 연결법을 사용하고, `linkedList`와 `tree`를 모두 사용한다 했었다.

[이거](https://hello-backend.tistory.com/112)랑 [이거](https://hello-backend.tistory.com/249)에서 조회 성능을 한번 확인해 보면 데이터가 적을 때에는 linkedList의 성능이 더 좋지만, 데이터가 많아지면 Tree의 성능이 좋은 것을 확인할 수 있을 것이다.

그래서 java에서는 데이터가 적을 때에는 linkedList, 데이터가 많을 때에는 tree를 사용하고 있다.


이전 글처럼 한번 hashmap의 put부터 살펴보자

![](https://hackmd.io/_uploads/BkCPa1W6h.png)

```
/**
 * Implements Map.put and related methods.
 *
 * @param hash hash for key
 * @param key the key
 * @param value the value to put
 * @param onlyIfAbsent if true, don't change existing value
 * @param evict if false, the table is in creation mode.
 * @return previous value, or null if none
 */
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

이게 data를 put하는 과정이다.

여기서 해싱 충돌을 처리하는 부분을 한번 빼서 보면

```
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
```
            
이거다
이게 위에서 설명하는 것인데, 일단 저거 코드는 else 부분이고, 위에서 p가 null인 경우가 아니었으니까 -> 해시 충돌이 발생했을 때의 알고리즘이 이거다.

* 처음 If에서는 p랑 hash된 친구를 비교해서 같은 키인지 확인해 본다(즉 충돌이 아니라 원래 원하던 값인지 판단한다.)
    * 같은 키라면 그냥 그걸 쓰면 된다.
* 다음 else if에서는 이 p가 TreeNode인지 확인한다.
    * 트리노드라면 그 트리노드의 putTreeVal이라는 함수를 호출시켜서 트리에 노드를 추가시킨다.
    * 참고로 이 putTreeVal의 경우는 putVal의 Tree 버전이라 생각하면 된다.
    * 그리고 이거 방식이 [레드블랙트리](https://hello-backend.tistory.com/248)라고 한다.
* 마지막 else에서는 이거 linkedList일 것이다.
    * 노드에서 for문으로 같은 키인지 확인하기 위해 끝까지 확인한다.
        * 여기서 찾은 경우에는 break되고 해당 값을 갱신하게 될 것이다.
    * 그리고 여기서 `TREEIFY_THRESHOLD-1` 보다 현재값이 큰 경우는 얘를 트리구조로 변경한다
        * 굳이 -1를 해주는 이유는 최초 노드가 추가될 때에 이미 1개의 노드가 존재하여 이를 반영하기 위함이다.
            * 그게 저 주석 의미기도 함ㅇㅇ
* 근데 원하는 값이 아니라면, 이거는 새로 추가하는 값일 것이다.
    * 이 때에는 linkedList에 값을 추가해주면 된다.

대충 이런 흐름이라고 이해하면 된다.
근데 이제... 그래서 저거 linkedList랑 treeNode 둘을 어떻게 구분하고 바꾸는데?? 라는 의문이 들 수 있다.

![](https://hackmd.io/_uploads/Bkx33Bf6n.png)

요거 내용을 한번 보면

* TREEIFY_THRESHOLD
    * 리스트 대신 트리를 사용하기 위한 버킷 개수 임계치
* UNTREEIFY_THRESHOLD
    * 분할된 버킷을 트리구조에서 untreeifying 하는 데에 필요한 임계치

라고 해석할 수 있다.
그러니까 8이 넘으면 트리가 되고, 6까지는 linkedList라는 것이다.

근데 7은 어디갔냐? 할 수 있는데, 후술하겠다.

```
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
```

putVal 맨 밑의 이거를 보면 눈치를 챘겠지만, 이게 size가 커지면서 발생하는 일 같을 것이다(위에서 값이 변경되면 break가 없었거든.. 그래서 ++size해서 가는거지)
그러면 저 resize() 함수의 동작을 살펴봐야한다.

```
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
```

이게 resize 함수의 내부 구현이다.

여기서 나머지는 뭐 비었는지를 확인하고, 노드를 추가하거나 하는 부분이다.
다만 여기서

```
else if (e instanceof TreeNode)
    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
```

해당 코드가 트리일 때에 나누는 부분이다.
이제 split()을 찾아가보면


```
        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            TreeNode<K,V> b = this;
            // Relink into lo and hi lists, preserving order
            TreeNode<K,V> loHead = null, loTail = null;
            TreeNode<K,V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                next = (TreeNode<K,V>)e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                }
                else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                    tab[index] = loHead;
                    if (hiHead != null) // (else is already treeified)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }
```


요렇게 나온다.
이거는 보면

* UNTREEIFY_THRESHOLD보다 lc(노드의 개수)가 작거나 같으면 untreeify한다.
* 그렇지 않으면 treeify한다.

로 된다.
그러니까 즉 지금 hashmap의 내용이 이 숫자보다 크냐 작냐에 따라 트리로 갈지 아닐지를 결정하는 것이다.

참고로 저 resize() 함수는 처음에도 한번 호출되는데, 그래서 그 때에 바로 linkedList 형태로 만들어짐을 알 수 있을 것이다.

자 근데 아까 putVal 부분에서 linkedList를 Tree로 만들 때에 `TREEIFY_THRESHOLD` 에 도달할 때에 tree로 바꿨을 것이다. -> -1이 있어서 7로 보일 수 있지만(내가 그랬음) 그거는 그냥 처음 노드를 반영하기 위함이다. 즉 8에 도달했을 때 바뀌는 것이다.

그리고 지금은 UNTREEIFY_THRESHOLD보다 크거나 같은 때에 linkedList로 바꾸고 있다.

그러니까

1. 처음에 linkedList -> tree 일 때에는 8일때 변경
2. 그 다음부터는 그냥 tree이다가 6이 될 때에 tree -> linkedList로 변경
3. 7일때는 현상 유지

이다.
보면 알겠지만 저거 둘이 서로 바꾸는 로직이 또 귀찮다.
그래서 java에서는 뭐 7의 경우는 딱히 둘이 성능차이도 없고, 계속 
6,7,6,7,6,7,6,7
7,8,7,8,7,8,7,8
뭐 이런식으로 바뀔 때에 구조를 바꾸는거를 방지하기 위해서 저런 식으로 하나의 버퍼를 두고 해준 것이다.

## 정리

* java는 해시 충돌 때에 **분리 연결법**을 사용함
* 데이터가 적을 때에는 linkedList로 보관
* 데이터가 많을 때에는 tree로 보관
* 저 데이터의 많고 적고 기준은 6, 8로 중간에 7은 그냥 현상유지

로 한다고 생각하면 된다.

이게 이전에 공부할 때에는 그냥 충돌 있으면 이거 함수가 동작을 하는구만, 그리고 그게 처리를 해줘서 보관이 가능하는군 했었는데 좋은 기회가 있어서 살펴보게 되었다. 쏘 그레잇
