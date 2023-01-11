# java에서 병렬성

일단 이 병렬처리는, 엄청나게 많은 데이터를 처리할 때에 하나로 다 하는것보다 나눠서 처리시켜서 **작업 처리 시간을 줄이는 것**에 목적이 있다.

참고로 이거 데이터 나누고 / 병렬 쓰레드 만들고 / 다시 합치고 등등...에서 시간을 잡아먹어서 안하느니만 못한 경우도 있다. 
그래서 요소가 많을때 주로 한다고 한다.

## 동시성, 병렬성

일단 동시성이랑 병렬성이 뭔지 좀 알고 넘어가자.

* 동시성
    * 하나의 코어에서 여러 쓰레드가 작업
    * 동시에 진행되는것이라기 보다는 그냥 빠르게 돌아가면서 실행하는것
* 병렬성
    * 여러 코어에서 작업을 진행
    * 실제로 여러개의 작업이 병렬적으로 실행되는것

![](https://i.imgur.com/vJ28CFy.png)

이런 느낌이다.

## 포크조인 프레임워크

자바 병렬 스트림은 요소들을 병렬 처리하기 위해 포크조인 프레임워크(JVM내부에 있는 프레임워크이다)를 사용한다.

* 포크 단계
    * 전체 요소를 서브 요소셋으로 분할
* 조인 단계
    * 서브 결과를 결합하여 최종 결과 완성

![](https://i.imgur.com/YMhdJpC.png)

요런거다.
그니까 나눠서 병렬처리하고 - 취합 하는게 포크조인 방식이라는것이다.

## 병렬 스트림 사용법

병렬 스트림을 사용하는 메소드는 두개가 있는데

* parallelStream()
    * List / Set 사용
    * Stream이 return된다.
    * 즉 Stream이 아닌거를 Stream으로 만들 때에 병렬스트림으로 만들어 주겠다~
* parallel()
    * 사용
        * java.util.Stream
        * java.util.IntStream
        * java.util.LongStream
        * java.util.DoubleStream
    * return
        * Stream
        * IntSream
        * LongStream
        * DoubleStream
    * 원래 Stream인 애들을 병렬스트림으로 만들어 주겠다~

## 주의사항

### 성능

이게 꼭 sequential보다 성능이 좋은거는 아니다.
언제가 좋을지는 3가지 요인을 통해 살펴볼수 있다.

* 요소의 수와 요소당 처리 시간
    * 컬렉션에 전체 요소의 수가 적고 요소당 처리 시간이 짧으면 sequential이 더 빠를 수 있음.
    * parallel은 추가적인 비용 소모 이유가 있기 때문이다.
        * 포크 및 조인(자르고 다시 취합)
        * 스레드 풀 생성(분할 처리를 위하여)
* 스트림 소스의 종류
    * ArrayList나 배열은 인덱스로 요소를 관리한다.
        * 포크 단계에서 요소 분리가 쉽다.
    * HashSet, TreeSet은 요소 분리가 어렵다.
        * https://hello-backend.tistory.com/210
        * 요소들을 저장할 때에 저장 위치가 hashing되어 띄엄띄엄
    * LinkedList의 경우도 요소 분리가 어렵다.
        * https://hello-backend.tistory.com/112
        * 자르는 위치까지 가려면 하나하나 찾아가야 해서이다.
* 코어의 수
    * CPU코어가 많으면 parallel 성능이 좋아짐
    * 코어가 적으면 sequential이 좋음!
