# Stream(스트림)

> 다양한 데이터 소스를 표준화된 방법으로 다루기 위한 것

여기서 데이터 소스란, 컬렉션이나 배열처럼 여러가지 데이터들을 저장하고 있는 것을 의미한다.
그동안은 표준화된 방법을 위해 Collection Framework(List, Set, Map)을 사용해 왔는데 이 Collection Framework의 List, Set, Map은 서로의 사용법이 달랐었다. -> 즉, 사실 제대로된 표준화가 아니었다.

JDK1.8부터는 stream을 통해 진짜 저 Collection Framework를 제대로 통일시킬 수 있었다.

## Stream 장점

1. 컬렉션(List, Set, Map)과 배열을
2. Stream에 태운다.
3. Stream의 사용 방식대로 쓰면
4. 다른 데이터소스인데도 같은 메서드를 사용해서 같은 결과를 낼 수 있다!!

## Stream 구조

* 중간 연산
    * N번(여러번) 수행
* 최종 연산
    * 딱 1번만 수행(안할수도 있고)
    * 연산 결과가 스트림이 아니다.
    * 스트림의 요소를 소모하는 연산임

중간 연산을 통해 여러번의 연산을 진행하고, 이를 원하는 데이터 양식으로 변환한 다음에 마지막으로 최종 연산을 통해 원하는 결과를 얻을 수 있는 것이다.

![](https://i.imgur.com/JfMFkOq.png)

* distinct
    * 중복제거(제거 하고나도 stream형식은 유지된다.)
* limit
    * 5개만(자르고 나도 당연히 stream)
* sorted
    * 분류(이것도 뭐 당연히 stream)
* forEach
    * Stream을 하나씩 꺼냄
        * 여기서는 println으로 하나씩 꺼내서 출력하는 것이다.

## Stream 생성

### int형 arraylist를 Stream으로

```
List<Integer> list = Arrays.aslist(1,2,3,4,5);
Stream<Integer> intStream = list.stream();
```

### String 배열을 Stream으로

`Stream<String> strStream = Stream.of(new String[]{"a", "b", "c"});`

### 0, 2, 4, 6 ...

`Stream<Integer> evenStream = Stream.iterate(0, n->n+2);`

### 람다식

`Stream<Double> randomStream = Stream.generate(Math::random);`

### 난수 스트림

`IntStream intStrea = new Random().ints(5);`

## Stream 특징 (1)

* 스트림은 데이터 소스로부터 데이터를 읽기만 할 뿐 이를 변경하지는 않는다.
    * 그러니까 Stream을 사용해서 읽어온 데이터에 따로 로직을 적용해도 기존 데이터는 그대로임
* Stream은 Iterator처럼 일회용이다.
    * 쓴 다음에 또 쓸일이 있으면 Stream을 다시 생성해야 한다.
* 최종 연산 전까지 중간연산이 수행되지 않는다. - 지연된 연산


```
IntStream intStream = new Random().ints(1, 46);  // 1~45범위의 무한 스트림 -> 계속해서 난수를 생성한다.
intStream.distinct(0).limit(6).sorted()  // 중간연산
.forEach(i->System.out.println(i+","));  // 최종연산
```

지금 이 코드를 보면

1. 난수가 계속해서 생성됨
2. 그 난수들의 중복제거
3. 6개 뽑기
4. 정렬
5. 출력

인데, 계속 생겨나는 난수의 중복제거? 이게 될리가 없다.
근데 Stream에서는 이거를 지연된 연산으로 처리하여 가능하게 해준다.

## Stream 특징 (2)

* 스트림은 작업을 내부 반복으로 처리한다.
    * 반복문을 그냥 forEach같은걸로 쓰는거임

## Stream 특징 (3)

* 스트림 작업을 병렬로 처리 - 병렬스트림 지원
    * 즉, 멀티쓰레드로 처리한다는 것이다.
        * [이 내용](https://hello-backend.tistory.com/228)을 참조하자

```
Stream<String> strStream = Stream.of("dd", "aaa", "CC", "cc", "b");
int sum = strStream.parallel()    // 병렬 스트림으로 전환(속성만 변경)
            .mapToInt(s -> s.length()).sum();    // 모든 문자열의 길이의 합
```

* 기본형 스트림 - IntStream, LongStream, DoubleStream
    * Stream\<Integer> 이런거 안해도 된다.
        * 이런 방식으로 하면 오토박싱&언박싱을 하게 된다.
            * 기본타입 -> wrapper : 오토박싱
            * wrapper -> 기본타입 : 언박싱
                * 오토박싱/언박싱에서의 비효율을 제거해준다.
    * 기본형 스트림에서만 가능하다.
    * 기본형 스트림은 메서드도 좀 다양하게 지원한다.

