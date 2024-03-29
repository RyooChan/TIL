## 1계층

> 물리 계층(Physical Layer)

먼저 두 대의 컴퓨터가 어떻게 통신하는지부터 생각해 보면
모든 파일과 프로그램은 0과 1의 나열이고, 결국 0과 1만 주고받을 수 있으면 통신이 가능하다는 것이다.

### 두 대의 컴퓨터 간 통신

![](https://i.imgur.com/v16Ga61.png)

두 대의 컴퓨터를 전선 하나로 연결한다 생각하면

* 1을 보낼 때는 +5V
* 0을 보낼 때는 -5V

이렇게 흘려보내면 0과 1의 전송이 가능해진다.
그래서 이제 0과 1을 주고받을수 있으므로 두 컴퓨터는 모든 데이터의 통신이 가능하다.

그런데 이게 실제로는 잘 동작하지 않았다.

![](https://i.imgur.com/l8jy7YX.png)

이렇게 X축은 시간, Y축은 전압을 갖는 Sin함수가 있다고 생각해 보았을 때

![](https://i.imgur.com/0KOjMtR.png)

그래서 이렇게 +5, -5의 전압을 갖는 애들을 주고받을 텐데...

![](https://i.imgur.com/mZ7TLzJ.png)

1초동안 몇번의 주파수가 있는지 세어보면 총 4번 진동할 것이다. (4Hz)

![](https://i.imgur.com/jhHLcEX.png)

근데 이거는 주파수 값이 하나로 고정되지 않고 계속 변경될 것이다.
그래서 이게 최소1, 최대 10의 전압을 갖고 있다고 가정하였을 때

![](https://i.imgur.com/wNcYrKE.png)

이렇게 5~8Hz의 전자기파만 통과시킬 수 있는 전선이 있으면 위의 저 1~10Hz전자기파를 흘려보내면 1~4, 9~10 구간은 잘 안갔을 것이다.
그러면 둘의 신호가 서로 달라질 것이다.

앞에서는 두대의 컴퓨터간 0/1의 통신으로 데이터를 주고받을 수 있다고 했는데 

![](https://i.imgur.com/BIt7GC4.png)

요런 전자기파가 주어지면(수직선과 수평선이 있는 전자기파는 항상 0~무한대의 주파수 범위를 가짐) 전선은 얘를 통과시킬수 없을 것이다.(전선에서 통과시킬 수 있는 주파수의 한계가 있으므로)

그러면 이걸 어떻게 전송해야 할까??

![](https://i.imgur.com/AuiQGTv.png)

이런 식으로 아날로그 신호로 변경해서 전송해야 할 것이다.

### 그래서 Physical Layer가?

* 0과 1의 나열을 아날로그 신호로 변경해서 전선으로 흘려 보내고(encoding)
* 아날로그 신호가 들어오면 0과 1의 나열로 해석하여(decoding)
* 물리적으로 연결된 두 대의 컴퓨터가 0과 1을 나열을 주고받을 수 있게(통신)해주는 모듈
* 소프트웨어가 아니고, 하드웨어적으로 구현되어 있다.

## 2계층

> 데이터 링크 계층(Data-Link Layer)

### 여러 대의 컴퓨터 간 통신

먼저 여러 대의 컴퓨터 간 통신에 대해 알아본다.
위에서처럼 매번 연결이 필요할 때마다 전선을 막 연결하기는 곤란할 것이다.

![](https://i.imgur.com/6WEvc3Q.png)

요렇게

그래서 이제 전선 하나를 가지고 여러 컴퓨터와 통신할 방법을 찾아야 한다.

![](https://i.imgur.com/4Zr8FqB.png)

이렇게 하나의 선을 통해 여러 컴퓨터가 연결되어 있다면?

![](https://i.imgur.com/p7iMapH.png)

파랑이가 만든 신호를 전송하면 한꺼번에 모든 곳에 아날로그 신호를 전달할 수 있을 것이다.
근데 만약에 빨간색에만 데이터를 보내고 싶으면??

![](https://i.imgur.com/1aTcLps.png)

![](https://i.imgur.com/0BB0dW0.png)

이렇게 뭔가 기능을 가진 상자(더미 허브)를 만들어서, 얘가 어디로 보내고싶은지 알아서 잘 필터링하도록 만들어주면 빨간색에만 신호를 보낼 수 있을 것이다.

그리고 이 상자를 `스위치`라고 한다.
이 `스위치`는 일종의 컴퓨터로서 이러한 작업을 수행해 준다.

### 여러 대의 컴퓨터 통신 v2

이렇게 두 개의 네트워크가 구성되어 있다고 하자(인트라넷이라고 함)

![](https://i.imgur.com/JYUmyAs.png)

근데 이러면 파랑 -> 빨강 으로의 전선 연결이 되어있지 않다.
그래서 서로 통신이 불가능해졌다.

![](https://i.imgur.com/eyG95g9.png)

그럼 이렇게 두 개의 스위치를 연결한다면?
ㅆㄱㄴ

이렇게 두 개의 스위치를 연결하여, 서로 다른 네트워크 간의 통신이 가능하게 해주는 친구를 `라우터`라고 한다.
-> 근데 사실 저기서 보이는 저 보라색 선은 `스위치+라우터`라서 `L3스위치`이긴 하다.

그래서 이런 모든 네트워크들을 쭈루루루룩 연결해 준 친구를 `인터넷`이라고 한다.

### 그래서 Data-Link Layer가

위에서 배운 1계층에서 사용된 기술만으로는 여러 컴퓨터 간 통신을 가능하게 하지 못했다.
그래서 스위치를 통해 여러 컴퓨터간 통신이 가능해졌는데

![](https://i.imgur.com/iZN3mWs.png)

이렇게 파랑 노랑 초록 -> 빨강
으로 데이터를 송신하면 빨강에서는 일단 이것들을 다 이어서 받는다.
근데 실제로 데이터를 읽으려면 얘들을 잘 나눠야 할 것이다.

이를 해결하기 위해 송신자는 데이터의 앞뒤에 특정 비트열을 붙인다.
예를 들어 앞에 `1100`, 뒤에 `0011`을 붙인다고 해보자.

![](https://i.imgur.com/7LoZku9.png)

이거를 이제 `11000011` <- 이게 데이터가 만들어질때 끝+앞이니까
을 구분자로 갖고 나누면

![](https://i.imgur.com/RQ5vrGS.png)

이렇게 원하는 데이터들을 잘 구분할 수 있는걸 확인 가능하다.

따라서 Data-Link Layer는

* 같은 네트워크에 있는 여러 대의 컴퓨터들이 데이터를 주고받기 위해 필요한 모듈
* Framing은 Data-link Layer에 속하는 작업 중 하나이다.
    * Framing은 저 `11000011` 으로 데이터를 감싸는 것이다.
* Lan Card에 하드웨어적으로 구현되어 있다.

## 3계층

> 네트워크 계층(Network Layer)

![](https://i.imgur.com/Bylacd3.png)

이런 식으로 인터넷이 구현되어 있을 떄 A -> B로 데이터를 보내고 싶다고 한다면

A에서 데이터 앞에 목적지 주소 즉 B의 주소(IP주소, 각 컴퓨터마다 고유하다)를 붙인다.

![](https://i.imgur.com/yK5egMK.png)

여기서 일단 `55.10.54.75 + Data` 를 패킷이라고 하자.

이 패킷은

1. 라우터 `가`로 해당 패킷 전송
2. 라우터 `가`가 해당 패킷을 열어본다.
3. `가`와 연결된 컴퓨터 중에서는 이 주소를 갖는 목적지가 없다.
4. 그래서 상위의 `마`에 다시 패킷을 포장해서 보낸다.
5. `마`에서는 이제 다시 패킷을 까서 확인해 본다.
6. `마`는 자신과 연결된 것들 중 해당 목적지가 있다는 것을 알고 있다. ([여기](https://hello-backend.tistory.com/176)서 다양한 통신 방법 참조)
7. 그래서 해당 목적지로 패킷을 전송한다.

그니까 가->마->바->라 로 패킷을 전달하게 될 것이다.

### 그래서 Network Layer가

* 수많은 네트워크의 연결 중 목적지 컴퓨터로 데이터를 전송하기 위해
    * IP주소를 이용해서 길을 찾고(routing)
    * 자신 다음의 라우터에게 데이터를 넘겨주는 것(forwarding)
* 얘는 운영체제의 커널에 소프트웨어적으로 구현되어 있다.

## 4계층

> 전송 계층(Transport Layer)

앞선 3계층까지의 결과로 전 세계의 모든 인터넷 상의 컴퓨터가 서로 통신할 수 있게 되었다.
그래서 수신자는 전 세계 컴퓨터로부터 데이터를 받을 수 있을 것이다.

근데 이 컴퓨터에는 지금 여러 프로그램들이 실행되는 중(프로세스)이라 생각해 보자..

컴퓨터는 자기가 받은 데이터들을 원하는 프로세스에 전송해야 할 것이다.
그를 위해 이 각각의 프로세스들은 포트 번호를 가져야 한다.
포트 번호란, **하나의 컴퓨터에서 동시에 실행되고 있는 프로세스들이 서로 겹치지 않게 가져야 하는 정수 값**이다.

예를 들어 A, B 두개의 프로세스가 동작중이면 얘들은 서로 겹치지 않게 8080, 8081의 포트값을 가져야 할 것이다.

-> A:8080 B:8081

그러면 이제 데이터 송신자는 데이터를 보낼 때에 데이터를 받는 수신자 컴퓨터에 있는 프로세스의 포트 번호를 붙여서 보낸다.

-> 8081 'Hello'

근데 그럼 데이터 전송자는 미리 이 포트번호를 알고 있어야 할 것이다!!
예를 들어 `www.naver.com` 이거는 `www.naver.com:80` 이거다. 80이 생략됨.

### 그래서 Transport Layer가

* Port번호를 이용하여 도착지 컴퓨터의 최종 목적지인 `프로세스`에 데이터가 도달하게 해주는 모듈이다.
* 얘도 커널에 소프트웨어적으로 구현되어 있다.

## 7계층

> 어플리케이션 레이어(Application Layer)

갑자기 7계층으로 와버림

### OSI7계층 vs TCP/IP모델

![](https://i.imgur.com/vgfnmb3.png)

OSI 7 Layer는 7개의 Layer로 이루어져 있다.
근데 사실 현대 인터넷은 OSI7Layer말고 TCP/IP모델을 따르고 있다.
그 이유는 이 OSI7Layer가 TCP/IP에게 시장 점유에서 졌기 때문이다.

![](https://i.imgur.com/lX7xtvQ.png)

이거 보면 5,6,7계층이 하나의 layer로 뭉뚱그려져 있다.
그리고 1,2계층도 그렇고 3계층도 뭉쳐져 있거나 좀 달랐는데 updated되면서 설계가 동일하게 바뀌었다.
그래서 이제 5,6,7계층만 다름.

### TCP/IP 소켓 프로그래밍(네트워크 프로그래밍)

이제 다시 Application Layer로 돌아와서

> 운영체제의 Transport layer에서 제공하는 API를 활용해서 통신 가능한 프로그램을 만드는 것.

이 소켓 프로그래밍 만으로도 클라이언트, 서버 프로그램을 따로따로 만들어서 동작시킬 수 있다.
뿐만 아니라 이를 통해 누구나 자신만의 Application Layer 인코더/디코더의 생성이 가능하다.

즉 아무나 자신만의 Application Layer 프로토콜의 생성이 가능하다는 것이다.

이 또한 다른친구들과 마찬가지로 인코더와 디코더를 갖는데, 여기서 http를 통해 잠깐 Application Layer를 알아본다.

1. 서버(전송측)에서 데이터를 보내준다 -> `뭐해?`
2. 서버 HTTP encoder에서 이를 `Status Cpde:500 / 뭐해?`로 바꿔준다.
3. 서버 4계층 encoder를 통해 목적지 `9000 + Status Code:500 / 뭐해?` 으로 데이터를 보낸다.
4. 서버 1~3계층을 통해서 아날로그 신호가 원하는 위치로 이동한다.
5. 이제 클라이언트(수신측)에서는 1~3계층을 통해 이를 다시 `9000 + Status Code:500 / 뭐해?`으로 바꿔준다.
6. 클라이언트 4계층 decoder를 통해 `Status Code:500 / 뭐해?`를 가져온다.
7. HTTP decoder를 통과해서 `뭐해?` 의 데이터만 꺼내올 수 있다.

### Layered Architecture

MVC패턴이랑 마찬가지로 소프트웨어 아키텍쳐중 하나이며, 네트워크 시스템은 이 Layered 아키텍쳐를 따른다.
그니까 네트워크 시스템은 하나의 커다란 소프트웨어라고 할 수 있고, OSI 7 layer모델은 거대한 네트워크 소프트웨어의 구조를 설명하는 것이다.
