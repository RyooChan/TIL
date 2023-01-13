# Stream 만들기

## collection을 Stream으로 만들기

> Collection 인터페이스의 stream()으로 컬렉션을 스트림으로 변환

`Stream<E> stream() // Collection인터페이스의 메서드`


List, Set을 변환할때 이를 사용한다.
예를 들어

```
List<Integer> list = Arrays.asList(1,2,3,4,5);
Stream<Integer> intStream = list.stream();  // list를 스트림으로 변환

// 스트림의 모든 요소를 출력
intStream.forEach(System.out::print);    //12345
```

요런 식으로, List를 stream()메소드를 사용하여 Stream으로 바꿀 수 있다.
그리고 메서드 참조를 사용하여 stream의 내용을 다 출력시키면 된다.

참고로 저거 `forEach`를 한번 사용하여 다 출력(최종연산)했으므로 더이상 사용 불가능이다.

## 배열을 Stream으로 만들기

> 객체 배열로부터 스트림 생성하기

```
Stream<T> Stream.of(T... values)    // 가변 인자
Stream<T> Stream.of(T[])
Stream<T> Arrays.stream(T[])
Stream<T> Arrays.stream(T[] array, int startInclusive, int endExclusive) // startInclusive ~ endExclusive 사이의 범위를 Stream으로 만든다.
```

> 기본형 배열로부터 스트림 생성하기

```
IntStream IntStream.of(int... values)        // Stream이 아니라 IntStream
IntStream IntStream.of(int[])
IntStream Arrays.stream(int[])
IntStream Arrays.stream(int[] aray, int startInclusive, int endExclusive)
```

### 임의의 수(난수)를 Stream으로 만들기

> 난수를 요소로 갖는 스트림 생성

```
IntStreamintStream = new Random().ints();      // 무한 스트림
intStream.limit(5).forEach(System.out::println);    // 5개의 요소만 출력

IntStream intStream = new Random().ints(5);        // 크기가 5인 난수스트림 반환
```

여기서 저 Random클래스 내에서 각각의 메서드는

```
Integer.MIN_VALUE <= ints() <= Integer.MAX_VALUE
Long.MIN_VALUE <= longs() <= Long.MAX_VALUE
0.0 <= doubles() < 1.0
```

요렇게 되어있다.
이들은 무한 스트림이다.
여기서 ints()는 Integer의 최소~최대의 값이 랜덤으로 나오는 것이다.

그리고 이들은 무한 스트림이기 때문에, 처음부터 크기를 지정해 주거나(`ints(5)` 이런식) -> 유한 스트림 
혹은 limit등의 방법으로 잘라 주어야 한다.

### 특정 범위의 정수를 Stream으로 만들기

> 특정 범위의 정수를 요소로 갖는 스트림 생성(IntStream, LongStream)

```
IntStream IntStream.range(int begin, int end) // end가 미포함
IntStream IntStream.rangeClosed(int begin, int end)    // end가 포함
```

### 람다식을 Stream으로 만들기

> iterate(), generate()
> 얘들은 기본적으로 무한 스트림이다.

* 람다식을 소스로 하는 스트림 생성하기

```
static <T> Stream<T> iterate(T seed, UnarayOperator<T> f)    // 이전 요소에 종속적
static <T> Stream<T> generate(Supplier<T> s)                 // 이전 요소에 독립적
```

저기 `UnaryOperator<T>`이나 `Supplier<T>` 는 람다식이다.
그래서 저 람다식을 이용해서 Stream을 만들어낸다.

* iterate()는 이전 요소를 seed로 해서 다음 요소를 계산한다.

`Stream<Integer> evenStream = Stream.iterate(0, n->n+2);    // 0, 2, 4, 6...`

여기서 보면 seed값인 `0`이 없으면 n이 어디서부터 시작하는지 모른다.
그렇기 때문에 seed값을 `0`으로 설정해 주어야 한다.
또, 이는 무한스트림이기 때문에 유한으로 바꾸어 주어야 한다.
-> 보면 seed에 종속되어 있다.

* generate()는 seed를 사용하지 않는다.

`Stream<Double> randomStream = Stream.generate(Math::random);        // 랜덤한 값을 계속 생성하는 무한 스트림`
`Stream<Integer> oneStream = Stream.generate(()->1);         // 계속 1이 나오는 무한 스트림`

이거는 이전 결과가 필요없기도 하고 쓰지도 않는다.
-> 즉 이거는 종속되지 않고 독립적이다.

## 파일에서 Stream생성, 빈 스트림 생성

> file을 소스로 하는 스트림 생성

`Stream<Path> Files.list(Path dir)        // Path는 파일 또는 디렉터리`

```
Stream<String> Files.lines(Path path)    // 파일내용을 line단위로 읽어서 그거를 string단위 Stream으로 만든다.
Stream<String> Files.lines(Path path, Charset cs)
Stream<String> lines()        // BufferedReader 클래스의 메서드
```

그래서 lines() 메서드를 쓰면 file내용을 한줄씩 Stream의 요소로 만들어 준다.
보통 log파일 분석이나 다량의 텍스트파일 분석에 쓰인다.

> 비어있는 스트림 생성하기

```
Stream emptyStream = Stream.empty();
long count = emptyStream.count();        // 0이 나온다. 빈거니까.
```
