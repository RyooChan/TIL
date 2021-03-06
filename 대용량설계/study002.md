# study002
###### tags: `Tag(가상 면접 사례로 배우는 대규모 시스템 설계 기초)`

시스템 용량이나 성능 요구사항을 개략적으로 추정하는 방법
이 개략적 규모 추정을 효과적으로 해내려면 규모 확장성을 표현하는 데 필요한 기본기에 능숙해야 한다.

## 2의 제곱수

데이터 양의 계산 결과를 얻으려면 볼륨의 단위를 2의 제곱수로 어떻게 표현하는지를 우선 알아야 한다.

![](https://i.imgur.com/b5D49VZ.png)

이게 데이터 볼륨 단위들이다.

## 응답지연 값

![](https://i.imgur.com/MqRSgx5.png)

2010년 공개된 통상적인 컴퓨터에서 연산들의 응답지연 값이다.

![](https://i.imgur.com/X1A1Q9P.png)

이 내용들을 분석하면 이런 결론이 나온다.

* 메모리는 빠르고, 디스크는 느리다.
    * 디스크 탐색은 가능한 한 피할것.
* 단순한 압축 알고리즘은 빠르다.
    * 인터넷으로 데이터를 보내기 전에 가능하면 압축하라.
* 데이터 센터는 보통 여러 지역에 분산되어 있고, 센터들 간에 데이터를 주고받는 데에는 시간이 걸린다.

## 가용성 관련 수치들

고가용성(high availability)은 시스템이 오랫동안 지속적으로 중단 없이 운영될 수 있는 능력을 지칭하는 용어이다.
고가용성을 표현하는 값은 퍼센트로 표현한다.
100%는 단 한번도 중단되지 않은 것이고, 보통 시스템들은 99~100% 사이의 값을 가진다.

SLA(Service Level Agreement)는 서비스 사업자가 보편적으로 사용하는 용어로, 서비스 사업자와 고객 사이에 맺어진 합의를 의미한다.
이 합의에는 서비스 사업자가 제공하는 서비스의 가용시간(uptime)이 공식적으로 기술되어 있다.

이 SLA는 숫자 9를 이용해 가용시간을 표시하고, 당연히 9가 많으면 많을수록 좋다.

![](https://i.imgur.com/NATPKhQ.png)

## 예제

* 가정
    * 월간 능동사용자(monthly active user)는 3억명이다.
    * 50%사용자가 트위터를 매일 사용한다.
    * 평균적으로 각 사용자는 매일 2건의 트윗을 올린다.
    * 미디어를 포함하는 트윗은 10% 정도이다.
    * 데이터는 5년간 보관된다.

이렇게 가종한다면 추정치는 다음과 같다.

* 추정
    * QPS(Query Per Second) 추정치
        * 일간 능동 사용자 = 3억 x 50% = 1.5억
        * QPS = 1.5억 x 2(트윗횟수) / 24시간 / 3600초 = 약3500
        * 최대 QPS(Peek QPS) = 2 x QPS = 약 7000

이를 통해 미디어 저장을 위한 저장소의 요구량을 구해본다.

* 저장소 요구량
    * 평균 트윗 크기
        * tweed_id에 64바이트
        * 텍스트에 140바이트
        * 미디어에 1MB
    * 미디어 저장소 요구량
        * 1.5억 x 2(최대QPS) x 10%(미디어 포함 트윗) x 1MB(미디어 1MB) = 30TB/일
    * 5년간의 미디어 저장을 위한 저장소 요구량
        * 30TB x 365 x 5 = 약 55PB

여기서 QPS, 최대QPS, 저장소 요구량, 캐시 요구량, 서버 수 등을 추정하는 방법을 잘 기억하면 좋을 것이다.
