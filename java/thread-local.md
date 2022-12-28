# ThreadLocal - 쓰레드로컬 이란?

이름 그대로... 쓰레드 단위 로컬 변수 할당임ㅇㅇ
사실 이 Thread local에 대해서는 잘 몰랐는데, 놀랍게도 java1.2버전부터 제공되고있던 유서깊은 클래스였다고 한다.

## 그게 뭔데

일단 보통 java에서는 멀티 쓰레드를 이용해 한번에 작업을 처리해서 성능상의 이점을 얻을 수 있다.
근데 이 멀티 쓰레드 간에 리소스를 어떻게 공유해야 할지, 그리고 그 리소스의 동시성 문제를 어떻게 해결할 수 있을지 고민해야 한다.

그 방안 중 하나가 바로 Thread local이다.

## 그거 어떻게 하는건데.

일단 ThreadLocal의 내부는 Map구조를 갖고 있는데, 
key - thread정보, value - 변수
요렇게 저장한다.

그래서 각각의 쓰레드는 자신의 변수에 접근하게 된다.

한번 코드로 살펴보자.

## 여러 쓰레드에서 Thread local을 사용해 보자.

[우리](https://hello-backend.tistory.com/213) [모두](https://hello-backend.tistory.com/214) 알다시피, 여러 쓰레드에서 동시에 접근하면 [쓰레드 문제](https://hello-backend.tistory.com/110)가 발생할 수 있다.

예를 들어서 말하자면 **여러 쓰레드가 동시에 동작할 때에 이 ThreadLocal값을 변경하면, 내가 무슨 값을 쓰는지 알수가 없을 것이다.**

근데 얘는 그 문제가 없다.

![](https://i.imgur.com/vPISITU.jpg)


함보자

### 로직

1. 여러개의 쓰레드를 for문을 통해 생성해서 동작시킴
2. 각각의 쓰레드는 하나의 ThreadLocal을 공유해서 쓴다.
3. 쓰레드 로컬에서 는
```
N번 쓰레드 시작!!
ThreadLocal정보 보여줌

N번 쓰레드 에서 ThreadLocal값 세팅해줌(각 쓰레드의 숫자 N에 따라 변수가 바뀜)
N번 쓰레드 끝!!
THreadLocal에 저장한 정보 보여줌
```
4. 이런 식으로 동작한다.


### 코드

```
public class RyooChanService implements Runnable {

    private static final ThreadLocal<String> ryooChanIs = ThreadLocal.withInitial(() -> "천재");

    private static final String[] ryooChanInfo = {"잘생겼어", "친절해", "착해", "멋져", "완벽해"};

    @Override
    public void run() {
        System.out.println();
        System.out.println(Thread.currentThread().getName() + "번 쓰레드 시작!!" + " \n " + "류찬...그는 " + ryooChanIs.get());
        System.out.println();
        ryooChanIs.set(ryooChanInfo[Integer.parseInt(Thread.currentThread().getName())]);
        System.out.println(Thread.currentThread().getName() + "번 쓰레드 끝!!" + " \n " + "류찬...그는 " + ryooChanIs.get());
    }
}
```

이제 이런 식으로 쓰레드에서 사용되도록 세팅해주자.
참고로 default는 "천재"이다. 아무것도 세팅이 안되면 "천재"라는 값이 출력될것이다.

참고로 저 의미없는 sout은 뭐지? 할수 있는데, 저거를 해줘야지 좀 시작-끝 이게 뒤죽박죽 나온다...

```
    public void 류찬은() {
        RyooChanService ryooChanService = new RyooChanService();

        for(int i=0; i<5; i++){
            Thread ryoochan = new Thread(ryooChanService, "" + i);
            ryoochan.start();
        }
    }
```

이제 이렇게 테스트를 해주자.

![](https://i.imgur.com/Fiw0EL7.png)

흠... 한번 보면

1. 0번 쓰레드 시작됨 -> thread local default(천재)라고 알려줌
2. 2번 쓰레드 시작됨 -> thread local default(천재)라고 알려줌
3. 2번 쓰레드 끄으읕 -> thread local "착해" 로 세팅됨
4. 1번 쓰레드 시작됨 -> thread local default(천재)라고 알려줌
5. 1번 쓰레드 끄으읕 -> thread local "친절해"로 세팅됨
6. 4번 쓰레드 시작됨 -> thread local default(천재)라고 알려줌
7. 4번 쓰레드 끄으읕 -> thread local "완벽해"로 세팅됨
8. 3번 쓰레드 시작됨 -> thread local default(천재)라고 알려줌
9. 3번 쓰레드 끄으읕 -> thread local "멋져"로 세팅됨
10. 0번 쓰레드 끝!! -> thread local "잘생겼어"로 세팅됨

이렇게 나왔다.
일단 보면 뭔가 그동안 생각하던 동시성이랑 좀 다르다.

왜냐? 다른 쓰레드가 분명 시작되기 전에 값을 바꾸었는데도 불구하고 사용해보면 default값이 출력되었기 때문이다.

뭐야!! 

![](https://i.imgur.com/9bzpXa4.jpg)

![](https://i.imgur.com/E5OkISo.jpg)

## 뭐냐ㅋㅋ

사실 위에서 설명했듯이 thread-local은
key-value로 되어있다.

그리고 key는 해당 thread의 정보이다.
그래서 각각의 thread는 자신에게 할당된 정보에만 접근이 가능하다는 것이다...

## 그러면 다른 thread랑 변수를 공유하려면 어떡하는건데??

[얘](https://hello-backend.tistory.com/213)랑 [얘](https://hello-backend.tistory.com/214)랑 [얘](https://hello-backend.tistory.com/110)를 한번 보자.
이거는 동기화를 써서 다른 방안을 모색하는게 현명하다.

애초에 thread-local은 활용법 자체가 이것들과는 좀 다른 느낌이다.
나는 **thread가 똑같은 작업을 해서 효율성을 높일때, 각각의 쓰레드가 완전 독립적으로 리소스를 공유하여 사용할 수 있도록 해준것이 thread-local**인 것이라 생각한다.

## 주의사항

**thread-local은 반드시 사용한 이후에 해제해 주어야 한다!!!!!**

1. 만약 A라는 유저가 `1번 쓰레드`가 thread-local상에서 데이터를 할당해 사용한 후 작업 마침
2. 뭔가 주루루룰 하다가 쓰레드 풀에서 다시 쓰레드 생성
3. 근데 B라는 유저가 `1번 쓰레드`를 쓰레드 풀에서 할당받아 씀
4. B라는 유저인데 A유저의 정보를 thread-local에서 받을수 있음....!!

그니까 작업이 끝나면 반 드 시 `remove()` 메서드를 통해 해당 thread-local의 변수를 제거해 주자.

