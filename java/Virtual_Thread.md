https://www.youtube.com/watch?v=BZMZIM-n4C0
요거 보고 정리해 봤다.

## Virtual Thread

- 안정성과 처리량에 대한 고민(전사 게이트웨이) -> 높은 트래픽에의 높은 처리량!!
    - Kotlin의 coroutine
    - java project loom 에 도입된 virtual thread
- 일단 virtual thread는 2018년에 project loom 으로 시작된 경량 스레드 모델이다.
    - 그리고 23년 JDK21에 정식 feature로 추가됨.
- 장점
    - 스레드 생성 및 스케줄링 비용이 기존 스레드보다 저렴
    - 스레드 스케줄링을 통해 nonbloking I/O 지원
    - 기존 스레드를 상속해서 사용 가능

### 스레드 생성 및 스케줄링 비용 저렴

- 기존 자바 스레드는 생성 비용이 크다.
    - 자바에서 미리 여러 요청 처리를 위한 스레드 풀을 생성
    - 사용 메모리 크기가 크다.
    - OS에 의해 스케줄링된다.(스레드 생성이나 소멸 과정 등에서 OS와 통신해야 해서 시스템 콜 발생)
        - 시스템 콜이란 자바의 터널 영역 호출을 위해 사용하는 인터페이스
- virtual thread는 생성 비용이 작다.
    - 스레드 풀 개념이 없다.(요청이 들어올 때 마다 새로 만들고 해제하는 방식)
    - 사용 메모리 크기가 작다.
    - 일반 스레드는 수메가를 쓴다면 얘는 수십킬로바이트 정도
    - OS가 아니라 JVM내에서 스케줄링된다.
- 많은 스레드를 생성하는 경우 기존보다 저렴하게 사용 가능

### nonblocking I/O 지원

- 요즘은 복잡한 애플리케이션에서 I/O blocking time이 증가하고 있다.
- Thread per request 모델에서 blocking time은 특히 병목이 된다.
- non blokcing I/O는 blocking time을 획기적으로 줄여준다.
    - Spring WebFlux 는 Netty의 Event loop를 활용해서 blocking I/O의 시간을 줄여왔다.
- virtual thread도 nonblocking I/O를 지원하는데, 이는 webflux의 방식과는 많이 다르다.
    - JVM 스레드 스케줄링 활용
    - Continuation 활용
- 하나의 API에서 오랜 시간이 소요되는 경우 그보다 적은 스레드를 활용해서 API를 여러번 호출하면 속도가 훨씬 빠르다.

### 기존 스레드 상속

- `VirtualThread`는 `BaseVirtualThread`를 상속하고, 걔는 다시 `Thread`를 상속한다.
    - 이게 뭔뜻이냐면 virtualThread는 Thread를 사용하는 모든 곳에서 호환이 된다는 것이다.
- `ExecitorServicee`의 경우도 `newVirtualThreadPerTaskExecutor`를 통해 치환 가능
- 이거는 즉 일반 자바랑 호환돼서 reactive coroutine 이런거 쓸필요 없이 고대로 사용할 수 있다.

## Virtual Thread 동작 원리

- 기존의 Thread
    - 플랫폼 스레드라고 한다.
    - OS에 의해 스케줄링
        - OS에 있는 커널 스레드와 1:1 매핑된다.
    - 작업 단위 Runnable
    - 특징
        - 커널 영역(OS)
        - 유저 영역(JVM / java code)
        - 유저 영역에서 커널 영역을 사용하기 위해서는 JNI(Java Native Interface)를 사용해야 한다.
            - 요 두개는 1:1로 매핑된다.
            - 즉 플랫폼스레드(유저영역에 객체) 가 JNI를 통해 커널 영역에 커널 스레드를 생성 요청하게 되는것.
                - 자바 코드를 보면 Thread start에서 `synchronized`로 커널 스레드 생성요청 진행
- Virtual Thread
    - 가상 스레드라고 한다.
    - JVM에 의해 스케줄링
    - JVM에 존재하는 캐리어 스레드와 1:N 매핑된다.
    - 작업 단위 Continuation
    - 특징
        - virtual thread 생성시 유저 영역에 객체가 생성된다.
            - JVM내에서 스케줄러가 얘를 관리해줄 것이다.
                - 처음에 따로 스케줄러 지정하지 않으면 `ForkJoinPool` 사용하는 `DEFAULT_SCHEDULER` 사용 ([요거](https://hello-backend.tistory.com/228) 참고)
                    - 그리고 얘는 static이라 모든 virtual thread가 공유한다.
                    - 프로세서 수의 Carrier Thread를 워커스레드로 사용한다.
                    - Work Stealing 방식으로 작업을 수행한다.
                        - 워커 스레드가 큐를 갖고 있고 task를 순차대로 처리하면서 자기 큐가 비어있으면 남의 큐를 훔쳐오는 방식!
            - 그래서 왜 이렇게 JVM에서 처리할까?
                - 일반 스레드는 생성/스케줄링 과정에서 커널 영역 접근하는데, 이게 비용이 발생한다.
                - 하지만 virtual thread는 JVM에서 스케줄링까지 완료되어 커널 영역 접근 비용이 발생하지 않는다.
                    - system call 발생하지 않음.
        - Continuation 작업단위?
            - 먼저 코루틴의 경우도 Continuation을 사용한다.
                - 이거는 동기처럼 한번에 호출 ~ 끝 해서 리턴이 아니라 호출하면 어느정도 실행하다가 중단하고 다시 제어권이 동작하고 또 나중에 코루틴을 호출해서 하는 이런식
            - Continuation은 말하자면 실행가능한 작업흐름이고, 중단이 가능하며 중단 지점으로부터 재실행이 가능한 것이다.
                - ![image](https://github.com/RyooChan/TIL/assets/53744363/25908027-36a9-4db3-a14f-b2917d59dcf1)
                - 되게 보기 좋은 테스트이다.
            - 그러면 virtual thread에서 이거를 어떻게 사용할까?
                    - 위에서 ForkJoinPool 사용해서 WorkQueue에 작업 큐를 넣는다고 했는데, virtual thread를 넣는다.
                    - 저기서 어느정도 실행하다가 중단하는 시점이 언젤까? -> park가 있다면 중단하게 되는데 `LockSupport.park()` 를 사용해서 block 해준다.
                        - 기존에는 커널스레드를 블락킹되도록 동작했었는데 JDK21에서 virtualthread의 park를 호출하도록 변경했다.
                            - 이 블라킹 시점에서 어떤 거냐에 따라 실제 스레드 블락이 아닌 작업의 중단(yield) 을 호출하도록 하는것.
                                - 즉 block되어도 실제 스레드는 중단되지 않고 다른 작업을 처리한다.
                                    - 시스템 콜도 없다. -> 컨텍스트 스위칭 비용이 낮음.

## 기존 스레드 모델 서버와 비교

- 기존마다 platform thread를 생성해서 커널 스레드랑 1:1 매칭
- virtual thread는 요청마다 무한대로 만들고, 사용 시에 carrier thread랑 매칭, 그리고 이 carrier thread가 커널 스레드랑 매칭된다.

## 그래서 어떨때 성능이 좋냐?

### vs 일반 thread

- ![image](https://github.com/RyooChan/TIL/assets/53744363/c123d9fe-9fa7-476b-8c63-e84f93821b1e)
    - I/O bound의 경우 더 높은 처리량
        - 논블라킹이니까 당연
    - CPU bound의 경우 더 낮은 처리량
        - 결국 근데 이거는 platform thread에서 동작해야 하는데 virtual thread를 굳이 만들어야 해서 낭비다.
    - 그리고 Thread서버는 특정 vuser수부터 장애 발생
        - 일반 thread가 최대 처리량이 낮다.

### vs 비동기

- ![image](https://github.com/RyooChan/TIL/assets/53744363/523e3914-3e4a-417d-b29b-4651826a042d)
    - 이거는 근데 극한의 상황이라 생각하면 된다. (장비 사양에 따른 context switching의 병목 때문)
    - virtual thread는 커널 스레드를 직접 블락하지 않아 그쪽 병목이 적은데 WebFlux의 경우 컨텍스트 블락킹에 따른 스위칭 비용으로 처리량이 저하된 것.

### 결론

- I/O bound 작업에 효율이 좋다.
- 제한된 사양에서 최대 처리량을 가진다(사양이 낮으면 매우 효율적)

## 주의사항

- Blocking carrier thread(Pin)
    - 캐리어 스레드를 block하면 virtual Thread 활용 불가
        - synchronized
        - parrallelStream
        - 이런거는 캐리어 스레드로부터 고정돼버림
        - 참고로 MySQL은 synchronized가 굉장히 많다...
        - 사용 라이브러리 release 점검하고, 변경 가능하면 java.util의 `ReentrantLock`을 사용하도록 변경
    - No Pooling
        - 풀링을 하면 안좋다.
        - 왜냐면 virtual thread는 생성비용이 저렴하고 사용할때마다 생성 - 사용완료 후 제거 하기 때문 
    - CPU bound
        - 어차피 virtual thread 써도 carrier thread 위에서 동작
        - nonblocking의 장점을 활용하지 못한다.
    - 경량 스레드
        - 가볍게 유지해야 한다(무거운 객체를 집어넣지 말자)
        - 매번 생성하고 매번 파괴하기 때문에 thread local에 메모리가 늘어날거니까
        - virtual thread는 수백만개의 스레드 생성 컨셉이니까.
        - JDK21 preview ScopedValue 라는게 이거를 개선하기 위해 도입됐다고 한다..
    - 배압
        - Virtual Thread는 배압조절 기능이 없다.
        - 하드웨어적인 문제가 있을 수 있으니 충분한 성능테스트가 필요하다.
        - 사용자도 배압에 대한 조절이 필요...(이거 생각보다 문제가 좀 있던것 같은데)

## 결론

- 빠르고 가볍고 nonblocking인 경량 스레드
- JVM 스케줄링 + Continuation
- Thread per request 사용중이고, I/O blocking time이 주된 병목인 경우 고려하자.

