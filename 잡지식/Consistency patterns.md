## Consistency patterns

- 질문
    - Fail-over와 Replication의 차이
        - Fail-over는 고가용성을 유지하기 위해 하나의 시스템이 고장나면 다른 시스템이 즉시 역할을 대신하도록 설계됨.
            - Active-passive와 Active-active 방식
        - Replication은 데이터의 복제본을 유지해 시스템 간 동기화를 보장하는 방식입니다.
            - 이는 데이터 무결성과 고가용성을 모두 확보 가능
        - 그러면 Replication 에는 Fail-over 가 포함되는 개념인가?
            - Fail-over 를 할 수 있는 방법 중에 Replication 이 있고, 이러면 성능도 해결 가능?!
    - 병렬 구성에서 가용성이 낮은 것들을 쓰는데 총 가용성이 올라가는 이유?
        - 다른것이 대체 가능
        - 그러면 반대는?
            - 걔는 순서대로 가야해서…안됨
