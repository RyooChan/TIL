# iterable과 iterator

뭔가 Collection Framework쪽을 보면 자주 보이는 두개이다. 뭐가 다르길래 굳이 따로 쓸까??

## Iterable interface

```
public interface Collection<E> extends Iterable<E> {
}
```

자바 내부 코드를 보면 Collection이 이렇게 Iterable 인터페이스를 상속받고 있다.

그러면 이제 Iterable의 내부를 살펴보면

```
public interface Iterable<T> {

    Iterator<T> iterator();

    default void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (T t : this) {
            action.accept(t);
        }
    }
}
```

내부를 살펴보면 다음과 같이 되어있다.
근데 보면 iterable이 iterator를 가져와서 쓴다... 뭐지?

## iterator interface

```
public interface Iterator<E> {

    boolean hasNext();

    E next();
    
    default void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
```

이렇게 되어있다.
이거를 보면

* hasNext()
    * 추가 데이터의 존재 유무를 확인하는 메서드
* next()
    * 다음 요소로 넘어가서 그 값을 return
* remove()
    * next()를 통해 가져온 요소 제거
    * next()호출 이후에는 remove()를 필수적으로 호출해야 한다.

그러니까 즉

> iterator 인터페이스를 사용해서 데이터를 순차적으로 가져올 수 있다.

그래서 collection인터페이스는 iterable을 extends하여, 하위의 모든 클래스에 iterator()를 구현할 수 있도록 하는 것이다.
이를 통해 모든 자료구조(list, set, queue...)에서 표준화된 데이터 read방식을 제공할 수 있다.

-> java8부터는 defailt method가 추가되어 Iterable인터페이스에 forEach()메서드가 추가되었다.

## ListIterator interface

참고로 iterator interface의 하위 인터페이스 중에는 `ListIterator`라는 친구가 존재한다.

```
public interface ListIterator<E> extends Iterator<E> {

    boolean hasNext();

    E next();

    boolean hasPrevious();

    E previous();

    int nextIndex();

    int previousIndex();

    void remove();

    void set(E e);

    void add(E e);
}
```

내부를 살펴보면 요래 돼있다.
대충 보아도 얘는 뭔가 전으로도 이동할 수 있을것 같다.

* ListIterator는 양방향 이동이 가능하다.
    * 근데 얘는 ArrayList, LinkedList같이 List를 이용한 인터페이스에서만 사용 가능하다.

## 참고로

이걸 보면 Map이 왜 Collection의 하위가 아닌지 알 수 있다...
Map은 k-v형태여서 iterable의 for-each loop에 포함되지 않기 때문이다.
