# Controller

쿠버네티스의 컨트롤러는 서비스 관리와 운영에 도움을 준다.

- Auto Healing
    - 노드 위에 파드가 있는데, 파드가 다운되거나 노드가 다운되면 컨트롤러가 이를 즉각적으로 인지하고 파드를 다른 노드 위에 즉시 만들어 준다.
- Auto Scaling
    - 파드의 리소스가 limit 상태가 되었을 때 파드를 하나 더 만들어 줌으로써 부하를 분산시켜 준다.
    - 성능 장에 없이 안정적인 운영 가능
- Software Update
    - 파드 업그레이드를 한번에 쉽게 하도록 해주고, 롤백도 쉽게 해준다.
- Job
    - 일시적으로 특정 작업을 해야할 때에 이를 위한 파드를 만들어준 후에 사용 후 삭제
    - 효율적인 자원 활용이 가능해진다.

## Replication Controller, RelicaSet

- Replication Controller (Deprecated)
    - Controller 와 pod 는 라벨과 셀렉터로 연결된다.
    - Template
        - Template으로 파드의 내용을 넣는다.
            - 얘는 파드가 죽으면 재생성시키는데, 이 특성을 사용해서 업그레이드 가능
                - 템플렛에 v2 파드로 업데이트 하고 기존 애들을 다운시키면 controller 는 template을 가지고 파드를 재생성하기 때문에 수동 업그레이드 가능해진다.
    - Replicas
        - 그 숫자 만큼 pod 의 갯수가 관리되고 숫자 만큼 파드를 만들어준다.(scale out)
        - 그리고 앞의 Template 과 함꼐 사용하면 pod 를 따로 만들지 않아도 replicas 수 만큼 템플렛의 파드 내용을 통해 파드를 만든다.
            - 이렇게 하면 바로바로 생성 가능
- ReplicaSet
    - 참고로 Template, Relicas 는 여기도 있다.
    - Selector (이 기능은 여기만 있음)
        - key - value 모두 같은 pod 와 replicaSet 과 연결해준다.
        - matchExpressions 같은 애는 설정해 준 것을 선택하도록 해준다.
            - Exists -> key 를 정하고 그에 맞춘 값을 가진 애들을 연결
            - DoesNotExist -> 반대로 없는걸 선택
            - In -> key에 대해 value 가 맞는 걸 선택
            - NotIn -> key에 대해 value 가 다른 걸 선택
        - 주의점
            - Selector 에 있는 내용이 template 의 metadata 의 labels 에 포함되어야 한다.


## Deployment

현재 한 서비스가 운영 중인데, 이 서비스를 업데이트 해야 해서 재배포 해야할 때 도움을 주는 컨트롤러이다.

- Recreate
    - 파드들이 자원을 사용하고 있다고 가정할 때
    - Recreate 하면 -> 파드들이 꺼지고 자원 사용이 없어지고 이후 생성
    - Down Time 이 발생하기 때문에 일시적인 서비스 다운이 있어도 괜찮을 때에 사용
- Rolling Update
    - 파드들이 자원을 사용하고 있다고 가정할 때
    - 먼저 v2의 파드를 하나 만들어 준다.(그만큼 자원 사용량이 늘어날 것이다.)
        - v1, v2 모두에 서비스가 만들어졌기 때문에 누군가는 v1, 누군가는 v2에 들어간다.
    - 그리고 v1을 지워준다.
    - 혹시 v1이 또 남아 있다면 그만큼 위의 내용을 반복
    - Down Time 이 없다는 장점이 있고 v2 를 하나 더 운영할 만큼의 자원이 요구된다.
- Blue/Green
    - Deployment 말고 controller 를 활용해서 가능
    - Controller 를 통해 만든 것에 대해 (v1)
    - 새로운 Controller 에 새로운 버전을 만들어 준다.(v2)
        - 이 때 자원 사용량은 기존의 두배가 된다.
    - 서비스의 라벨을 변경하게 되면 Service 의 라벨을 변경하여 기존 파드와의 관계를 끊고 v2 버전의 새 파드와 연결이 된다.
    - 서비스의 다운타임은 없다.(순간적으로 바꾸기 때문)
    - 그리고 혹시 문제가 있으면 롤백도 가능하다(v1이 남아있기 때문)
    - 위의 장점이 있기는 한데 자원 소모가 두배지요
- Canary
    - v1의 pod 들이 있고 ty:app 이 있을 때
    - v2의 pod 를 만들고 걔도 ty:app 으로 둔다.
    - 그리고 Service 에서 ty:add 으로 쓰게 하면 v1, v2 둘다 접근된다.
    - 그래서 문제가 있는지 확인
    - 혹은 Service 를 두 개 만들어서(v1, v2 각각을 다는 service)
    - Ingress Controller 에서 어디서 오는지에 따라 어떤 Service 를 타게 할지를 정할 수 있다.
    - 테스트기간 동안 문제가 없으면 기존 내용들을 삭제하면 된다.
    - DownTime 은 없지만 자원 소모량은 많이 늘 수 있다.

## DaemonSet, Job, CronJob

- DaemonSet
    - 각각 노드들이 있고 자원이 다르게 남아있는 상황에서 볼 때
    - ReplicaSet 의 경우는 자원이 많이 남아있는 곳에는 Pod 를 많이 배치하고 적게 남아있는 곳에는 조금 배치하거나 안 할 것이다.
    - DaemonSet은 노드의 자원 상태와 상관 없이 균일하게 하나씩 생긴다는 특징이 있다.
        - 포트메테우스(성능 수집), Fluentd(로그 수집), GlusterFS(Storage) 등등
- Job
    - 파드를 직접 만들거나, ReplicaSet을 통해 만들거나 Job에 의해 만들거나..
    - 파드들이 Node1 에서 돌아가다가 Node1이 다운되면
        - 직접 만든 경우는 장애 발생 - 서비스 유지 불가
        - ReplicaSet에 의해 만들어진 경우는 다른 노드에 재생성(Recreate)된다. - 서비스 유지 가능
            - Replicas 에 의해 만들어진 pod 는 일하지 않으면 Restart 에 의해 서비스 재개
                - Restart 와 Recreate의 차이는 Recreate는 파드를 다시 만들기 떄문에 IP 같은 것들이 변경된다. Restart는 컨테이너만 재기동
        - Job 에 의해 만들어진 경우는 다른 노드에 재생성(Recreate)된다. - 서비스 유지 가능
            - 얘는 프로세서가 일하지 않으면 파드가 종료된다.(자원 사용 X)
                - 파드 안에 들어가서 내용 확인 가능.
                    - 필요없으면 알아서 지우시고
- CronJob
    - 위의 Job 들을 주기에 따라서 사용할 수 있도록 한다.
    - Backup이나 Checking, Messaging 등등 작업에서 사용된다.
