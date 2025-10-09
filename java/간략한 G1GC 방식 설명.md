# 간략한 G1GC 방식 설명

여기서 '1' 은 one 이 아니라 first 라고 생각하면 된다.
즉, garbage 를 먼저 처리하는 방식이다.

## 개념

[이전의 serial, parallel GC](https://hello-backend.tistory.com/413) 에서는 Young 영역과 old 영역을 물리적으로 나누어 두었는데, 이거는 다른 식으로 생각한다.

- 전체 Heap 을 여러 개의 'Region' 으로 나눈다. (보통은 heap 영역의 크기에 맞춰서 2048개의 region 을 만들도록 각 region 의 크기를 결정한다.)
- 각각의 region 은 동적으로 Eden, Survivor, Old 역할을 부여받는다.
    - 보통 때에는 Free 상태로 있다가 필요할 때 할당된다.
        - 참고로 GC가 끝나면 다시 Free 상태로 돌아감
    - Region 의 크기의 50% 를 초과하는 객체는 'Humongous' 라는 Region 에 별도로 할당된다.
        - 그 이유는 얘들은 Full GC 의 원인이 될 수 있기 때문. 후술
- G1GC는 목표 멈춤 시간이 존재한다.
    - 이 목표 멈춤 시간에 맞춰 최대의 영역(예를 들어 Eden 영역은 100개만 존재) 개수를 결정한다.
    - 참고로 목표 멈춤 시간이 있기 때문에 Young GC 발생할 때마다 소요 시간을 기록하며 Young Generation 의 크기를 맞춰나간다 (예를 들어 GC시간이 목표치보다 짧은 경우는 Young Generation 의 크기를 늘려서 처리율을 향상하는 식으로)

## 동작

Young-Only phase, Space-Reclamation phase 두 단계로 나뉘어 수행된다.

- Young-Only phase
    - 기본적으로 객체들은 Eden 역할을 하는 Region 에 할당된다.
    - 모든 Eden Region 이 꽉 차면 Young GC 발생
        - JVM 의 memory manager 이 객체 할당 시점에 파악하고 수행한다.
    - 요 부분은 Minor GC 와 유사하게 진행된다. (STW)
    - Young-only phase 내에서 young GC 는 여러 번 발생할 수 있다.
- Space-Reclamation phase
    - 이게 핵심이다. 전체 Heap 점유율이 특정 임계값을 넘으면 Young GC 와 함께 old 영역 공간을 회수하는 단계를 시작한다.
    - Concurrent Marking
        - Initial Mark (STW 발생)
            - Young GC 가 발생할 때 함께 진행된다.
            - GC roots 가 직접 참조하는 객체들만 빠르게 스캔하여 마킹
        - Root Region Scanning & Concurrent Marking (STW 없음)
            - 여기서 가장 오래 걸린다.
            - 위의 Initial Mark 에서 찾은 객체부터 전체 Heap 의 모든 객체를 찾아 마킹
        - Remark (STW 발생)
            - 위의 Concurrent Marking 과정 중에 애플리케이션이 변경한 참조들을 최종적으로 반영
            - 여기서 정확성을 위해 STW 발생하지만 위의 concurrent Marking 단계에서 대상 범위를 줄어 전체 시간이 오래 걸리지는 않는다.
        - Cleanup (일부 STW 발생)
            - 살아있는 객체가 하나도 없는 Old region 은 free 상태로 돌려보낸다.
            - 어떤 Old Region 에 대상이 얼마나 있는지 최종적으로 계산해서 Mixed GC 준비
    - Mixed GC
        - 위의 단계가 끝나면 G1GC는 어떤 Old region 이 GC 대상인지 알게 된다.
        - 그래서 여러번에 걸쳐 Mixed GC 를 수행한다.
        - 모든 Young Region 이랑 쓰레기가 많은(GC 대상이 되는) Old Region 을 묶어서 함께 GC 수행
            - 이 단계에서도 살아남은 객체들을 다른 region 으로 옮기기 때문에 파편화는 일어나지 않는다.

### 동작 중 Full GC 발생 시나리오

- Young-Only phase 에서 survivor 객체 복사를 위한 free region 이 부족할 때
    - 즉 메모리에 할당하는 속도가 GC 회수 속도를 앞설 때
- Space-Reclamation phase 에서 concurrent marking 단계에서 Old 영역 객체 할당 속도를 따라잡지 못할 때

이런 경우가 발생하면 Heap 관련 혹은 GC 튜닝이 필요하다. 
혹은 애플리케이션 메모리 분석이 필요한 순간이 온다.

## 장점

- STW 가 짧고, 예측 가능하다.
    - 멈춤 시간을 설정하면 G1GC가 알아서 맞춰서 해준다.
    - 그리고 전체 과정은 오래 걸릴 수 있지만 STW 가 걸리는 시간 자체는 짧다.
- 대용량 Heap 에서 효율적이다.
    - parallel GC 에서 Heap 이 커지면 Full GC 발생 시 STW 가 오래 걸리기 때문
    - 근데 G1GC는 Heap 이 커져도 concurrent Marking 을 통해 STW 없이 체크가 가능하기 때문에 효율적이고, 반대로 Heap 크기가 작으면 오버헤드가 있어서 성능 면에 손해가 있을 수 있다.

## 단점

- 처리율 저하
    - 아무래도 GC 과정이 백그라운드에서 수행되는 과정이 있기 때문에(Concurrent GC 등) 추가적으로 CPU 자원을 소모한다.
- 메모리 오버헤드
    - 다른 region 에 참조가 있기 때문에 이를 추적하기 위한 추가적인 자료 구조(Remembered Sets 등) 가 있어 메모리 오버헤드 발생

기본적으로 API 등에서는 STW가 적어서 효율적이지만 Batch 등에서는 처리율의 한계로 Parallel 이 효율적일 수 있는 여지는 있을 듯.
