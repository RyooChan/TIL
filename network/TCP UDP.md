# TCP/UDP

## Transport Layer

> Endpoint간 신뢰성있는 데이터 전송을 담당하는 계층이다.

말하자면 클라이언트 - 서버 간 원하는 포트 번호에 해당하는 프로세스에 데이터를 순차적으로, 안정적으로 전달하는 것을 담당한다.

만약 이 전송 계층이 없다면 어떤 일이 발생할까?

* 데이터 신뢰 불가능
    * 송신자가 요청한 데이터와 수신한 데이터가 서로 다를 수 있다. (1,2,3 요청 -> 2,3,1 수신)
* 흐름 문제
    * 송/수신자 간 데이터 처리 속도의 차이 때문에 수신자가 처리할 수 있는 데이터량을 초과하게 되는 문제가 발생할 수 있다.
    * 만약 수신자가 처리할 수 있는 데이터를 초과해서 계속 요청이 온다면 문제가 발생할 것이다.
* 혼잡 문제
    * 네트워크가 혼잡하거나 뭔가 문제가 있을 때에 송신자가 보냈는데 제대로 데이터가 들어가지 않는 등의 문제가 발생 가능하다.

이러면 패킷이 없어지거나 데이터가 손실되는 등의 문제가 있을 수 있을 것이다.

그래서 이를 해결하기 위해 도입된 것이 바로 TCP(Transmission Control Protocol)이다.

## TCP(Transmission Control Protocol)

* 신뢰성있는 데이터 통신을 가능하게 해주는 프로토콜
* Connection연결 (3 way-handshake) - 양방향 통신
* 데이터의 순차 전송을 보장
* Flow Control(흐름 제어)
* Congestion Control(혼잡 제어)
* Error Detection(오류 감지)

### Segment - TCP프로토콜의 PDU

> IP 프로토콜의 패킷처럼 프로토콜 내에서 데이터가 처리되고 움직일 때에 그 데이터의 단위

![](https://i.imgur.com/5tUnCRA.png)

Application에서 TCP로 Data를 전송하게 되면, TCP는 이를 다시 잘라 준다.
이후에 해당 Data에 TCP Header를 붙여주게 되는데, 이 각각의 친구들을 Segment라고 한다.

이를 가지고 프로토콜 내에서 작업을 처리한다.

#### TCP Header

TCP에서 Data를 잘라서 TCP Header를 붙여준다고 했는데 그래서 이 TCP Header가 무엇일까??

![](https://i.imgur.com/izNzc8n.png)

전송이라는 것이 원하는 포트 번호로 데이터를 전송하게 되는데, 여기서 보면 발신지 - 목적지 포트 주소와 Sequence number Acknowledgement number 등이 존재한다.

그리고 Flag Field에서 9개의 CWR, ECE, URG ... 이런 애들이 있는데 여기서 TCP연결 제어 및 데이터 관리를 진행해 준다.

이 중 특히 `ACK` `SYN` `FIN` 3가지에 대해 설명하자면

* SYN
    * TCP가 커넥션을 연결할 때 사용하는 Flag bit
* FIN
    * 커넥션을 끝낼 때 사용하는 Flag bit
* ACK
    * 수신자가 다시 전송할 때에 사용하는 Flag bit -> 잘 알았다고 보내는 친구

### TCP 3-way handshake

#### 기본 통신 방식

1. Client에서 Server에 연결 신청을 할 때에 `SYN` 비트를 1로 설정해 패킷 송신
2. 서버에서 잘 받았다고 알려주기 위해 서버 측에서 `SYN`, `ACK`을 1로 설정해 패킷 송신
3. `ACK`비트를 1로 설정해 패킷 송신

#### 통신 실패

1. Client에서 Server에 연결 신청을 할 때에 `SYN` 비트를 1로 설정해 패킷 송신
2. 서버에서 잘 받았다고 알려주기 위해 서버 측에서 `SYN`, `ACK`을 1로 설정해 패킷 송신
3. 만약에 여기서 신호가 유실되어 클라이언트 측에서 `ACK`을 받지 못하는 경우 다시 packet을 통해 데이터 요청

#### 통신 close(4 way-handshake)

1. 데이터를 전부 송신한 Client가 FIN송신
2. Server가 ACK송신
3. Server에서 남은 패킷 송신(클라이언트가 TIME_WAIT상태로 일정 시간 대기)
4. 패킷 송신이 끝나면 Server가 FIN송신
5. Client가 ACK송신

![](https://i.imgur.com/656bINL.png)

이런 식으로 TCK에서는 신뢰성 있는 통신을 구현해 내었다.

### TCP의 문제점

전송의 신뢰성을 보장하기는 하지만 여러 문제점이 있다.

**[여기](https://hello-backend.tistory.com/193)서** 해당 문제점에 대해 정리해 두었다.

* 매번 Connection을 연결해서 시간 손실 발생(3 way-handshake)
* 패킷을 조금만 손실해도 재전송 해야한다

### UDP(User Datagram Protocol)

> TCP보다 신뢰성이 떨어지지만 전송 속도가 일반적으로 빠른 프로토콜

순차전송X, 흐름제어X, 혼잡제어X

* Connectionless(3 way-handshake X)
* Error Detection
    * checksum을 통해 error만을 검증해준다.
* 비교적 데이터의 신뢰성이 필요하지 않을 때 사용(ex. 영상 스트리밍)

### User Datagram - UDP프로토콜의 PDU

![](https://i.imgur.com/I1GA0z7.png)

TCP가 segment를 가지고 작업한다면, UDP는 USer Datagram을 가지고 동작한다.
application단에서 Data가 들어오게 되면 그 Data를 UDP Header를 추가해서 사용한다.

여기서 차이점은 TCP에서는 데이터를 쪼갰다면 UDP에서는 쪼개지 않고 그냥 보낸다.
-> 그래서 UDP를 사용해서 통신을 진행하면 Application에서 직접 쪼개야 할 것이다.

#### UDP Header

![](https://i.imgur.com/lysk1nd.png)

UDP Header은 TCP헤더만큼 복잡하지 않다.
그냥 에러 검출을 위한 `Checksum`을 집어넣어 주는 정도로 생각하면 된다.

### UDP의 데이터 전송 방식

![](https://i.imgur.com/twVoLEK.png)

서버에서는 받기 위해 열어두고, Sender에서는 보내고 싶을 때에 데이터를 그냥 보내주면 된다.
뭐 오류 검증 이런게 따로 없고 데이터가 안올수도 있다.

