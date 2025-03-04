# Pod

## LifeCycle

- pod 가 있고, Status 안에 
    - 파드 전체 상태를 대표하는 phase 속성
        - pending
            - InitContainer 라고 본 컨테이너 기동 전에 초기에 수행해야 할 내용이 있는 경우 그 내용을 담는 컨테이너가 있다.
                - 요게 본 컨테이너 생성보다 먼저 수행되어야 한다.
            - 파드 생성 전에 해야하는 작업들 수행 과정
        - running
            - 참고로 이거 pod는 running 인데 내부 container 들이 running 이 아닐 수도 있다.
                - 모든 contidion 이 true 이도록 유지하는게 좋다.
                - 컨테이너 상태의 모니터링도 필요함!
            - 파드가 더이상 일을 하지 않는다면 succeeded 나 failed 로 간다.
        - succeeded
            - 컨테이너 모두가 다 작업을 잘 마치고 성공하면 일로감.
        - failed
            - 컨테이너 중 하나라도 error 코드와 함께 종료되면 일로간다.
            - 혹시라도 재시작 중이면 Runnnig 상태가 유지되는것.
        - unknown
            - pending이나 running 중 통신 장애가 발생하면 unknown 으로 가는데 이게 지속되면 failed 로 간다.
    - 파드가 실행되면서 나타나는 단계와 상태를 알려주는게 Conditions 속성
        - Initialized
        - ContainerReady
        - PodScheduled
        - Ready
        - 그리고 요 Conditions 의 세부 내용을 알려주는 Reason 항목도 있다.
    - 파드 안에 컨테이너마다 각각 컨테이너를 대표하는 상태가 State
        - Waiting
        - Running
        - Terminated
        - 그리고 위의 State 의 세부 내용을 알려주는 Reason

## ReadinessProbe, LivenessProbe

파드를 만들면 그 안에 컨테이너가 생기고 그것들이 잘 running 되면서 그 안에 앱도 정상적으로 구동이 될 것이다.
그리고 Service 에 구동이 되고 외부에서 이 Service 의 IP를 통해 실시간 접근을 하는것

그런데 이렇게 잘 동작하다가 node2 가 failed 된다면 일단 node1 로 동작하면서 트래픽을 받고 node3 에 auto healing 을 통해 다른 노드에 재생성될 것이다.
이 node3 에서 pod 와 container 은 Running 중인데 App 은 구동이 안되어서 사용자의 절반은 에러 페이지를 보게 될 것이다.

그래서 ReadinessProbe 를 두게 되면 App 구동 순간에 트래픽 실패를 없앤다.
근데 만약에 App 은 Down 되었는데 Pod 나 Container 은 멀쩡한 경우가 있다. 이를 확인해주는게 LivenessProbe 이다. (문제가 생기면 Pod 를 재실행하게 한다.)

그래서

- ReadinessProbe
    - App 구동 순간에 트래픽 실패를 없앰
- LivenessProbe
    - App 장애시 지속적인 트래픽 실패를 없앰

이다.

## QoS classes

이게 어떨때 필요할까?
한 노드 위에 파드가 3개 있고 각각이 균등하게 자원을 사용하고 있는 상태라고 가정한다.
그런데 pod1 이 추가적으로 자원을 더 사용해야 하는 상황이라면, 기존 방식으로는 node 자체에 남은 자원이 없다면 어떻게 해야할까?
그냥 자원이 없어서 pod1 이 멈추는 것이 좋을지, 아니면 pod2 혹은 pod3 을 멈추고 그 자원을 pod1 에 주는게 좋을지를 생각해 봐야 한다.
Quality of Service 라는 것은 pod 별 중요도를 두고 어떤 것을 다운시킬지 우선순위를 정할 수 있다.
BestEffort, Burstable, Guaranteed 3개가 있어서 가장 우선순위가 낮은 BestEffort 를 다운시키고 이 자원을 나눠주는 것이 가능하다.
container resource 설정에서 memory, cpu 설정을 어떻게 해주냐에 따라 설정되는데(즉 명시적으로 정할 수 없음)

- Guaranteed
    - 파드에 여러 컨테이너가 있다면
    - 모든 Container 에 Request 와 limit 가 설정되어 있어야 한다.
    - Request와 Limit 에는 Memory와 CPU가 모두 설정되어 있어야 한다.
    - 각 Container 내에 Memory와 CPU의 Request와 Limit 값이 같음.
- Burstable
    - 저 둘의 중간
    - 그러면 이게 Guaranteed도 아니고 BestEffort 도 아닌게 많은데 뭐가 먼저 제거될지를 어떻게 알 수 있는가?
        - 각각 Memory의 Request 와 실제 사용량이 있을 때 Usage가 더 높은 애가 먼저 삭제된다.
            - 예를 들어 1번은 Request가 5G인데 4G 사용중, 2번은 Request가 8G인데 4G 사용중이면 1번이 먼저 제거된다. (OOM Score 이 높음)
- BestEffort
    - 파드의 어떤 Container 에도 Request 와 Limit 미설정

## Node Scheduling

파드는 기본적으로 스케쥴러에 따라 지정되지만 사용자가 지정할수도 있다.
쿠버네티스는 다양한 기능으로 이를 지원한다.

- Node 선택
    - 파드를 특정 노드에 할당되도록 선택하기 위한 용도
    - NodeName
        - 스케쥴러와 상관 없이 해당 노드의 이름으로 바로 할당 가능
        - 명시적으로 파드를 할당할 수 있는데 잘 사용 안함
    - NodeSelector
        - 특정 노드 추가할 때에 권장되는 방식
        - label key-value 가 같은 노드에 맞춰서 할당
            - 근데 만약 두개가 같은 key-value 라면 둘 중 남은 자원이 많은 쪽에 pod가 할당된다.
        - 그리고 key-value 가 완전히 같은게 없다면 pod 가 할당이 안돼서 에러가 발생한다.
    - NodeAffinity
        - 위의 NodeSelector 의 보완
        - pod 에 key 만 설정하면 해당 그룹의 스케쥴러를 통해 자원이 많은 곳에 할당한다.
            - 그리고 아예 key 가 맞는게 아예 없더라도 스케쥴러가 알아서 판단해서 자원이 많은 노드에 pod 가 할당되도록 옵션을 줄 수 있다.
                - 아예 스케줄링되지 않을 수도 있고 조건이 맞지 않아도 스케줄링 되도록 설정할 수도 있고
- Pod 간 집중 / 분산
    - 여러 파드들을 한 노드에 집중해서 할당하거나 pod 들 간에 겹치는 노드 없이 분산해서 할당 가능
    - PodAffinity
        - 같은 Node 에 pod 를 할당하기 위해 label key-value를 동일하게 하면 PV 가 node 에 생기고 같은 곳에 만들어지게 된다.
    - Anti-Affinity
        - 마스터가 죽으면 슬레이브가 그 역할을 대신해야 하므로 기존에 있던 곳과 다른 곳에 들어가야 한다.
        - 그래서 이거를 설정해주면 동일한 key-value 로 설정하면 각각 pod 가 다른 node 로 들어가게 된다.
- Node 에 할당제한
    - 특정 노드에는 아무 파드나 할당되지 않도록 제한
    - Toleration
    - Taint
        - 이거 설정하면 일반적인 파드는 할당되지 않고 Toleration을 가진 pod 만 여기 할당이 된다.
        - NoSchedule : Toleration이 없으면 Pod가 스케줄링되지 않음
        - PerferNoSchedule : 되도록이면 스케줄링되지 않지만 강제는 아님
        - NoExecute : Toleration 이 없으면 이미 존재하는 Pod도 삭제됨
