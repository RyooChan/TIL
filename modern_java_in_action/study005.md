## 정리

스트림을 활용하는 방법에 관한 내용인데, 사실 이 부분은 어느 정도 알고 있기 때문에 정리하고자 한다.

> 이전에서 보았듯, 스트림은 내부 반복 뿐 아니라 코드를 병렬로 실행할지 여부를 알고 있다.
> 그래서 내부적으로 다양한 최적화가 가능하다.

### 필터링

* 프레디케이트 필터링 (filter)
    * [프레디케이트](https://hello-backend.tistory.com/261)를 사용해서 필터핑을 한다.
* 고유 요소 필터링 (distinct)
    * 이름에서 알 수 있듯, 중복된 값을 제거하는것
        * 객체의 [Equals, HashCode](https://hello-backend.tistory.com/211)를 통해 진행한다.

### 슬라이싱

> 요소의 선택이나 스킵

* 프레디케이트 활용 슬라이싱
    * Takewhile
        * predicate결과값 이외는 모두 버리는 연산
        * 즉, 결과값이 TRUE일 때까지는 값을 가져오지만 false가 되는 순간 스트림이 중지되고 반환
        * filter와의 차이점은 이건 목표를 이룬 후에 중단된다는것
        * 그렇기 때문에 정렬된 값에서 쓰면 효율적이다.
    * Dropwhile
        * Takewhile과 반대
        * 즉, 처음으로 False가 나온 순간 더이상의 작업을 중단하고 그 false시점 이후 모든 값을 반환
        * 참고로 takewhile과 마찬가지로 얘도 무한스트림에서도 사용 가능하다.
* 스트림 축소
    * limit
        * 프레디케이트와 일치하는 요소를 Limit만큼 선택하고 바로 결과 반환
        * 정렬되지 않은 상태에서도 사용 가능하기는 하다.
* 건너뛰기
    * skip
        * 그만큼 건너뛰고 진행
        * 참고로 앞에서 filter같은게 있다면 필터링 이후 skip이 진행된다.(즉 4개중 2개가 필터링되면 뒤의 2개에 대해 skip 진행)

### 매핑

* 스트림 각 요소에 함수 적용
    * 함수를 인수로 받는 map메서드 지원
    * 함수를 적용한 결과가 새로운 요소로 변환된다
        * 기존 함수를 써도 되고 내부에서 return해줘도 되고
    * 참고로 map은 여러개 합쳐서 쓸수도 있다.
* 스트림 평면화
    * flatmap
        * 생성된 스트림을 하나의 스트림으로 평면화
            * 예를 들어 split처럼 여러 Stream을 반환하는 Map을 썼을 때에 그 결과들을 하나로 매핑시켜주는 것이다.

### 검색과 매칭

* 매치
    * anyMatch
        * 적어도 하나랑 매칭되는지
    * allMatch
        * 모든 요소랑 매칭되는지
    * noneMatch
        * 아무 요소랑도 매칭되지 않는지
* 검색
    * findAny
        * 현재 스트림에서 임의의 요소 반환
        * 말 그대로 아무거나 반환(1개 or 0개) -> optional & 쇼트서킷
    * findFirst
        * 첫 번째 요소 반환
    * 저 두개(any, first)의 차이는 Stream이 병렬성을 지원하기 때문에 존재하는데, 아무래도 반드시 맨 처음값을 first는 병렬성에서 좀 찾기 힘들 것이다. 그래서 findAny 존재
* 참고로 얘들은 모두 매치용은 스트림에서 [쇼트서킷](https://hello-backend.tistory.com/268)이 되어 원하는 결과가 나왔다면 바로 반환이 가능하다.

### 리듀싱

* 요소의 합(reduce)
    * 초기값이 있는 경우
        * 초기값, BinaryOperator\<T> 두 개의 인자를 갖는다.
        * 초기값에서 BinaryOperator의 결과를 조합한 값을 반환한다.(참고로 첫 파라미터는 초기값이 될것이다.)
    * 초기값이 없는 경우
        * 이 경우는 결과값이 Optional이 되는데, 당연하겠지만 이 reduce연산이 수행되지 않으면 초기값이 없어서 Null이 반환되어서다.

### 스트림 만들기

* Stream.of
    * 값을 여기에 넣어서 스트림을 만들 수 있다.
* Stream.empty
    * java9부터 null이 될 수 있는 개체를 스트림으로 만들 수 있다.
* Array.stream
    * 배열 -> 스트림

### 함수로 무한 스트림 만들기

* Stream.iterate
    * 초기값, 람다를 인수로 받아 무한히 생성되는 값을 만들 수 있다.
    * 두 번째 인수로 프레디케이트를 받아 언제까지 작업을 수행할지 판단한다.
        * Limit이나 takeWhile 등등 소트서킷 지원 활용
* Stream.generate
    * Supplier\<T>를 인수로 받아 무한히 생성되는 값을 만들 수 있다.
