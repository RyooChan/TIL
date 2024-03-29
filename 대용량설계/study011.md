# study011
###### tags: `Tag(가상 면접 사례로 배우는 대규모 시스템 설계 기초)`

## 뉴스 피드 시스템 설계

### 1단계 문제 이해 및 설계 범위 확정

* 모바일 앱과 웹 모두를 지원한다.
* 중요한 기능으로는, 사용자는 뉴스 피드 페이지에 새로운 스토리를 올릴 수 있어야 하고, 친구들이 올리는 스토리를 볼 수도 있어야 한다.
* 뉴스 피드는 시간 흐름의 역순으로 표시된다.
* 한 명의 사용자는 최대 5000명의 친구를 갖는다.
* 트래픽 규모는 매달 천만명(10million DAU)
* 이미지나 비디오 등의 미디어 파일 포함 가능

### 2단계 개략적 설계안 제시 및 동의 구하기

1. 피드 발행
2. 뉴스 피드 생성

의 두 가지 부분으로 나뉘게 된다.

**피드 발행**에서는 포스팅한 스토리를 캐시와 DB에 기록한다. 새 포스팅은 친구의 뉴스 피드에도 전송된다.
**뉴스 피드 생성**에서는 지면 관계상 모든 친구의 포스팅을 시간 흐름 역순으로 모아서 만든다고 가정한다.

#### 피드 발행 API

새 스토리의 포스팅을 위한 API
HTTP POST형태로 요청을 보낸다.

`POST/v1/me/feed`

이런 형태를 띤다.

인자 : 
* body) 포스팅 내용에 해당
* Authorization 헤더) API 호출 인증을 위해 사용

#### 피드 읽기 API

뉴스 피드를 가져온다.

`GET/v1/me/feed`

이런 형태를 띤다.

인자 : 
* Authorization 헤더) API 호출 인증을 위해 사용

![](https://i.imgur.com/iuJkZsv.png)

* 피드 발행
    * 사용자가 포스트를 올리면 관련된 데이터가 캐시/데이터베이스에 기록되고, 해당 사용자의 친구 뉴스 피드에 뜨게 된다.

1. 사용자 단말에서 피드 발행을 보낸다.
2. 로드 밸런서를 통해서 트래픽 분산을 해주면서 특정 웹 서버에 서비스 요청한다.
3. 포스팅 저장 서비스에서는 DB에 포스트를 저장하고, 자주 사용되거나 무거운 정보를 캐시해준다.
4. 포스팅 전송 서비스를 통해 새 포스팅을 친구의 뉴스 피드에 푸시한다..
5. 알림 서비스를 통해 친구에게 보여준다.

---

이전 3장과 관련된 내용이라, 이 때 고민한 내용을 가져와보았다.

> 11장을 보면 친구의 뉴스 피드에 새 포스팅을 푸시한다고 하는데, 최대 친구 수 5000명에 대해서 포스팅이 올라갈 때마다 푸시하는게 맞나 싶다....
> 일단 이런 방식으로 하는 이유는 최신글부터 보여주는 식으로 피드가 진행되기 때문에 새 글이 올라올 때마다 캐시로 보내주면 알아서 최신부터 되니까? 일것같기는 하다.

> 스터디를 통해 의견을 나누어 보니 최신 글부터 보여주는 식이 아니여도 해당 방법을 사용하는 것이 낫다.
> 그 이유는 피드의 경우 모든 포스트의 데이터를 가지고 있는 것이 아니기 때문에 이미 이 자체가 데이터를 최적화 할 수 있기 때문.


---

![](https://i.imgur.com/mz16Rkh.png)

* 뉴스 피드 생성

1. 사용자 단말에서 피드를 보려고 접속한다.
2. 로드 밸런서를 통해서 트래픽 분산을 해주면서 특정 웹 서버에 서비스 요청한다.
3. 3-1쪽에서 친구 쪽에 캐시된 뉴스 피드 캐시들을 가져와서 최신 포스트부터 정렬해서 피드를 생성한다.

### 3단계 상세 설계

![](https://i.imgur.com/79s7Cmt.png)

웹 서버는 클라이언트와의 통신, 인증, 처리율 제한 등 기능을 수행한다.
Authorization헤더를 통해 API호출 사용자만 포스팅 가능하게, 또 포스팅 수 제한 등을 수행한다.


1. 그래프 데이터베이스에서 친구 ID목록 가져오기. 그래프DB는 친구 관계나 친구 추천 관리에 적합하다.
2. 사용자 정보 캐시에서 친구들의 정보 가져오기. 그리고 나를 차단했거나 피드를 받지 않기로 한 친구에게는 뉴스 피드가 보이지 않도록 해야 할 것이다.
3. 친구 목록과 새 스토리의 포스팅 ID를 메시지 큐에 넣는다.
4. 팬아웃 작업 서버가 메시지 큐에서 데이터를 꺼내어 뉴스 피드 데이터를 뉴스 피드 캐시에 넣는다.
뉴스 피드 서버는 <포스팅ID, 사용자 ID>의 순서쌍을 보장하는 매핑 테이블이라 볼 수 있다. 따라서 새로운 포스팅이 만들어질 때마다 레코드가 추가된다.
메모리 요구량을 줄이기 위해 필요한 정보들만 저장하고, 개시 크기를 제한한다.


> 그래프DB가 친구 관계나 추천 관리에 적합한 이유는
> 내가 알기로 RDB는 join이 발생할 때 마다 지수함수의 형태로 시간 복잡도가 늘어나고, GraphDB는 로그함수의 형태로 시간 복잡도가 늘어난다.
> 이는 GraphDB는 Data간의 관계를 직접 생성하고 이 관계를 그냥 횡단해서 필요한 데이터를 조회하기 때문이다.

---

#### 쓰기 시점에 팬아웃하게 되면(push모델)

##### 장점

* 뉴스 피드가 실시간으로 갱신되며 친구 목록에 있는 사용자에게 즉시 전송된다.
* 새 포스팅이 기록되는 순간에 뉴스 피드가 이미 갱신되므로 뉴스 피드를 읽는 데 드는 시간이 짧아진다.

##### 단점

* 친구가 많은 사용자의 경우 친구 목록을 가져오고 그 목록에 있는 사용자 모두의 뉴스 피드를 갱신하는 데에 많은 시간이 소요될 수 있다.(핫키)
* 유령회원의 피드까지 갱신해서 낭비

---

#### 읽기 시점에 팬아웃하게 되면(pull모델)

##### 장점

* 유령회원에 데이터를 보내는 낭비가 없어진다.
* 모든 데이터를 친구 각각에 보낼 필요가 없어서 핫키문제 해결

##### 단점

* 뉴스 피드를 읽는 데에 많은 시간이 소요될 수 있다.

---

여기서는 위의 두 방법을 결합한다.
1. 뉴스 피드를 빠르게 가져오는것은 중요하므로 대부분의 사용자에는 push 모델 사용
2. 친구나 팔로어가 많은경우 pull 모델 사용하여 과부하 방지
3. 안정 캐시를 통해 요청과 데이터를 고르게 분산하여 핫키 문제 줄이기

![](https://i.imgur.com/GHurLvp.png)
