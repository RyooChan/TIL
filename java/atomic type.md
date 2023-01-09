# java의 Atomic Type(AtomicInteger, AtomicBoolean)

멀티 쓰레드 환경에서는 주로 동시성 문제 해결을 위해 다양한 방안이 도입된다.

그중 유명한 것들이 [synchronized](https://hello-backend.tistory.com/110), [volatile](https://hello-backend.tistory.com/203), 그리고 Atomic이다.

## Atomic??

### CAS

일단 Atomic Type은 CAS(Compare And Swap)알고리즘을 사용한다.
이게 뭐냐면...

> 변수의 값을 변경하기 전에 기존에 가지고 있던 값이 예상하던 것과 같은 경우에만 새로운 값으로 할당하는 방법

예를 들자면

```
public class AtomicExample {
    int val;
    
    public boolean compareAndSwap(int oldVal, int newVal) {
        if(val == oldVal) {
            val = newVal;
            return true;
        } else {
            return false;
        }
    }
}
```

이런 느낌으로, 기존 값이 변경을 하려는 값과 같을 때에만 변경하는 것인데, [여기서](https://hello-backend.tistory.com/213) 낙관적 락을 생각하면 좀 이해가 편할 듯 싶다.

이런 식으로, 변경에 대해서 원자성을 띄고 있기 때문에 얘는 Atomic이라고 하는 것이다.

또한, Atomic Type은 따로 synchronized를 걸어주지는 않는 방법이다.
따라서 Non-blocking 방식이라 할 수 있다.

### 그래서 이게 장점이 뭔데??

비관적락 vs 낙관적락을 생각하면 편하지 않을까 한다.

synchronized는 하나의 Thread에서 선점하고 있으면 아예 다른 Thread에서의 접근이 차단된다.
하지만 Atomic class는 보다시피 값이 변경되어 있지만 않으면 다수의 쓰레드에서 데이터에 접근해서 변경해도 문제가 없다.

그렇기 때문에 synchronized에 비해 성능이 좋다는 장점이 있다.

### AtomicBoolean

그냥 boolean을 갖고있는 Atomic 클래스라고 생각하면 된다.
초기값은 false.

`AtomicBoolean atomicBoolean = new AtomicBoolean()`

요런 식으로 사용할수 있다.

명령어들은

* get()
    * 값을 읽어오는 메서드
* set()
    * 값을 변경하는 메서드
* getAndSet()
    * 값을 읽은 후 -> 값을 변경하는 메서드
* compareAndSet()
    * 예상한 값과 동일하면 변경
    * 다르면 변경 안하고 false return

### AtomicInteger

이거 외에도 이것저것 있다...
이거는 당연히 Integer를 갖고있는 Atomic class이다.
그리고 명령어가 진짜 엄청 많은데

https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicInteger.html

요기서 확인해보쟈
