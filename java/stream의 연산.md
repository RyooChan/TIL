# 스트림의 연산

> 스트림은 중간 연산과 최종 연산을 제공한다.

* 중간연산
    * 연산결과가 Stream
    * 여러 번 적용 가능
* 최종연산
    * 연산결과가 Stream이 아니다.
    * 단 한번만 적용 가능(Stream요소를 소모함)

## 중간 연산

### 스트림 자르기 - skip(), limit()

```
Stream<T> skip(long n)    // 앞에서부터 n개 건너뛰기 -> n+1 ~ 끝까지
Stream<T> limit(long maxSize)    // maxSize이후는 잘라냄 -> 0 ~ maxSize까지
```

예를 들어

```
IntStream intStream = IntStream.rangeClosed(1, 10);    // 1 2 3 4 5 6 7 8 9 10
intStream.skip(3).limit(5).forEach(System.out::print);    // 4 5 6 7 8
```

이런 식으로,

* skip(3)
    * 앞에서부터 3개 잘라냄
        * 4 5 6 7 8 9 10
* limit(5)
    * 5개 보내고 그뒤 버림
        * 4 5 6 7 8

### 스트림 요소 걸러내기 - filter(), distinct()

```
Stream<T> filter(Predicate<? super T> predicate)  // 조건에 맞지 않는 요소 제거
Stream<T> distinct()        // 중복제거
```

filter는 조건에 맞는 애들만 출력시키는 친구이다.
그리고 filter은 여러 번 쓸 수 있는데, 이는 나눠서 써도 되고 그냥 `&&`조건으로 써도 된다.

### 스트림 정렬하기 - sorted()

```
Stream<T> sorted()            // 스트림 요소의 기본 정렬(comparable)로 정렬
Stream<T> sorted(Comparator<? super T> comparator)        // 지정된 Comparator로 정렬
```

Comparator를 사용해서 정렬 기준을 정해줄 수 있고, 아예 안써서 그냥 기본 정렬을 쓸수도 있다.

![](https://i.imgur.com/6XedBGH.png)

#### Comparator의 comparing 메서드에 관해서

```
comparing(Function<T, U> keyExtractor)
comparing(Function<T, U> keyExtractor, Comparator<U> keyComparator)
```

Function 함수형 인터페이스를 매개변수로 받는다.
그리고 추가로 Comparator를 지정해줄수 있다.

사용법은

```
studentStream.sorted(Comparator.comparing(Student::getBan))    // 반별로 정렬
            .forEach(System.out::println)
```

이런 식으로, 반별로 정렬해서 이를 출력하도록 할 수 있다.

> 혹시 추가로 정렬 기준이 더 필요하면?

```
thenComparing(Comparator<T> other)
thenComparing(Function<T, U> keyExtractor)
thenComparing(Function<T, Y> keyExtractor, Comparator<U> keyComp)
```

이런 식으로 해줄 수 있다.

예를 들면

```
studentStream.sorted(Comparator.comparing(Student::getBan)    // 반별로 정렬
             .thenComparing(Student::getTotalScore)           // 총점별로 정렬
             .thenComparing(Student::getName))                // 이름별로 정렬
             .forEach(System.out::println);
```

이런 식으로 반별 외에도 추가적인 정렬 기준을 부여할 수 있다.

### 스트림 요소 변환하기 - map()

```
Stream<R> map(Function<? super T, ? extends R> mapper)    // Stream<T> -> Stream(R)
```

Function은 T가 들어왔을때, 변환 타입을 R로 주는 것이다.
file -> String 등으로 변환이 가능하다.

### 스트림의 요소를 소비하지 않고 엿보기 - peek()

```
Stream<T> peek(Consumer<? super T> action)        // 중간 연산(스트림 소비 X)
void forEach(Consumer<? super T> action)          // 최종 연산(스트림 소비 O)
```

peek는 보통 중간연산 작업 사이사이에 잘 되고있나 확인하기 위해 사용한다.

### 스트림의 스트림을 스트림으로 변환 - flatMap()

```
Stream<String[]> strArrStrm = Stream.of(new String[]{"abc", "def", "ghi"},
                                        new String[]{"ABC", "GHI", "JKLMN"});
```

이런 식으로, ** Stream의 요소가 배열인 ** Stream이 있다.
-> 요소가 저 소문자배열, 대문자배열으로 되어있는 스트림인거임

```
Stream<Stream<String>> strStrStrm = strArrStrm.map(Arrays::stream);
```

이런 식으로 map을 이용해서 사용하면 **Stream을 요소로 받는 Stream**이 된다.
-> 요소가 소문자배열스트림, 대문자배열스트림으로 된 스트림인거다.

근데 이거는 Stream내부에 Stream들이 주루루루룩 들어가 있는 것이다.
이를 모두 뭉쳐서 하나의 Stream으로 만들려면 flatMap을 사용한다.

```
Stream<String> strStrStrm = strArrStrm.flatMap(Arrays::stream);    // Arrays.strea(T[])
```

이렇게 하면 보이곘지만 `Stream<String>`인데, 전체 스트림의 요소들 각각이 뭉쳐져서 하나의 스트림 내부의 요소들이 된 것이다.
보통은 여러개의 배열로 만들어진애를 하나의 Stream으로 만들고 싶을 것이기 때문에 이 메서드를 사용해준다.

