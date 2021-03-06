# study008
###### tags: `Tag(가상 면접 사례로 배우는 대규모 시스템 설계 기초)`

## URL 단축기 설계

### 1단계 문제 이해 및 설계 범위 확정

질문 결과 기본적 기능이 아래와 같다고 한다.

1. URL단축 : 주어진 긴 URL을 훨씬 짧게 줄인다.
2. URL리디렉션 : 축약된 URL로 HTTP 요청이 오면 원래 URL로 안내
3. 높은 가용성과 규모 확장성, 그리고 장애 감내가 요구됨

#### 개략적 추정

* 쓰기 연산 : 매일 1억 개의 단축 URL생성
* 초당 쓰기 연산 : 1억/24/3600=1160
* 읽기 연산 : 읽기 연산과 쓰기 연산 비율은 10:1이라고 하자. 그 경우 읽기 연산은 초당 11600회 발생한다.
* URL단축 서비스를 10년간 운영한다고 가정하면 1억X365X10=3650억 개의 레코드를 보관해야 한다.
* 축약 전 URL의 평균 길이는 100이라고 하자
* 따라서 10년동안 필요한 저장 용량은 3650억X100바이트=36.5TB이다.

### 단계 개략적 설계안 제시 및 동의 구하기

API엔드포인트, URL리디렉션, 그리고 URL단축 플로에 대해 살펴본다.

#### API엔드포인트

클라이언트는 서버가 제공하는 API엔드포인트를 통해 서버와 통신한다.
우리는 이를 REST스타일로 설계할 것이다.
URL단축기는 기본적으로 두 개의 엔드포인트를 필요로 한다.

1. URL단축용 엔드포인트
새 단축 URL을 생성하고자 하는 클라이언트는 이 엔드포인트에 단축할 URL을 인자로 실어서 POST요청을 보내야 한다. 
이 엔드포인트는 다음과 같은 형태를 띤다.

`POST/api/v1/data/shorten`

* 인자 : {longUrl: longURLstring}
* 반환 : 단축 URL

2. URL 디리렉션용 엔드포인트
단축 URL에 대해서 HTTP요청이 오면 원래 URL로 보내주기 위한 용도의 엔드포인트.
다음과 같은 형태를 띤다.

`GET/api/v1/shortUrl`

* 반환 : HTTP 리디렉션 목적지가 될 원래 URL

#### URL 리디렉션

![](https://i.imgur.com/5136C0U.png)

브라우저에 단축 URL을 입력하면 무슨 일이 생기는지 보여준다.
단축 URL을 받은 서버는 그 URL을 원래 URL로 바꾸어서 301응답의 Location헤더에 넣어 반환한다.

![](https://i.imgur.com/3I8mF6Y.png)

1. 단축 URL을 방문한다.
2. 상태코드(301)과 본래 단축되지 않은 URL을 서버에서 return한다.
3. 본래의 URL에 방문한다.

301 코드는  Permanently Moved 으로 해당 URL이 영구적으로 이동했다는 뜻이다.
그리고 영구적으로 이동되었으므로 브라우저는 해당 응답을 캐시한다.
이후에 같은 URL로 요청을 보낼 필요가 있을 때 브라우저는 캐시된 원래 URL로 요청을 보내게 된다.
참고로 301코드를 쓰면 단축 URL로 접근해서 -> 원래 URL로 이동한다고 해도 트래픽은 원래 URL로 이동했다고 파악한다.

302코드는 Temporarily Moved 으로 임시 이동이며, 이 때는 단축 URL로 트래픽이 잡힐 것이다.
이 때는 따로 캐시되지 않고 언제나 단축 URL에 먼저 보내진 후에 원래 URL로 리디렉션 된다.
이 경우 트래픽은 단축 URL쪽에 잡힌다.

각각의 방법은 다른 장단점을 갖고 있다.

* 서버 부하를 줄이고 싶으면 -> 301코드
첫 번째 요청만 단축 URL로 가고 이후로는 바로 원래 URL로 가기 때문이다.
* 트래픽 분석이 중요하다 -> 302코드
클릭 발생률이나 발생 위치 추적에 유리하다.

URL리디렉션 구현의 가장 직관적인 방법은 해시 테이블을 이용하는 것이다.
해시 테이블에 <단축URL, 원래URL>의 쌍을 저장한다고 가정하면 URL리디렉션은 다음과 같이 구현된다.

* 원래 URL=hashTable.get(단축 URL)
* 301 또는 302 응답 Location 헤더에 원래 URL을 넣은 후 전송

#### URL단축

단축 URL이 
`www.tinyurl.com/{hashValue}`
와 같은 형태라고 해보자. 결국 중요한 것은 긴 URL을 이 해시 값으로 대응시킬 해시 함수 fx를 찾는 일이 될 것이다.

![](https://i.imgur.com/PLcE7dB.png)

이 해시 함수는 다음 요구사항을 만족해야 한다.

* 입력으로 주어지는 긴 URL이 다른 값이면 해시 값도 달라야 한다.
* 계산된 해시 값은 원래 입력으로 주어졌던 긴 URL로 복원될 수 있어야 한다.

이 해시 함수에 대한 상세 설계는 다음 절에서 살펴본다.

### 3단계 상세 설계

이번에는 데이터 모델, 해시 함수, URL단축 및 리디렉션에 관한 보다 구체적인 설계안을 만들어 본다.

#### 데이터 모델

개략설 설계할 때에는 모든 것을 해시 테이블에 두었다.
이는 실제 시스템에 쓰기 곤란한데, 메모리는 유한한 데다 비싸기 때문이다.
더 좋은 방법은 <단축URL, 원래URL>의 순서쌍을 RDB에 저장하는 것이다.

![](https://i.imgur.com/XML3IJM.png)

#### 해시 함수

해시 함수는 원래 URL을 단축 URL로 변환하는데 쓰인다.
편의상 해시 함수가 계산하는 단축 URL의 값을 hashValue라고 지칭한다.

hashValue는 [0-9, a-z, A-Z]의 문자들로 구성된다.
따라서 사용할 수 있는 문자의 개수는 10 + 26 + 26 = 62개다.
hashValue의 길이를 정하기 위해서는 62^n >= 3650억인 n의 최소값을 찾아야 한다.

![](https://i.imgur.com/k1u5kAZ.png)

hashValue의 길이와 해시 함수가 만들 수 있는 URL의 개수 사이의 관계에 관한 표이다.

n=7이며 3.5조개의 URL을 만들 수 있고, 이는 3650억개의 요구사항을 충분히 만족한다.
따라서 hashValue의 길이는 7로 한다.

해시 함수 구현에는 두 가지 방법이 사용되는데

* 해시 후 충돌 해소 방법
* base-62변환 방법

이 있다.

##### 해시 후 충돌 해소

긴 URL을 줄이려면 원래 URL을 7글자 문자열로 줄이는 해시 함수가 필요하다.
손쉬운 방법은 CRC32, MD5, SHA-1 같이 잘 알려진 해시 함수를 이용하는 것이다.

이를 사용해서 `https://en.wikipedia.org/wiki/Systems_design` 을 축약하면

![](https://i.imgur.com/dVrKXA6.png)

이렇게 나온다.

근데 표를 보면 CRC32가 계산한 가장 짧은 해시값조차도 7보다는 길다.
이걸 어떻게 줄일까??

첫 번째 방법은 계산된 해시 값에서 처음 7글자만 이용하는 것인데, 이렇게 하면 해시결과가 충돌할 확률이 높아진다.
충돌이 발생했을 때는 이 충돌이 해결될 때 까지 사전에 정한 문자열을 해시값에 덧붙인다.

![](https://i.imgur.com/Old3vmm.png)

이런 절차로 더해준다.

1. 처음에 긴 URL이 입력됨
2. 해시 함수로 짧은 URL구함
3. DB에 존재하지 않는 경우 충돌이 없다. 그냥 저장
4. DB에 존재하는 경우 충돌이 발생하므로...longURL뒤에 사전에 정한 문자열을 추가한다.

이렇게 하면 충돌은 해소할 수 있지만 단축 URL을 생성할 때 한 번 이상 DB질의를 해야 하므로 오버헤드가 크다.
여기서 DB대신 블룸 필터를 사용하면 성능을 높일 수 있다.
블룸 필터는 특정 원소가 있는지 검사할 수 있도록 하는, 확률론에 기초한 공간 효율이 좋은 기술이다.

#### base-62변환

진법 변환은 URL단축키를 구현할 때 흔히 사용되는 접근법 중 하나이다.
이 기법은 수의 표현 방식이 다른 두 시스템이 같은 수를 공유하여야 하는 경우에 유용하다.
62진법을 쓰는 이유는 hashValue에 사용할 수 있는 문자 개수가 62개이기 때문이다.

10진수 11157을 62진수로 변환하면 2TX가 된다.
그래서 단축 URL이 `https:/tinyurl.com/2TX` 가 된다.

---

이 두 방법들 차이에는 이런 차이가 있다.

![](https://i.imgur.com/SyO2dTp.png)


---

### URL단축키 상세 설계

URL단축기는 시스템의 핵심 컴포넌트이기 때문에 그 처리 흐름이 논리적으로 단순해야 하고 기능적으로 언제나 동작하는 상태로 유지되어야 한다.

62진법 변환 기법을 사용해 설계하고, 그 처리 흐름을 순서도 형태로 정리한다.

![](https://i.imgur.com/nj6IfXm.png)

1. 입력으로 긴 URL을 받는다.
2. DB에 해당 URL이 있는지 검사한다.
3. DB에 있다면 해당URL에 대한 단축 URL을 만든 적이 있다는 것이다. 따라서 DB에서 해당 단축 URL을 가져와서 클라이언트에 반환한다.
4. 없는 경우 해당 URL은 새로 접수된 것으로 유일한 ID를 생성한다. 이 ID는 DB의 PK로 사용된다.
5. 62진법 변환을 적용, ID를 단축 URL로 만든다.
6. UD, 단축 URL, 원래 URL로 새 DB record를 만든 후 단축 URL을 클라이언트에 전달한다.

예제를 통해 보자면

![](https://i.imgur.com/3YmPg2m.png)

이런 느낌이다.

ID생성기의 주된 용도는 단축 URL을 만들 때 사용할 ID를 만드는 것이고, 이 ID는 전역적 유일성이 보장되어야 한다.
고도로 분산된 환경에서 이런 생성기를 만드는건 어렵다. 이전 스터디 내용을 확인하자~

#### URL리디렉션 상세 설계

여기 URL리디렉션 메커니즘의 상세한 설계가 있다.
쓰기보다 읽기를 더 자주 하는 시스템이라, <단축URL, 원래URL>의 쌍을 캐시에 저장하여 성능을 높였다.

![](https://i.imgur.com/lE303Re.png)

로드밸런서의 동작 흐름을 요약하면

1. 사용자가 단축 URL을 클릭한다.
2. 로드밸런서가 해당 클릭으로 발생한 요청을 웹 서버에 전달한다.
3. 단축 URL이 이미 캐시에 있는 경우 원래 URL을 바로 꺼내서 클라이언트측에 전달한다.
4. 캐시에 해당 단축 URL이 없는 경우 DB에서 꺼낸다. DB에 없다면 아마 사용자가 잘못된 단축 URL을 입력했을 것이다.
5. DB에서 꺼낸 URL을 캐시에 넣은 후 사용자에게 반환한다.

### 4단계 마무리

여기까지 한 후에 시간이 남으면 더 고려해 볼 사항은

* 처리율 제한 장치
이 시스템은 엄청난 양의 URL단축 요청이 밀려들 경우 무력화될 수 있다는 잠재적 보안 결함을 갖고 있다.
처리율 제한 장치를 두면 IP주소를 비롯한 필터링 규칙들을 이용해 요청을 걸러낼 수 있을 것이다.
* 웹 서버의 규모 확장
이 웹 계층은 무상태 계층이므로, 웹 서버를 자유로이 증설 혹은 삭제할 수 있다.
* DB규모 확장
DB를 다중화하거나 샤딩하여 규모 확장성의 달성이 가능하다.
* 데이터 분석 솔루션
성공적인 비즈니스를 위해서는 데이터가 중요하다.
URL단축기에데이터 분석 솔루션을 통합해 두면 어떤 링크를 얼마나 많은 사용자가 클릭했는지, 언제 주로 클릭했는지 등 중요한 정보를 알아낼 수 있을 것이다.
* 가용성, 데이터일관성, 안정성
대규모 시스템이 성공적으로 운영되기 위해서는 반드시 갖추어야 할 속성들이다.
