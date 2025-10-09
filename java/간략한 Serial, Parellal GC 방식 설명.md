# 간략한 Serial, Parellal GC 방식 설명

## Heap Memory 구조

- Young Generation
    - 새로 생성된 객체 대부분이 여기 있다.
    - 이거는 3개로 나뉜다. `eden`, `survivor0`, `survivor1`
        - Eden : 새로 생기면 여기 있음
        - Survivor0, Survivor1 : Eden 에서 최소 한 번 이상 살아남으면 여기 있다. 하나의 영역에 모든 애들이 존재하고 Minor GC 단계에서 스와핑함
- Old Generation
    - Young Generation 에서 여러번 GC가 있어도 살아남은 친구들을 여기로 옮긴다.
    - 이거는 오랫동안 사용될 가능성이 높은 친구들이다.

## 동작

- Minor GC
    - Eden 영역이 꽉차면 발생한다.
    - Mark and Copy로 동작한다.
        - STW
        - Mark
            - Stack 영역이나 static 변수 등에서부터 시작해서 Eden, Survivor 영역의 살아있는 객체(아직 호출/참조중인 칭구들)을 찾아낸다.
        - Copy
            - 살아있는 친구들을 복사한다. (이동이 아님)
                - Eden -> Survivor 영역으로 복사
                - Survivor -> Survivor 다른 영역으로 복사 (이 때 age 를 하나씩 증가시킴)
        - Promotion
            - 위의 Survivor 에서 나이가 특정 임계값을 넘으면 Old Generation 으로 이동
        - Clean / Reset
            - 복사가 완료된 후에 기존 Eden, 복사처리된 Survivor 에 있는 모든 객체는 싹 비워버림
        - STW 해제
    - 이거는 자주 발생하고(Eden 영역이 꽉차면 발생하니) 처리 속도도 빠르다.
- Major GC
    - 참고로 Major GC 는 Old 영역, Full GC 는 Old+Young 영역 모두 정리하는것인데, 사실상 Major GC 하면 Full GC 도 같이 나오니 비슷하다 봐도 됨
    - Old 영역이 꽉차면, 혹은 위의 Promotion 단계에서 대상 공간이 부족할 때, 혹은 `System.gc()` 호출 시 발생한다.
    - Mark-Sweep-Compact 로 동작한다.
        - STW
        - Mark
            - GC Roots 에서부터 Heap 전체를 스캔해서 살아있는 모든 객체 확인
                - GC Roots 는 이름과는 다르게 **절대 쓰레기일 리 없는 시작점**을 뜻한다. 여기서 스캔된 친구들은 GC 대상이 아니라는 것.
        - Sweep
            - 더이상 참조되지 않거나 호출되지 않고 있는 모든 객체를 메모리에서 제거
            - 이게 끝나면 메모리의 중간중간 단편화 발생
        - Compact
            - 위의 단편화 해결을 위해 살아있는 객체들을 이동시켜서 연속된 공간으로 만든다.
            - 참고로 여기서 Address 를 바꾸면 이걸 참조하고 있던 모든 곳을 찾아가서 기존 주소를 다 업데이트 해야한다. 여기서 시간이 엄청 소요된다.
        - STW 해제
    - 이게 꽤 길기 때문에, GC 튜닝은 보통 이 Major/Full GC 시간을 줄이는 것을 목표로 한다.

## 그래서 Serial GC 랑 Parellel GC 차이는 뭐냐

- 걍 이름에서 알 수 있듯 parallel GC 는 여러개의 스레드를 통해 GC 수행
